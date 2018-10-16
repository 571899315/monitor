package monitor.metrics.jdbc;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class JdbcWrapper {

    private static final Log LOGGER = LogFactory.getLog(JdbcWrapper.class);

    static Map<String, String> datasourceConfigMetadata = new LinkedHashMap<String, String>();
    static final Pattern PATTERN = Pattern.compile("(?i)[a-z]+");

    static {
        String[] metadata = {
                "url",
                "initialSize",
                "maxIdle",
                "maxActive",
                "maxWait",
                "minIdle"
        };
        for (String each : metadata) {
            datasourceConfigMetadata.put(each, each);
        }

    }

    private static final Map<String, Object> DATASOURCE_CONFIG_PROPERTIES = new LinkedHashMap<String, Object>();

    private static final AtomicInteger ACTIVE_CONNECTION_COUNT = new AtomicInteger();
    private static final AtomicInteger HOLDED_CONNECTION_COUNT = new AtomicInteger();
    private static final AtomicLong USED_CONNECTION_COUNT = new AtomicLong();
    private static final Map<Integer, LeakConnectionInformations> HOLD_CONNECTION_INFORMATIONS
            = new ConcurrentHashMap<Integer, LeakConnectionInformations>();

    private static final int MAX_USED_CONNECTION_INFORMATIONS = 500;

    JdbcWrapper() {
    }

    static final class ConnectionInformationsComparator
            implements Comparator<LeakConnectionInformations>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(LeakConnectionInformations connection1, LeakConnectionInformations connection2) {
            return new Date(connection1.getOpeningTime()).compareTo(new Date(connection2.getOpeningTime()));
        }
    }

    /**
     * Statement | PreparedStatement InvocationHandler
     */
    private class StatementInvocationHandler implements InvocationHandler {
        private String requestName;
        private final Statement statement;

        StatementInvocationHandler(String query, Statement aStatement) {
            super();
            assert aStatement != null;

            this.requestName = query;
            this.statement = aStatement;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final String methodName = method.getName();
            if (isEqualsMethod(methodName, args)) {
                return statement.equals(args[0]);
            } else if (isHashCodeMethod(methodName, args)) {
                return statement.hashCode();
            } else if (methodName.startsWith("execute")) {
                if (isFirstArgAString(args)) {
                    requestName = (String) args[0];
                }
                requestName = String.valueOf(requestName);
                return doExecute(requestName, statement, method, args);
            } else if ("addBatch".equals(methodName) && isFirstArgAString(args)) {
                requestName = (String) args[0];
            }
            return method.invoke(statement, args);
        }

        private boolean isFirstArgAString(Object[] args) {
            return args != null && args.length > 0 && args[0] instanceof String;
        }
    }

    boolean isConnectionInformationsEnabled() {
        return true;
    }

    /**
     * Connection InvocationHandler
     */
    private class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection connection;
        private boolean alreadyClosed;

        ConnectionInvocationHandler(Connection aConnection) {
            super();
            assert aConnection != null;
            this.connection = aConnection;
        }

        void init() {
            if (isConnectionInformationsEnabled()
                    && HOLD_CONNECTION_INFORMATIONS.size() < MAX_USED_CONNECTION_INFORMATIONS) {
                HOLD_CONNECTION_INFORMATIONS.put(
                        LeakConnectionInformations.getUniqueIdOfConnection(connection),
                        new LeakConnectionInformations());
            }
            HOLDED_CONNECTION_COUNT.incrementAndGet();
            USED_CONNECTION_COUNT.incrementAndGet();
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final String methodName = method.getName();
            if (isEqualsMethod(methodName, args)) {
                return areConnectionsEquals(args[0]);
            } else if (isHashCodeMethod(methodName, args)) {
                return connection.hashCode();
            }
            try {
                Object result = method.invoke(connection, args);
                if (result instanceof Statement) {
                    final String requestName;
                    if ("prepareStatement".equals(methodName) || "prepareCall".equals(methodName)) {
                        requestName = (String) args[0];
                    } else {
                        requestName = null;
                    }
                    result = createStatementProxy(requestName, (Statement) result);
                }
                return result;
            } finally {
                if ("close".equals(methodName) && !alreadyClosed) {
                    HOLDED_CONNECTION_COUNT.decrementAndGet();
                    HOLD_CONNECTION_INFORMATIONS
                            .remove(LeakConnectionInformations.getUniqueIdOfConnection(connection));
                    alreadyClosed = true;
                }
            }
        }

        private boolean areConnectionsEquals(Object object) {
            if (Proxy.isProxyClass(object.getClass())) {
                final InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
                if (invocationHandler instanceof DelegatingInvocationHandler) {
                    final DelegatingInvocationHandler d = (DelegatingInvocationHandler) invocationHandler;
                    if (d.getDelegate() instanceof ConnectionInvocationHandler) {
                        final ConnectionInvocationHandler c = (ConnectionInvocationHandler) d
                                .getDelegate();
                        return connection.equals(c.connection);
                    }
                }
            }
            return connection.equals(object);
        }
    }

    private abstract static class AbstractInvocationHandler<T>
            implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("all")
        private final T proxiedObject;

        AbstractInvocationHandler(T aProxiedObject) {
            super();
            this.proxiedObject = aProxiedObject;
        }

        T getProxiedObject() {
            return proxiedObject;
        }
    }

    private static class DelegatingInvocationHandler implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 7515240588169084785L;
        @SuppressWarnings("all")
        private final InvocationHandler delegate;

        DelegatingInvocationHandler(InvocationHandler aDelegate) {
            super();
            this.delegate = aDelegate;
        }

        InvocationHandler getDelegate() {
            return delegate;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return delegate.invoke(proxy, method, args);
            } catch (final InvocationTargetException e) {
                if (e.getTargetException() != null) {
                    throw e.getTargetException();
                }
                throw e;
            }
        }
    }

    Object doExecute(String requestName, Statement statement, Method method, Object[] args)
            throws IllegalAccessException, InvocationTargetException {
        assert requestName != null;
        assert statement != null;
        assert method != null;

        if (requestName.startsWith("explain ")) {
            ACTIVE_CONNECTION_COUNT.incrementAndGet();
            try {
                return method.invoke(statement, args);
            } finally {
                ACTIVE_CONNECTION_COUNT.decrementAndGet();
            }
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        final long beginTime = System.currentTimeMillis();
        try {
            ACTIVE_CONNECTION_COUNT.incrementAndGet();
            return method.invoke(statement, args);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof SQLException) {
                final int errorCode = ((SQLException) e.getCause()).getErrorCode();
                final int n = 20000, m = 30000;
                if (errorCode >= n && errorCode < m) {
                    Map<String, Object> expayload = new LinkedHashMap<String, Object>();
                    expayload.put("type", "SQL");
                    expayload.put("class", e.getCause().getClass().getSimpleName());
                    expayload.put("msg", e.getCause().getMessage());
                    expayload.put("beginTime", System.currentTimeMillis());
//TODO                	MetricsManager.collect(MetricsType.EXCEPTION,expayload);
//                	//保存异常，在调用链中只显示一个异常
//                	if(MetricsTraceContextHolder.getMetricsTraceContext()!= null){
//                		MetricsTraceContextHolder.getMetricsTraceContext().addThrowable(e);
//                	}
                }
            }
            throw e;
        } finally {
            ACTIVE_CONNECTION_COUNT.decrementAndGet();

            Matcher m = PATTERN.matcher(requestName);
            if (m.find()) {
                payload.put("type", m.group().toLowerCase());
            }
            long endTime = System.currentTimeMillis();
            //设置调用方法名称
            payload.put("sql", requestName);
            //开始时间
            payload.put("beginTime", beginTime);
            //记录执行的时间
            payload.put("duration", endTime - beginTime);
            //记录sql长度
            payload.put("length", requestName.length());
            //发送监控记录
            //TODO MetricsManager.collect(MetricsType.SQL,payload);
        }
    }

    /**
     * 创建代理数据源对象
     *
     * @param name
     * @param dataSource
     * @return
     */
    DataSource createDataSourceProxy(String name, final DataSource dataSource) {
        assert dataSource != null;
        pullDataSourceConfigProperties(name, dataSource);
        final InvocationHandler invocationHandler = new AbstractInvocationHandler<DataSource>(
                dataSource) {
            private static final long serialVersionUID = 1L;

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(dataSource, args);
                if (result instanceof Connection) {
                    result = createConnectionProxy((Connection) result);
                }
                return result;
            }
        };
        return createProxy(dataSource, invocationHandler);
    }

    /**
     * 拉取数据源的配置属性
     *
     * @param name
     * @param dataSource
     */
    private void pullDataSourceConfigProperties(String name, DataSource dataSource) {
        DATASOURCE_CONFIG_PROPERTIES.clear();
        for (Map.Entry<String, String> each : datasourceConfigMetadata.entrySet()) {
            try {
                Object result = BeanUtils.getProperty(dataSource, each.getValue());
                if (result != null) {
                    DATASOURCE_CONFIG_PROPERTIES.put(each.getKey(), result);
                }
            } catch (Throwable e) {
                LOGGER.warn("DataSource " + name + " miss property:" + each);
            }
        }
        LOGGER.info("DataSourceWrapper properties: " + DATASOURCE_CONFIG_PROPERTIES);
    }

    /**
     * 创建代理Connection对象
     *
     * @param connection
     * @return
     */
    Connection createConnectionProxy(Connection connection) {
        assert connection != null;
        final ConnectionInvocationHandler invocationHandler = new ConnectionInvocationHandler(
                connection);
        final Connection result;
//        if (jonas) {
//            result = createProxy(connection, invocationHandler,
//                    Arrays.asList(new Class<?>[] { Connection.class }));
//        } else {
        result = createProxy(connection, invocationHandler);
//        }
        if (result != connection) { // NOPMD
            invocationHandler.init();
        }
        return result;
    }

    /**
     * 创建Statement|PreparedStatement对象
     *
     * @param query
     * @param statement
     * @return
     */
    Statement createStatementProxy(String query, Statement statement) {
        assert statement != null;
        final InvocationHandler invocationHandler = new StatementInvocationHandler(query,
                statement);
        return createProxy(statement, invocationHandler);
    }

    boolean isEqualsMethod(Object methodName, Object[] args) {
        return "equals".equals(methodName) && args != null && args.length == 1; // NOPMD
    }

    boolean isHashCodeMethod(Object methodName, Object[] args) {
        return "hashCode".equals(methodName) && (args == null || args.length == 0);
    }

    int getHoldedConnectionCount() {
        return HOLDED_CONNECTION_COUNT.get();
    }

    int getActiveConnectionCount() {
        return ACTIVE_CONNECTION_COUNT.get();
    }

    long getUsedConnectionCount() {
        return USED_CONNECTION_COUNT.get();
    }

    int getMaxActive() {
        if (DATASOURCE_CONFIG_PROPERTIES.containsKey("maxActive")) {
            return Integer.parseInt(DATASOURCE_CONFIG_PROPERTIES.get("maxActive").toString());
        }
        return -1;
    }

    int getMaxIdle() {
        if (DATASOURCE_CONFIG_PROPERTIES.containsKey("maxIdle")) {
            return Integer.parseInt(DATASOURCE_CONFIG_PROPERTIES.get("maxIdle").toString());
        }
        return -1;
    }

    int getMinIdle() {
        if (DATASOURCE_CONFIG_PROPERTIES.containsKey("minIdle")) {
            return Integer.parseInt(DATASOURCE_CONFIG_PROPERTIES.get("minIdle").toString());
        }
        return -1;
    }

    String getUrl() {
        if (DATASOURCE_CONFIG_PROPERTIES.containsKey("url")) {
            return DATASOURCE_CONFIG_PROPERTIES.get("url").toString();
        }
        return "";
    }

    int getMaxWait() {
        if (DATASOURCE_CONFIG_PROPERTIES.containsKey("maxWait")) {
            return Integer.parseInt(DATASOURCE_CONFIG_PROPERTIES.get("maxWait").toString());
        }
        return -1;
    }

    Map<String, Object> getDBConfigProperties() {
        return DATASOURCE_CONFIG_PROPERTIES;
    }

    List<LeakConnectionInformations> getHoldedConnectionInformationsList() {
        final List<LeakConnectionInformations> result = new ArrayList<LeakConnectionInformations>(
                HOLD_CONNECTION_INFORMATIONS.values());
        Collections.sort(result, new ConnectionInformationsComparator());
        return Collections.unmodifiableList(result);
    }

    static <T> T createProxy(T object, InvocationHandler invocationHandler) {
        return createProxy(object, invocationHandler, null);
    }

    static <T> T createProxy(T object, InvocationHandler invocationHandler,
                             List<Class<?>> interfaces) {
        if (isProxyAlready(object)) {
            return object;
        }
        InvocationHandler ih = new DelegatingInvocationHandler(invocationHandler);
        return JdbcWrapperHelper.createProxy(object, ih, interfaces);
    }

    private static boolean isProxyAlready(Object object) {
        return Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object)
                .getClass().getName().equals(DelegatingInvocationHandler.class.getName());
    }

}

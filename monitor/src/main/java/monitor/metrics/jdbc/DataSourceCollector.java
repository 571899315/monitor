package monitor.metrics.jdbc;

import monitor.metrics.scheduler.MetricsCollector;
import monitor.metrics.spring.BeanPostProcessorWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataSourceCollector implements BeanPostProcessorWrapper, MetricsCollector {

	final Logger logger = LoggerFactory.getLogger(DataSourceCollector.class);

	private Map<String, JdbcWrapper> jdbcWrapperMapper = new HashMap<String, JdbcWrapper>();

	private List<String> excludedDatasources;

	/**
	 * 设置监控数据源的属性内容,默认 dbcp | druid 不用配置,c3p0 | jndi数据源需要配置
	 *
	 * @param configMetadata
	 */
	public void setConfigMetadata(String configMetadata) {

		if (!StringUtils.isEmpty(configMetadata)) {
			Map<String, String> hash = new LinkedHashMap<String, String>();
			for (String each : configMetadata.split(",")) {
				String[] kv = each.split(":");
				hash.put(kv[0], kv[1]);
			}
			JdbcWrapper.datasourceConfigMetadata = hash;
			logger.info("CONFIG_METADATA: " + hash);
		}
	}

	/**
	 * 不需要监控的数据源
	 *
	 * @param aExcludedDatasources
	 */
	public void setExcludedDatasources(String aExcludedDatasources) {
		if (!StringUtils.isEmpty(aExcludedDatasources)) {
			logger.info("ExcludedDatasources: " + aExcludedDatasources);
			this.excludedDatasources = Arrays.asList(aExcludedDatasources.split(","));
		}
	}

	private boolean isExcludedDataSource(String beanName) {
		if (excludedDatasources != null && excludedDatasources.contains(beanName)) {
			logger.info("Spring datasource excluded: " + beanName);
			return true;
		}
		return false;
	}

	private Object createProxy(final Object bean, final String ctxId, final String beanName) {
		final InvocationHandler invocationHandler = new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = method.invoke(bean, args);
				if (result instanceof DataSource) {
					JdbcWrapper jdbcWrapper = new JdbcWrapper();
					jdbcWrapperMapper.put((!StringUtils.isEmpty(ctxId) ? (ctxId + ":") : "") + beanName, jdbcWrapper);
					result = jdbcWrapper.createDataSourceProxy(beanName, (DataSource) result);
				}
				return result;
			}
		};
		return JdbcWrapper.createProxy(bean, invocationHandler);
	}

	public boolean interest(Object bean) {
		return (bean instanceof DataSource) || (bean instanceof JndiObjectFactoryBean);
	}

	public synchronized Object wrapBean(Object bean, String ctxId, String beanName) {
		try {
			if (bean instanceof DataSource) {
				if (isExcludedDataSource(beanName) || beanName.matches("\\(inner bean\\).+")) {
					return bean;
				}
				String key = (!StringUtils.isEmpty(ctxId) ? (ctxId + ":") : "") + beanName;
				if (jdbcWrapperMapper.containsKey(key)) {
					logger.warn("metrics.collector ctxId=" + ctxId + ",beanName=" + beanName + " duplicated.");
					return bean;
				}
				final DataSource dataSource = (DataSource) bean;
				JdbcWrapper jdbcWrapper = new JdbcWrapper();
				jdbcWrapperMapper.put(key, jdbcWrapper);
				final DataSource result = jdbcWrapper.createDataSourceProxy(beanName, dataSource);
				logger.info("metrics.collector wrapped DataSource beanName=" + beanName);
				return result;
			} else if (bean instanceof JndiObjectFactoryBean) {
				if (isExcludedDataSource(beanName)) {
					return bean;
				}
				final Object result = createProxy(bean, ctxId, beanName);
				logger.info("metrics.collector wrapped JNDI factory beanName= " + beanName + ", bean=" + result);
				return result;
			}
		} catch (Throwable e) {
			logger.error("metrics.collector wrap bean " + beanName + " error", e);
		}
		return bean;
	}

	public synchronized void doCollect() {
		for (Entry<String, JdbcWrapper> each : jdbcWrapperMapper.entrySet()) {
			JdbcWrapper jdbcWrapper = each.getValue();
			Map<String, Object> payload = new LinkedHashMap<String, Object>();
			payload.put("dataSourceName", each.getKey());
			payload.put("url", jdbcWrapper.getUrl());
			payload.put("maxIdle", jdbcWrapper.getMaxIdle());
			payload.put("minIdle", jdbcWrapper.getMinIdle());
			payload.put("maxActive", jdbcWrapper.getMaxActive());
			payload.put("maxWait", jdbcWrapper.getMaxWait());
			payload.put("activeCount", jdbcWrapper.getActiveConnectionCount());
			payload.put("usedCount", jdbcWrapper.getUsedConnectionCount());
			payload.put("leakCount", jdbcWrapper.getHoldedConnectionCount());
			logger.info("获取JDBC信息结束。 dataSource明细：" + payload);

			// TODO MetricsManager.collect(MetricsType.JDBC,payload);

			// 当最大连接数等于Hold数需要发出告警,统计阻塞服务的名称和调用个数
			// if(jdbcWrapper.getMaxActive()== jdbcWrapper.getHoldedConnectionCount()){
			// List<LeakConnectionInformations> list=
			// jdbcWrapper.getHoldedConnectionInformationsList();
			// List<StackTraceElement[]> openingStackTraces= new
			// ArrayList<StackTraceElement[]>(list.size());
			// for(LeakConnectionInformations info: list){
			// openingStackTraces.add(info.getOpeningStackTrace());
			// }
			//// blockServiceTraceCollector.collectBlockServiceTrace(MetricsType.JDBC,
			// each.getKey(),
			// openingStackTraces);
			// }
		}
	}

	public synchronized void remove(String ctxId, String beanName) {
		jdbcWrapperMapper.remove((!StringUtils.isEmpty(ctxId) ? (ctxId + ":") : "") + beanName);
	}
}

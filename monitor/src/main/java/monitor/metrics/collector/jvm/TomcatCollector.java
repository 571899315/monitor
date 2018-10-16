package monitor.metrics.collector.jvm;

import monitor.metrics.MetricsTimer;
import monitor.metrics.scheduler.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class TomcatCollector implements MetricsCollector {

    final Logger logger = LoggerFactory.getLogger(TomcatCollector.class);

    private final boolean tomcatUsed;

    private final MBeans mBeans;

    private final List<ObjectName> threadPools = new ArrayList<ObjectName>();

    private final Map<String, ObjectName> globalRequestProcessors = new LinkedHashMap<String, ObjectName>();

    public TomcatCollector() {
        tomcatUsed = System.getProperty("catalina.home") != null;
        mBeans = new MBeans();
        if (!tomcatUsed) {
            return;
        }
        try {
            threadPools.addAll(mBeans.getTomcatThreadPools());
            logger.info("Catalina Thread Pool: " + threadPools);
            Set<ObjectName> tomcatGlobalRequestProcessors = mBeans.getTomcatGlobalRequestProcessors();
            for (ObjectName each : threadPools) {
                String name = each.getKeyProperty("name");
                for (ObjectName globalRequestProcessor : tomcatGlobalRequestProcessors) {
                    if (globalRequestProcessor.getKeyProperty("name").equals(name)) {
                        this.globalRequestProcessors.put(name, globalRequestProcessor);
                        break;
                    }
                }
            }
        } catch (MalformedObjectNameException e) {
            logger.equals(e);
            return;
        }
    }

    public void doCollect() {

        if (!tomcatUsed) {
            return;
        }
        try {

            for (ObjectName each : threadPools) {
                Map<String, Object> payload = new LinkedHashMap<String, Object>();
                String name = each.getKeyProperty("name");
                payload.put("name", name);
                if (!name.contains("http") && !name.contains("https")) {
                    continue;
                }

                Integer maxThreads = (Integer) mBeans.getAttribute(each, "maxThreads");
                Integer currentThreadCount = (Integer) mBeans.getAttribute(each, "currentThreadCount");
                Integer currentThreadsBusy = (Integer) mBeans.getAttribute(each, "currentThreadsBusy");
                payload.put("maxThreads", maxThreads);
                payload.put("currentThreadCount", currentThreadCount);
                payload.put("currentThreadsBusy", currentThreadsBusy);

                if (globalRequestProcessors.containsKey(name)) {
                    ObjectName grp = globalRequestProcessors.get(name);
                    payload.put("bytesReceived", (Long) mBeans.getAttribute(grp, "bytesReceived"));
                    payload.put("bytesSent", (Long) mBeans.getAttribute(grp, "bytesSent"));
                    payload.put("requestCount", (Integer) mBeans.getAttribute(grp, "requestCount"));
                    payload.put("errorCount", (Integer) mBeans.getAttribute(grp, "errorCount"));
                    payload.put("processingTime", (Long) mBeans.getAttribute(grp, "processingTime"));
                    payload.put("maxTime", (Long) mBeans.getAttribute(grp, "maxTime"));
                }
                //MetricsManager.collect(MetricsType.TOMCAT,payload);

                MetricsTimer.S_METRICS_DTO.setMaxThreads((Number) payload.get("maxThreads"));
                MetricsTimer.S_METRICS_DTO.setCurrentThreadsBusy((Number) payload.get("currentThreadsBusy"));
                if (MetricsTimer.OLD_METRICS_DTO.getRequestCount() != null) {
                    MetricsTimer.S_METRICS_DTO.setRequestCount(((Integer) payload.get("requestCount")
                            - (Integer) MetricsTimer.OLD_METRICS_DTO.getRequestCount()));
                } else {
                    MetricsTimer.S_METRICS_DTO.setRequestCount((Integer) payload.get("requestCount"));
                }
                if (MetricsTimer.OLD_METRICS_DTO.getErrorCount() != null) {
                    MetricsTimer.S_METRICS_DTO.setErrorCount(((Integer) payload.get("errorCount")
                            - (Integer) MetricsTimer.OLD_METRICS_DTO.getErrorCount()));
                } else {
                    MetricsTimer.S_METRICS_DTO.setErrorCount((Integer) payload.get("errorCount"));
                }
                MetricsTimer.S_METRICS_DTO.setProcessingTime((Number) payload.get("processingTime"));
                MetricsTimer.S_METRICS_DTO.setMaxTime((Number) payload.get("maxTime"));
                MetricsTimer.S_METRICS_DTO.setBlock(0);

                MetricsTimer.OLD_METRICS_DTO.setRequestCount((Number) payload.get("requestCount"));
                MetricsTimer.OLD_METRICS_DTO.setErrorCount((Number) payload.get("errorCount"));

                // 线程池满了做阻塞处理
                if (currentThreadsBusy == maxThreads) {
                    MetricsTimer.S_METRICS_DTO.setBlock(1);
                }
//                System.out.println("获取Tomcat信息结束。 metricsDTO：" + JSONObject.toJSONString(MetricsTimer.S_METRICS_DTO));
//                System.out.println("获取Tomcat信息结束。 OLD_METRICS_DTO：" + JSONObject.toJSONString(MetricsTimer.OLD_METRICS_DTO));

//                logger.info("获取Tomcat信息结束。 payload：" + payload);

            }
        } catch (JMException e) {
            logger.error("获取tomcat信息异常" + e);
        }
    }

}

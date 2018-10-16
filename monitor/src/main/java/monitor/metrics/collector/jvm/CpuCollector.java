package monitor.metrics.collector.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import monitor.metrics.MetricsTimer;
import monitor.metrics.scheduler.MetricsCollector;

public class CpuCollector implements MetricsCollector {

	private final Logger logger = LoggerFactory.getLogger(CpuCollector.class);

	private final Method method;

	public CpuCollector() {
		OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
		logger.info("OperatingSystem: " + operatingSystem.getClass());
		method = ReflectionUtils.findMethod(operatingSystem.getClass(), "getProcessCpuLoad");
		if (method != null) {
			ReflectionUtils.makeAccessible(method);
			logger.info("Will use getProcessCpuLoad method to collect cpu load.");
		} else {
			logger.info("Will use getSystemLoadAverage method to collect cpu load.");
		}
	}

	public void doCollect() {
		try {
			Map<String, Object> payload = new LinkedHashMap<String, Object>();
			// 璁剧疆鍐呭瓨绫诲埆
			// payload.put("category","cpu");
			// 鑾峰彇鎿嶄綔绯荤粺
			OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
			// 鑾峰彇澶勭悊鍣ㄦ牳鏁�
			payload.put("availableProcessors", operatingSystem.getAvailableProcessors());
			// 鎿嶄綔绯荤粺鍚嶇О
			payload.put("name", operatingSystem.getName());
			// 鎿嶄綔绯荤粺鐗堟湰鍙�
			payload.put("version", operatingSystem.getVersion());

			double systemLoadAverage;
			if (method != null) {
				double loadAverage = (Double) ReflectionUtils.invokeMethod(method, operatingSystem);
				systemLoadAverage = loadAverage;
				payload.put("systemLoadAverage", loadAverage);
			} else {
				double load = operatingSystem.getSystemLoadAverage();
				if (load > 0) {
					load = load / operatingSystem.getAvailableProcessors();
				}
				systemLoadAverage = load;
				payload.put("systemLoadAverage", load);
			}

			MetricsTimer.S_METRICS_DTO.setSystemLoadAverage(systemLoadAverage);
		} catch (Exception e) {
			logger.error("鑾峰彇cpu淇℃伅寮傚父" + e);
		}
	}
}

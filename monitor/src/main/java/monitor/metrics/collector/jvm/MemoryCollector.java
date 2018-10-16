package monitor.metrics.collector.jvm;

import monitor.metrics.MetricsTimer;
import monitor.metrics.common.MetricsConstants;
import monitor.metrics.scheduler.MetricsCollector;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryCollector implements MetricsCollector, InitializingBean {

	final Logger logger = LoggerFactory.getLogger(MemoryCollector.class);

	private Map<String, Map<String, Long>> gcvals = new LinkedHashMap<String, Map<String, Long>>(2);

	public static Number sUsedMemery;
	Map<String, String> tags = new HashMap<String, String>();

	public void doCollect() {

		try {
			Map<String, Object> payload = new LinkedHashMap<String, Object>();
			// 铏氭嫙鏈烘渶澶у唴瀛�
			payload.put("maxMemory", MetricsConstants.b2m(Runtime.getRuntime().maxMemory()));
			// 宸茬粡浣跨敤鐨勮櫄鏈哄唴瀛�
			payload.put("usedMemory",
					MetricsConstants.b2m(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
			// 鑾峰彇姘镐箙甯﹀唴瀛�
			MemoryPoolMXBean permGenMemoryPool = getPermGenMemoryPool();
			if (permGenMemoryPool != null) {
				MemoryUsage usage = permGenMemoryPool.getUsage();
				// 鏈�澶ф案涔呭甫鍐呭瓨
				payload.put("maxPermGen",
						MetricsConstants.b2m(usage.getMax() == -1 ? usage.getCommitted() : usage.getMax()));
				// 宸蹭娇鐢ㄧ殑涔呭甫鍐呭瓨
				payload.put("usedPerGen", MetricsConstants.b2m(usage.getUsed()));
			} else {
				payload.put("maxPermGen", -1);
				payload.put("usedPerGen", -1);
			}
			// 鍔犺浇鐨勭被鏁伴噺
			payload.put("loadedClassesCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
			// 寰楀埌闈炲爢鍐呭瓨瀵硅薄
			MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
			// 鏈�澶ч潪瀵瑰唴瀛�
			payload.put("maxNonHeapMemory", MetricsConstants
					.b2m(memoryUsage.getMax() == -1 ? memoryUsage.getCommitted() : memoryUsage.getMax()));
			// 浣跨敤鐨勯潪瀵瑰唴瀛�
			payload.put("usedNonHeapMemory", MetricsConstants.b2m(memoryUsage.getUsed()));
			// 鑾峰彇鍫嗗唴瀛樺璞�
			memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			// 鏈�澶у爢鍐呭瓨
			payload.put("maxHeapMemory", MetricsConstants.b2m(memoryUsage.getMax()));
			// 宸蹭娇鐢ㄧ殑涔呭甫鍐呭瓨
			payload.put("usedHeapMemory", MetricsConstants.b2m(memoryUsage.getUsed()));
			// 鑾峰彇鎿嶄綔绯荤粺瀵硅薄
			OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
			String[] opsysprops = { "totalPhysicalMemorySize", "freePhysicalMemorySize", "totalSwapSpaceSize",
					"freeSwapSpaceSize" };
			if (isSunOsMBean(operatingSystem)) {
				for (String each : opsysprops) {
					try {
						payload.put(each,
								MetricsConstants.b2m(Long.parseLong(BeanUtils.getProperty(operatingSystem, each))));
					} catch (Exception e) {
						payload.put(each, -1);
					}
				}
			} else {
				for (String each : opsysprops) {
					payload.put(each, -1);
				}
			}

			// logger.info("鑾峰彇memory淇℃伅缁撴潫銆� payload锛�" + payload);
			Double usedMemoryPercent = (Double) payload.get("usedMemory") / (Double) payload.get("maxMemory");
			Double usedHeapMemoryPercent = (Double) payload.get("usedHeapMemory")
					/ (Double) payload.get("maxHeapMemory");
			MetricsTimer.S_METRICS_DTO.setMaxMemory((Number) payload.get("maxMemory"));
			MetricsTimer.S_METRICS_DTO.setUsedMemory((Number) payload.get("usedMemory"));
			MetricsTimer.S_METRICS_DTO.setUsedMemoryPercent(usedMemoryPercent);
			MetricsTimer.S_METRICS_DTO.setLoadedClassesCount((Number) payload.get("loadedClassesCount"));
			MetricsTimer.S_METRICS_DTO.setMaxHeapMemory((Number) payload.get("maxHeapMemory"));
			MetricsTimer.S_METRICS_DTO.setUsedHeapMemory((Number) payload.get("usedHeapMemory"));
			MetricsTimer.S_METRICS_DTO.setUsedHeapMemoryPercent(usedHeapMemoryPercent);

			if (payload.get("totalPhysicalMemorySize") != null && (Double) payload.get("totalPhysicalMemorySize") != -1
					&& payload.get("freePhysicalMemorySize") != null
					&& (Double) payload.get("freePhysicalMemorySize") != -1) {
				MetricsTimer.S_METRICS_DTO.setPhysicalMemoryPercent((Double) payload.get("freePhysicalMemorySize")
						/ (Double) payload.get("totalPhysicalMemorySize"));
			}

			if (payload.get("totalSwapSpaceSize") != null && (Double) payload.get("totalSwapSpaceSize") != -1
					&& payload.get("freeSwapSpaceSize") != null && (Double) payload.get("freeSwapSpaceSize") != -1) {
				MetricsTimer.S_METRICS_DTO.setSwapSpacePercent(
						(Double) payload.get("freeSwapSpaceSize") / (Double) payload.get("totalSwapSpaceSize"));
			}

			long scavengeCount = 0;
			long scavengeTime = 0;
			long markSweepCount = 0;
			long markSweepTime = 0;

			// 閲囬泦鍐呭瓨鍨冨溇鍥炴敹淇℃伅
			for (GarbageCollectorMXBean each : ManagementFactory.getGarbageCollectorMXBeans()) {
				payload = new LinkedHashMap<String, Object>();
				// 鍐呭瓨鍖哄悕绉�
				String name = !StringUtils.isEmpty(each.getName()) ? each.getName()
						: Arrays.asList(each.getMemoryPoolNames()).toString();
				payload.put("name", name);

				Map<String, Long> vals = gcvals.get(name);
				if (vals != null) {
					// 鍨冨溇鍥炴敹鐨勬鏁�
					long count = each.getCollectionCount() - vals.get("collectionCount");
					if (count == 0) {
						continue;
					}
					if (name.contains("Scavenge")) {
						scavengeCount = count;
						scavengeTime = each.getCollectionTime() - vals.get("collectionTime");
						MetricsTimer.OLD_METRICS_DTO.setScavengeTime(scavengeTime);
					}

					if (name.contains("MarkSweep")) {
						markSweepCount = count;
						markSweepTime = each.getCollectionTime() - vals.get("collectionTime");
						MetricsTimer.OLD_METRICS_DTO.setMarkSweepTime(markSweepTime);
					}
					payload.put("collectionCount", count);
					// 鍨冨溇鍥炴敹鎸佺画鐨勬椂闂�
					payload.put("collectionTime", each.getCollectionTime() - vals.get("collectionTime"));
					// 鍐呭瓨姹犲悕绉�
					payload.put("MemoryPoolNames", Arrays.asList(each.getMemoryPoolNames()).toString());

					vals.put("collectionCount", each.getCollectionCount());
					vals.put("collectionTime", each.getCollectionTime());

					// 鍙戦�侀噰闆嗕俊鎭�
					// MetricsManager.collect(MetricsType.GC,payload);
				}
			}

			if (scavengeCount != 0 && MetricsTimer.OLD_METRICS_DTO.getScavengeCount() != null) {
				MetricsTimer.S_METRICS_DTO
						.setScavengeCount(scavengeCount - (Long) MetricsTimer.OLD_METRICS_DTO.getScavengeCount());
			} else {
				MetricsTimer.S_METRICS_DTO.setScavengeCount(scavengeCount);
			}

			if (MetricsTimer.OLD_METRICS_DTO.getScavengeTime() != null) {
				MetricsTimer.S_METRICS_DTO.setScavengeTime(
						scavengeTime != 0 ? scavengeTime : MetricsTimer.OLD_METRICS_DTO.getScavengeTime());
			} else {
				MetricsTimer.S_METRICS_DTO.setScavengeTime(scavengeTime);
			}

			if (markSweepCount != 0 && MetricsTimer.OLD_METRICS_DTO.getMarkSweepCount() != null) {
				MetricsTimer.S_METRICS_DTO
						.setMarkSweepCount(markSweepCount - (Long) MetricsTimer.OLD_METRICS_DTO.getMarkSweepCount());
			} else {
				MetricsTimer.S_METRICS_DTO.setMarkSweepCount(markSweepCount);
			}

			if (MetricsTimer.OLD_METRICS_DTO.getMarkSweepTime() != null) {
				MetricsTimer.S_METRICS_DTO.setMarkSweepTime(
						markSweepTime != 0 ? markSweepTime : MetricsTimer.OLD_METRICS_DTO.getMarkSweepTime());
			} else {
				MetricsTimer.S_METRICS_DTO.setMarkSweepTime(markSweepTime);
			}
		} catch (Exception e) {
			logger.error("鑾峰彇memory淇℃伅寮傚父" + e);
		}
	}

	private static MemoryPoolMXBean getPermGenMemoryPool() {
		for (final MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (memoryPool.getName().endsWith("Perm Gen") || memoryPool.getName().endsWith("Metaspace")) {
				return memoryPool;
			}
		}
		return null;
	}

	private boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
		String className = operatingSystem.getClass().getName();
		return "com.sun.management.OperatingSystem".equals(className)
				|| "com.sun.management.UnixOperatingSystem".equals(className)
				|| "sun.management.OperatingSystemImpl".equals(className);
	}

	public static void main(String[] args) {
		String str = "Perm Gen";
		System.out.println(str.matches("Perm\\sGen|Metaspace"));
	}

	public void afterPropertiesSet() throws Exception {

		for (GarbageCollectorMXBean each : ManagementFactory.getGarbageCollectorMXBeans()) {
			Map<String, Long> payload = new LinkedHashMap<String, Long>();
			// 鍨冨溇鍥炴敹鐨勬鏁�
			payload.put("collectionCount", each.getCollectionCount());
			// 鍨冨溇鍥炴敹鎸佺画鐨勬椂闂�
			payload.put("collectionTime", each.getCollectionTime());
			// 瀛樺偍鍒濆鍊�
			String name = each.getName();
			if (StringUtils.isEmpty(name)) {
				name = Arrays.asList(each.getMemoryPoolNames()).toString();
				logger.warn("GarbageCollectorMXBean.getName is empty, will use getMemoryPoolNames: " + name);
			}
			gcvals.put(name, payload);
		}
	}
}

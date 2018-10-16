package monitor.metrics.service;

import monitor.metrics.scheduler.MetricsCollector;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceExecutionTimeCollector implements MetricsCollector {

	static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutionTimeCollector.class);

	private static List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	public static void recordTime(Map<String, Object> payload) {
		try {
			final int n = 10;
			if (payload == null || StringUtils.isBlank(String.valueOf(payload.get("duration")))) {
				// TODO logger.info("ServiceExecutionTimeCollector接口交易时间为空" + payload);
				return;
			}
			if (list == null) {
				list = new ArrayList<Map<String, Object>>();
			}
			if (list.size() == 0) {
				list.add(payload);
				return;
			}
			for (int i = 0; i < list.size() && i < n; i++) {
				Map<String, Object> map = list.get(i);
				if (map == null) {
					list.add(i, payload);
					break;
				}
				if ((Long) payload.get("duration") > (Long) map.get("duration")) {
					list.add(i, payload);
					break;
				}
			}

			if (list.size() >= n) {
				List<Map<String, Object>> list2 = list.subList(0, n);
				list = list2;
			}
		} catch (Exception e) {
			LOGGER.error("获取http请求时间异常" + e);
		}

	}

	public void doCollect() {

		try {
			if (list == null) {
				return;
			}
			for (int i = 0; i < list.size(); i++) {
				LOGGER.info("获取接口调用时长排序。 第" + i + "个：" + list.get(i));
			}
		} catch (Exception e) {
			LOGGER.error("获取http请求时间异常" + e);
		}
	}
}

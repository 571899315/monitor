package monitor.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import monitor.domain.BaseMessageBody;

public final class MonitorMqSender {
	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 10;
	private static final long KEEP_ALIVE_SECONDS = 10;
	private static final int QUEUE_SIZE = 4000;
	private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue(QUEUE_SIZE));
	private static Logger logger = LoggerFactory.getLogger(MonitorMqSender.class);

	private MonitorMqSender() {
	}

	/**
	 * 寮傛鍙戦�佹秷鎭�
	 *
	 * @param body
	 * @param topicName
	 */

	public static void sendMessage0(BaseMessageBody body, String topicName, boolean async) {
		String bodyString = JSON.toJSONString(body);
		if (StringUtils.isBlank(bodyString) || StringUtils.isBlank(topicName)) {
	
			return;
		}
		// ProducerData data;
		if (body.getUserId() != null) {
			// data = new ProducerData(body.getUserId().toString(), bodyString);
		} else {
			// data = new ProducerData(bodyString);
		}
		try {
			if (async) {
				// getAsyncProducer().publish(topicName, data);
				// logger.info(String.format("async send business monitor
				// message,topic:%s,body:%s", topicName, body));
			} else {
				// Boolean result = getSyncProducer().publish(topicName, data);
				// logger.info(String.format("sync send business monitor
				// message,topic:%s,body:%s,result:%s",
				// topicName, body, result));
			}

		} catch (Exception e) {
			// logger.error(String.format("send business monitor message
			// error,topic:%s,body:%s", topicName, body), e);
		}
		// EXECUTOR_SERVICE.submit(thread);
	}
}

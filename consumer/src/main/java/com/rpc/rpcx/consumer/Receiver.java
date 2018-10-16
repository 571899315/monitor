package com.rpc.rpcx.consumer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Receiver {

	private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

	@Autowired
	private InfluxDBConnect connection;

	public void receiveMessage(Map<String, Object> message) {
		Map<String, String> tags = new HashMap<String, String>();
		tags.put("monitor", "monitor");
		String measurement = "test";
		connection.insert(measurement, tags, message);
		logger.info("插入成功");
	}
}

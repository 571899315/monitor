package com.rpc.rpcx.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfiguration {

	private String username = "admin";// 用户名
	private String password = "admin";// 密码
	private String openurl = "http://localhost:8086";// InfluxDB连接地址
	private String database = "monitor";// 数据库

	@Bean
	public InfluxDBConnect getInfluxDBConnect() {
		InfluxDBConnect influxDB = new InfluxDBConnect(username, password, openurl, database);
		influxDB.influxDbBuild();
		influxDB.createRetentionPolicy();
		return influxDB;
	}
}

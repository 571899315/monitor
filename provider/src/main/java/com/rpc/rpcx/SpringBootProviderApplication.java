package com.rpc.rpcx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ImportResource("classpath:spring/spring*.xml")
@EnableScheduling
public class SpringBootProviderApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringBootProviderApplication.class);
    }
}

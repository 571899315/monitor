package com.rpc.rpcx;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class Receiver {

	private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

	
    public void receiveMessage(Map<String,Object> message) {
        //System.out.println("Received <" + JSON.toJSONString(message) + ">");
    }
}

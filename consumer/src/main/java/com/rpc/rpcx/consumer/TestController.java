
package com.rpc.rpcx.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

	private static final Logger logger = LoggerFactory.getLogger(TestController.class);



	@GetMapping(value = "test")
	public String hello() {
		String result = "";

		return result;
	}

}

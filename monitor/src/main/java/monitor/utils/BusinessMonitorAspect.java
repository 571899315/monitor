package monitor.utils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import monitor.domain.BaseMessageBody;
import monitor.domain.BusinessMonitor;

public class BusinessMonitorAspect {
	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 10;
	private static final int KEEP_ALIVE_SECONDS = 10;
	private static final int QUEUE_SIZE = 4000;
	private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS,
			new LinkedBlockingQueue(4000));
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public Object afterReturn(final JoinPoint joinPoint, final Object ret) {
		try {
			Runnable runnable = new Runnable() {

				public void run() {
					try {
						logger.info("business monitor :after returning:start");
						// 获取方法上的注解内容
						final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
						BusinessMonitor businessMonitor = method.getAnnotation(BusinessMonitor.class);
						if (needSendMessage(businessMonitor, ret)) {
							sendMessage(businessMonitor, joinPoint, ret);
						}
					} catch (Exception e) {
						logger.error("business monitor :after returning:error:", e);
					}

				}
			};
			EXECUTOR_SERVICE.submit(runnable);
		} catch (Exception e) {
			logger.error("business monitor run exception", e);
		}
		return ret;
	}

	private Boolean needSendMessage(BusinessMonitor ppdBusinessMonitor, Object ret) {
		try {
			JexlContext jexlContext = new MapContext();
			jexlContext.set("ret", ret);
			JexlEngine jexl = new JexlEngine();
			Expression expression = jexl.createExpression(ppdBusinessMonitor.needSendMqMessage());
			String result = expression.evaluate(jexlContext).toString();
			if (result != null && new Boolean(result)) {
				logger.info("needSendMessage :true");
				return true;
			}
		} catch (Exception e) {
			logger.warn("run needSendMessage error", e);
			logger.info("needSendMessage :false");
			return false;
		}
		logger.info("needSendMessage :false");
		return false;
	}

	private void sendMessage(BusinessMonitor businessMonitor, JoinPoint joinPoint, Object ret)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		String topicName = businessMonitor.topicName();
		String[] initMessageBody = businessMonitor.initMessageBody();
		// String[] defaultInitMessageBody =
		// ppdBusinessMonitor.defaultInitMessageBody();
		// initMessageBody = (String[]) ArrayUtils.addAll(defaultInitMessageBody,
		// initMessageBody);
		BaseMessageBody body = (BaseMessageBody) Class.forName(businessMonitor.messageBody()).newInstance();
		JexlContext jexlContext = new MapContext();
		String[] parameters = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
		Object[] values = joinPoint.getArgs();
		for (int i = 0; i < parameters.length; i++) {
			jexlContext.set(parameters[i], values[i]);
		}
		// jexlContext.set("request", joinPoint.getArgs()[0]);
		jexlContext.set("body", body);
		jexlContext.set("ret", ret);
		JexlEngine jexl = new JexlEngine();
		for (String line : initMessageBody) {
			Expression expression = jexl.createExpression(line);
			expression.evaluate(jexlContext);
		}
		if (body.getEventTs() == null) {
			body.setEventTs(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		}
		body.setEventId(UUID.randomUUID().toString());
		
		//MonitorMqSender.sendMessage((body), topicName, true);
	}

}

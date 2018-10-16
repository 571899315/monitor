package monitor.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.alibaba.fastjson.JSON;

import monitor.metrics.collector.jvm.CpuCollector;
import monitor.metrics.collector.jvm.MemoryCollector;
import monitor.metrics.collector.jvm.TomcatCollector;
import monitor.metrics.common.IdGenerator;
import monitor.metrics.dto.MetricsDTO;
import monitor.metrics.jdbc.DataSourceCollector;
import monitor.metrics.scheduler.MetricsCollector;
import monitor.metrics.service.ServiceExecutionTimeCollector;
import monitor.utils.NumberUtil;

public class MetricsTimer extends TimerTask implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

	private final Log logger = LogFactory.getLog(MetricsTimer.class);

	private Timer mTimer;

	public static final MetricsDTO S_METRICS_DTO = new MetricsDTO();
	public static final MetricsDTO OLD_METRICS_DTO = new MetricsDTO();

	private static final Map<String, Object> parameterMap = new ConcurrentHashMap<String, Object>();

	private List<MetricsCollector> mMetricsCollectors;


	private  RabbitTemplate rabbitTemplate;
	
	private String topic;
	
	@Override
	public void run() {
		if (mMetricsCollectors == null || mMetricsCollectors.size() < 1) {
			logger.info("MetricsTimer run metricsCollectors size is 0");
			return;
		}
		logger.info("MetricsTimer run start");
		for (MetricsCollector metricsCollector : mMetricsCollectors) {
			try {
				metricsCollector.doCollect();
			} catch (Throwable e) {
				logger.error("metrics resource error", e);
			}

		}
		parameterMap.clear();
		parameterMap.putIfAbsent("cpu.systemLoadAverage", NumberUtil.nullToZero(S_METRICS_DTO.getSystemLoadAverage()));
		parameterMap.putIfAbsent("memory.maxMemory", NumberUtil.nullToZero(S_METRICS_DTO.getMaxMemory()));
		parameterMap.putIfAbsent("memory.usedMemory", NumberUtil.nullToZero(S_METRICS_DTO.getUsedMemory()));
		parameterMap.putIfAbsent("memory.usedMemoryPercent", NumberUtil.nullToZero(S_METRICS_DTO.getUsedMemoryPercent()));
		parameterMap.putIfAbsent("memory.loadedClassesCount", NumberUtil.nullToZero(S_METRICS_DTO.getLoadedClassesCount()));
		parameterMap.putIfAbsent("memory.scavengeCount", NumberUtil.nullToZero(S_METRICS_DTO.getScavengeCount()));
		parameterMap.putIfAbsent("memory.scavengeTime", NumberUtil.nullToZero(S_METRICS_DTO.getScavengeTime()));
		parameterMap.putIfAbsent("memory.markSweepCount", NumberUtil.nullToZero(S_METRICS_DTO.getMarkSweepCount()));
		parameterMap.putIfAbsent("memory.markSweepTime", NumberUtil.nullToZero(S_METRICS_DTO.getMarkSweepTime()));
		parameterMap.putIfAbsent("memory.maxHeapMemory", NumberUtil.nullToZero(S_METRICS_DTO.getMaxHeapMemory()));
		parameterMap.putIfAbsent("memory.usedHeapMemory", NumberUtil.nullToZero(S_METRICS_DTO.getUsedHeapMemory()));
		parameterMap.putIfAbsent("memory.usedHeapMemoryPercent", NumberUtil.nullToZero(S_METRICS_DTO.getUsedHeapMemoryPercent()));
		parameterMap.putIfAbsent("memory.physicalMemoryPercent", NumberUtil.nullToZero(S_METRICS_DTO.getPhysicalMemoryPercent()));
		parameterMap.putIfAbsent("memory.swapSpacePercent", NumberUtil.nullToZero(S_METRICS_DTO.getSwapSpacePercent()));
		parameterMap.putIfAbsent("tomcat.maxThreads", NumberUtil.nullToZero(S_METRICS_DTO.getMaxThreads()));
		parameterMap.putIfAbsent("tomcat.currentThreadsBusy", NumberUtil.nullToZero(S_METRICS_DTO.getCurrentThreadsBusy()));
		parameterMap.putIfAbsent("tomcat.requestCount", NumberUtil.nullToZero(S_METRICS_DTO.getRequestCount()));
		parameterMap.putIfAbsent("tomcat.errorCount", NumberUtil.nullToZero(S_METRICS_DTO.getErrorCount()));
		parameterMap.putIfAbsent("tomcat.processingTime", NumberUtil.nullToZero(S_METRICS_DTO.getProcessingTime()));
		parameterMap.putIfAbsent("tomcat.maxTime", NumberUtil.nullToZero(S_METRICS_DTO.getMaxTime()));
		parameterMap.putIfAbsent("tomcat.block", NumberUtil.nullToZero(S_METRICS_DTO.getBlock()));
		parameterMap.putIfAbsent("currentTime", System.currentTimeMillis());
		
		logger.info("Metrics parameter is:" + JSON.toJSONString(parameterMap));
		rabbitTemplate.convertAndSend(topic, parameterMap);
	}

	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (mTimer == null) {
			mTimer = new Timer();
			long delay = 0, period = 0;
			final long defaultValue = 1000;
			try {

				if (delay == 0) {
					delay = defaultValue;
				}

				if (period == 0) {
					period = defaultValue;
				}
				mMetricsCollectors = new ArrayList<MetricsCollector>();
				mMetricsCollectors.add(new CpuCollector());
				mMetricsCollectors.add(new MemoryCollector());
				mMetricsCollectors.add(new DataSourceCollector());
				mMetricsCollectors.add(new ServiceExecutionTimeCollector());
				mMetricsCollectors.add(new TomcatCollector());
			} catch (Exception e) {
				logger.error("Start to collect metrics error, ");
				delay = defaultValue;
				period = defaultValue;
			}

			mTimer.schedule(this, delay, period);
			logger.info("Start to collect metrics, period: " + period);
		}
	}

	public void destroy() throws Exception {
		if (mTimer != null) {
			mTimer.cancel();
			logger.info("Stop to collect metrics");
		}
	}

	public Timer getTimer() {
		return mTimer;
	}

	public Timer getmTimer() {
		return mTimer;
	}

	public void setmTimer(Timer aTimer) {
		this.mTimer = aTimer;
	}

	public List<MetricsCollector> getmMetricsCollectors() {
		return mMetricsCollectors;
	}

	public void setmMetricsCollectors(List<MetricsCollector> aMetricsCollectors) {
		this.mMetricsCollectors = aMetricsCollectors;
	}

	public RabbitTemplate getRabbitTemplate() {
		return rabbitTemplate;
	}

	public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	
	
}

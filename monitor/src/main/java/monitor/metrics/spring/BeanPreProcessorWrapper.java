package monitor.metrics.spring;

public interface BeanPreProcessorWrapper {
	
	boolean interest(Object bean);
	
	void decorate(Object bean, String ctxId, String beanName);
	
}

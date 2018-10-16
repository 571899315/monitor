package monitor.metrics.spring;

/**
 * Created by qiw on 16/8/19.
 */
public interface BeanPostProcessorWrapper {

    boolean interest(Object bean);

    Object wrapBean(Object bean, String ctxId, String beanName);

    void remove(String ctxId, String beanName);
}

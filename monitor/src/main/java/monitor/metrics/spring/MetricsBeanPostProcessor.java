package monitor.metrics.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MetricsBeanPostProcessor implements BeanPostProcessor, PriorityOrdered ,DisposableBean {

    private int order = LOWEST_PRECEDENCE;
    
    private static boolean enabled= true;
    
    private String ctxId= "";

    private List<BeanPostProcessorWrapper> beanPostProcessorWrappers = Collections.emptyList();
    
    private List<BeanPreProcessorWrapper> beanPreProcessorWrappers = Collections.emptyList();
    
    private Map<String,BeanPostProcessorWrapper> hash= new HashMap<String,BeanPostProcessorWrapper>();
    
    public void setEnabled(boolean enabled){
    	MetricsBeanPostProcessor.enabled= enabled;
    }
    
    public static boolean isEnabled(){
    	return enabled;
    }
    
    public void setCtxId(String ctxId){
    	this.ctxId= ctxId;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    	if(enabled){
    		 for(BeanPreProcessorWrapper each: beanPreProcessorWrappers){
    			 if(each.interest(bean)){
 	               each.decorate(bean,ctxId,beanName);
 	               break;
 	            }
    		 }
    	}
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    	if(enabled){
	        for(BeanPostProcessorWrapper each: beanPostProcessorWrappers){
	            if(each.interest(bean)){
	                Object obj= each.wrapBean(bean,ctxId,beanName);
	                hash.put(beanName, each);
	                return obj;
	            }
	        }
    	}
        return bean;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setBeanPostProcessorWrappers(List<BeanPostProcessorWrapper> beanPostProcessorWrappers){
        this.beanPostProcessorWrappers = beanPostProcessorWrappers;
    }
    
    public void setBeanPreProcessorWrappers(List<BeanPreProcessorWrapper> beanPreProcessorWrappers){
        this.beanPreProcessorWrappers = beanPreProcessorWrappers;
    }

	public void destroy() throws Exception {
		for(String each: hash.keySet()){
			hash.get(each).remove(ctxId,each);
		}
	}

}

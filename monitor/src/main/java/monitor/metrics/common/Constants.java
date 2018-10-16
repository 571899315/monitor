package monitor.metrics.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public abstract class Constants {
	
	final private static long M_UNIT = 1024*1024;

    final public static String TRACE_ID = "metrics-trace-id";
    final public static String TRACE_PARENT_ID = "metrics-trace-parent-id";
    
    final public static double b2m(long value){
    	if(value<0){
    		return value;
    	}
    	BigDecimal   b   =   new   BigDecimal(value/M_UNIT);  
    	return   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
    final public static Class<?>[] getInterfaces(Class<?> cls){
    	return getInterfaces(cls,new Class<?>[0]);
    }
    
    final public static Class<?>[] getInterfaces(Class<?> cls,Class<?> ...interfaces){
    	Set<Class<?>> idx= new HashSet<Class<?>>();
    	while(cls!=null && cls!=Object.class){
    		if(cls.getInterfaces().length>0){
    			idx.addAll(Arrays.asList(cls.getInterfaces()));
    		}
    		cls = cls.getSuperclass();
    	}
    	if(interfaces!= null && interfaces.length>0){
    		idx.addAll(Arrays.asList(interfaces));
    	}
    	return idx.toArray(new Class<?>[idx.size()]);
    }
}

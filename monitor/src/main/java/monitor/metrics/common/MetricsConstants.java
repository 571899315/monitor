package monitor.metrics.common;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class MetricsConstants {

    private MetricsConstants() { }

    private static final long M_UNIT = 1024 * 1024;

    public static final String TRACE_ID = "metrics-trace-id";
    public static final String TRACE_PARENT_ID = "metrics-trace-parent-id";

    public static double b2m(long value) {
        if (value < 0) {
            return value;
        }
        BigDecimal b   =   new   BigDecimal(value / M_UNIT);
        return   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Class<?>[] getInterfaces(Class<?> cls) {
        return getInterfaces(cls, new Class<?>[0]);
    }

    public static Class<?>[] getInterfaces(Class<?> cls, Class<?> ...interfaces) {
        Set<Class<?>> idx = new HashSet<Class<?>>();
        while (cls != null && cls != Object.class) {
            if (cls.getInterfaces().length > 0) {
                idx.addAll(Arrays.asList(cls.getInterfaces()));
            }
            cls = cls.getSuperclass();
        }
        if (interfaces != null && interfaces.length > 0) {
            idx.addAll(Arrays.asList(interfaces));
        }
        return idx.toArray(new Class<?>[idx.size()]);
    }
}

package monitor.metrics.context;

import org.slf4j.MDC;

public final class MetricsTraceContextHolder {

    private MetricsTraceContextHolder() {
    }

    private static ThreadLocal<MetricsTraceContext> holder = new ThreadLocal<MetricsTraceContext>();

    /**
     * 获取调用链上下文
     *
     * @return
     */
    public static MetricsTraceContext getMetricsTraceContext() {
        return holder.get();
    }

    public static void setMetricsTraceContext(MetricsTraceContext ctx) {
        holder.set(ctx);
    }

    /**
     * 清空调用链上下文
     */
    public static void clear() {
        holder.remove();
        MDC.remove("traceId");
    }
}

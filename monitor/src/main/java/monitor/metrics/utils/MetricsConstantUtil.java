package monitor.metrics.utils;

/**
 * Created by qiwang on 2017/9/6.
 */
public class MetricsConstantUtil {

    protected MetricsConstantUtil() { }
    //metircs 功能开关
    public static final String METRICS_SWITCH_STATUS = "metrics.collector.timer.swich";
    public static final boolean METRICS_SWITCH_STATUS_DEFAULT = false;



    //metircs 定时获取系统信息间隔
    public static final String METRICS_TIMER_DELAY = "metrics.timer.delay";
    public static final Long METRICS_TIMER_DELAY_DEFAULT = 10000l;

    //metircs 定时获取系统信息初始化后延迟多久执行
    public static final String METRICS_TIMER_PERIOD = "metrics.timer.period";
    public static final Long METRICS_TIMER_PERIOD_DEFAULT = 10000l;


}

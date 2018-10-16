package monitor.metrics.utils;

/**
 * Created by yangxingyu on 2017/9/29.
 */
public class MetricReportName {

    // CPU系统平均负载
    public final static String CPU_USER_MEMORY_NAME = "ppdai.loan.usedMemory";

    // maxMemory 虚拟机最大内存
    public final static String JVM_MAX_MEMORY_NAME = "ppdai.loan.maxMemory";

    // usedMemory 已经使用的虚机内存
    public final static String JVM_USED_MEMORY_NAME = "ppdai.loan.maxMemory";

    // loadedClassesCount 加载的类数量
    public final static String JVM_LOADED_CLASSES_COUNT_NAME = "ppdai.loan.loadedClassesCount";

    // collectionCount 垃圾回收次数
    public final static String COLLECTION_COUNT_NAME = "ppdai.loan.collectionCount";

    // collectionTime 垃圾回收持续的时间
    public final static String COLLECTION_TIME_NAME = "ppdai.loan.collectionTime";

}

package monitor.metrics.dto;


public class MetricsDTO {

    // CPU鍗犵敤鐜�
    private Number systemLoadAverage;

    // JVM鏈�澶у唴瀛�
    private Number maxMemory;

    // JVM宸蹭娇鐢ㄥ唴瀛�
    private Number usedMemory;

    // JVM宸蹭娇鐢ㄥ唴瀛樼櫨鍒嗘瘮
    private Number usedMemoryPercent;

    // JVM 鍔犺浇绫绘暟閲�
    private Number loadedClassesCount;

    // scavenge鍨冨溇鍥炴敹娆℃暟
    private Number scavengeCount;

    // scavenge鍨冨溇鍥炴敹鏃堕棿
    private Number scavengeTime;

    // markSweep鍨冨溇鍥炴敹娆℃暟
    private Number markSweepCount;

    // markSweep鍨冨溇鍥炴敹鏃堕棿
    private Number markSweepTime;

    // Tomcat 鏈�澶х嚎绋嬫暟
    private Number maxThreads;

    // Tomcat 姝ｅ湪鎵ц鐨勭嚎绋嬫暟
    private Number currentThreadsBusy;

    // Tomcat 璇锋眰鏁�
    private Number requestCount;

    // Tomcat  閿欒鏁�
    private Number errorCount;

    // Tomcat 澶勭悊鏃堕棿
    private Number processingTime;

    // Tomcat 鏈�澶у鐞嗘椂闂�
    private Number maxTime;

    // Tomcat 绾跨▼姹犳槸鍚﹀凡婊� 1锛氬凡婊★紱 0鏈弧
    private Number block;

    // 鏈�澶у爢鍐呭瓨
    private Number maxHeapMemory;

    //  宸蹭娇鐢ㄥ爢鍐呭瓨
    private Number usedHeapMemory;

    //  宸蹭娇鐢ㄥ爢鍐呭瓨
    private Number usedHeapMemoryPercent;

    // 鐗╃悊鍐呭瓨鐧惧垎姣�
    private Number  physicalMemoryPercent;

    // 鐗╃悊鍐呭瓨鐧惧垎姣�
    private Number  swapSpacePercent;

    public Number getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(Number aSystemLoadAverage) {
        this.systemLoadAverage = aSystemLoadAverage;
    }

    public Number getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Number aMaxMemory) {
        this.maxMemory = aMaxMemory;
    }

    public Number getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Number aUsedMemory) {
        this.usedMemory = aUsedMemory;
    }

    public Number getLoadedClassesCount() {
        return loadedClassesCount;
    }

    public void setLoadedClassesCount(Number aLoadedClassesCount) {
        this.loadedClassesCount = aLoadedClassesCount;
    }

    public Number getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(Number aMaxThreads) {
        this.maxThreads = aMaxThreads;
    }

    public Number getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Number aRequestCount) {
        this.requestCount = aRequestCount;
    }

    public Number getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Number aErrorCount) {
        this.errorCount = aErrorCount;
    }

    public Number getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Number aProcessingTime) {
        this.processingTime = aProcessingTime;
    }

    public Number getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Number aMaxTime) {
        this.maxTime = aMaxTime;
    }

    public Number getBlock() {
        return block;
    }

    public void setBlock(Number aBlock) {
        this.block = aBlock;
    }

    public Number getMaxHeapMemory() {
        return maxHeapMemory;
    }

    public void setMaxHeapMemory(Number aMaxHeapMemory) {
        this.maxHeapMemory = aMaxHeapMemory;
    }

    public Number getUsedHeapMemory() {
        return usedHeapMemory;
    }

    public void setUsedHeapMemory(Number aUsedHeapMemory) {
        this.usedHeapMemory = aUsedHeapMemory;
    }

    public Number getScavengeCount() {
        return scavengeCount;
    }

    public void setScavengeCount(Number aScavengeCount) {
        this.scavengeCount = aScavengeCount;
    }

    public Number getScavengeTime() {
        return scavengeTime;
    }

    public void setScavengeTime(Number aScavengeTime) {
        this.scavengeTime = aScavengeTime;
    }

    public Number getMarkSweepCount() {
        return markSweepCount;
    }

    public void setMarkSweepCount(Number aMarkSweepCount) {
        this.markSweepCount = aMarkSweepCount;
    }

    public Number getMarkSweepTime() {
        return markSweepTime;
    }

    public void setMarkSweepTime(Number aMarkSweepTime) {
        this.markSweepTime = aMarkSweepTime;
    }

    public Number getCurrentThreadsBusy() {
        return currentThreadsBusy;
    }

    public void setCurrentThreadsBusy(Number aCurrentThreadsBusy) {
        this.currentThreadsBusy = aCurrentThreadsBusy;
    }

    public Number getUsedMemoryPercent() {
        return usedMemoryPercent;
    }

    public void setUsedMemoryPercent(Number aUsedMemoryPercent) {
        this.usedMemoryPercent = aUsedMemoryPercent;
    }

    public Number getUsedHeapMemoryPercent() {
        return usedHeapMemoryPercent;
    }

    public void setUsedHeapMemoryPercent(Number aUsedHeapMemoryPercent) {
        this.usedHeapMemoryPercent = aUsedHeapMemoryPercent;
    }

    public Number getPhysicalMemoryPercent() {
        return physicalMemoryPercent;
    }

    public void setPhysicalMemoryPercent(Number aPhysicalMemoryPercent) {
        this.physicalMemoryPercent = aPhysicalMemoryPercent;
    }

    public Number getSwapSpacePercent() {
        return swapSpacePercent;
    }

    public void setSwapSpacePercent(Number aSwapSpacePercent) {
        this.swapSpacePercent = aSwapSpacePercent;
    }
}


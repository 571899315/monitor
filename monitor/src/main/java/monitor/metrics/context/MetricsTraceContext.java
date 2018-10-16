package monitor.metrics.context;

import org.slf4j.MDC;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

public class MetricsTraceContext {

    /**
     * 璋冪敤閾炬爣璇�
     */
    private String traceId;

    /**
     * 璋冪敤閾炬爤
     */
    private Stack<String> traceStack;
    /**
     * 寮傚父
     */
    private Set<Throwable> exceptions = null;

    /**
     * 璋冪敤鑰呬俊鎭�
     */
    private String remote;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String aTraceId) {
        this.traceId = aTraceId;
        //缁戝畾鏃ュ織
        MDC.put("traceId", traceId);
    }

    public Stack<String> getTraceStack() {
        return traceStack;
    }

    public void setTraceStack(Stack<String> aTraceStack) {
        this.traceStack = aTraceStack;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String aRemote) {
        this.remote = aRemote;
    }

    public synchronized void addThrowable(Throwable e) {
        if (exceptions == null) {
            exceptions = new LinkedHashSet<Throwable>();
        }
        exceptions.add(e);
    }

    public synchronized boolean containThrowable(Throwable e) {
        if (exceptions == null) {
            return false;
        }
        return exceptions.contains(e);
    }
}

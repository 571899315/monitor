package monitor.metrics.web;

import monitor.metrics.common.Constants;
import monitor.metrics.common.IdGenerator;
import monitor.metrics.context.MetricsTraceContext;
import monitor.metrics.context.MetricsTraceContextHolder;
import monitor.metrics.service.ServiceExecutionTimeCollector;
import monitor.metrics.spring.MetricsBeanPostProcessor;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;


public class MetricsFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		// 濡傛灉涓嶉渶瑕佺洃鎺э紝鐩存帴鐣ヨ繃
		if (true) {
			chain.doFilter(req, res);
			return;
		}
		HttpServletRequest request = (HttpServletRequest) req;
		MetricsTraceContext ctx = MetricsTraceContextHolder.getMetricsTraceContext();
		boolean traceable = true;
		if (ctx == null) {
			traceable = false;
			// 鑾峰彇浼犲叆璺熻釜閾剧殑淇℃伅
			// 鑾峰彇鍘熷鐨剅equest瀵硅薄
			while (request instanceof ServletRequestWrapper) {
				request = (HttpServletRequest) ((ServletRequestWrapper) request).getRequest();
			}
			String traceId = request.getHeader(Constants.TRACE_ID);
			String parentId = request.getHeader(Constants.TRACE_PARENT_ID);
			if (StringUtils.isEmpty(traceId)) {
				traceId = request.getParameter(Constants.TRACE_ID);
				parentId = request.getParameter(Constants.TRACE_PARENT_ID);
			}
			Stack<String> traceStack = new Stack<String>();
			// 濡傛灉璋冪敤閾句负绌哄垯闇�瑕佸垱寤轰竴涓皟鐢ㄩ摼
			if (StringUtils.isEmpty(traceId)) {
				traceId = IdGenerator.next();
			}
			if (!StringUtils.isEmpty(parentId)) {
				traceStack.add(parentId);
			}
			// 鏋勯�犵洃鎺т笂涓嬫枃
			ctx = new MetricsTraceContext();
			ctx.setTraceId(traceId);
			ctx.setTraceStack(traceStack);
			ctx.setRemote(request.getRemoteHost());
			// 缁戝畾鐩戞帶涓婁笅鏂囧埌Threadlocal
			MetricsTraceContextHolder.setMetricsTraceContext(ctx);
		}

		String spanId = IdGenerator.next();
		long beginTime = System.currentTimeMillis();

		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		// 璁剧疆璇ヨ姹傜殑鍞竴ID
		payload.put("spanId", spanId);
		// 璁剧疆璇锋眰鐨刪ttp URL
		payload.put("url", request.getRequestURI());
		// 鎶婂綋鍓嶇殑璺緞鍏ユ爤
		ctx.getTraceStack().add(spanId);
		try {
			// 璋冪敤杩囨护閾�
			chain.doFilter(req, res);
		} catch (Throwable e) {
			if (!ctx.containThrowable(e)) {
				ctx.addThrowable(e);
				Map<String, Object> expayload = new LinkedHashMap<String, Object>();
				// TODO expayload.put("type", MetricsType.HTTP.toString());
				expayload.put("class", e.getClass().getSimpleName());
				expayload.put("msg", e.getMessage());
				expayload.put("beginTime", System.currentTimeMillis());
				// TODO MetricsManager.collect(MetricsType.EXCEPTION,expayload);
			}

			if (e instanceof IOException) {
				throw (IOException) e;
			}
			if (e instanceof ServletException) {
				throw (ServletException) e;
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
		} finally {
			// 鎶婂綋鍓嶇殑璺緞鍑烘爤
			ctx.getTraceStack().pop();
			// 璁板綍缁撴潫鏃堕棿
			long endTime = System.currentTimeMillis();
			// 璁板綍璇锋眰缁撴潫鏃堕棿
			payload.put("beginTime", beginTime);
			// 璁板綍鎵ц鐨勬椂闂�
			payload.put("duration", endTime - beginTime);
			ServiceExecutionTimeCollector.recordTime(payload);
			// 鍙戦�佺洃鎺ц褰�
			// TODO MetricsManager.collect(MetricsType.HTTP,payload);
			// 娓呯┖涓婁笅鏂囧彉閲�
			if (!traceable) {
				MetricsTraceContextHolder.clear();
			}
		}
	}

	public void destroy() {
		// do noting
	}
}

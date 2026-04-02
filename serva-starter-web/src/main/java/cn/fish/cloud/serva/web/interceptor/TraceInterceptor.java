package cn.fish.cloud.serva.web.interceptor;

import cn.fish.cloud.serva.common.jwt.JWTUtils;
import cn.fish.cloud.serva.common.trace.LogTrace;
import cn.fish.cloud.serva.common.trace.Trace;
import cn.fish.cloud.serva.common.trace.TraceConstants;
import cn.hutool.core.util.IdUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



/**
 * TraceId 拦截器，共享TranceId
 *
 * @author onethefish
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String traceId = request.getHeader(TraceConstants.TRACE_ID);
        String token = request.getHeader(TraceConstants.TOKEN);
        String traceApp = request.getHeader(TraceConstants.TRACE_APP);
        String userCode = null;
        if (StringUtils.isNotEmpty(token)) {
            userCode = JWTUtils.getUsernameFromToken(token);
        }
        if (StringUtils.isEmpty(traceId)) {
            traceId = IdUtil.getSnowflakeNextIdStr();
        }
        if (StringUtils.isEmpty(traceApp)) {
            traceApp = null;
        }
        LogTrace logTrace = new LogTrace();
        logTrace.setUserCode(userCode);
        logTrace.setTraceApp(traceApp);
        logTrace.setTraceId(traceId);
        Trace.LOG_TRACE.set(logTrace);
        return true;
    }
}

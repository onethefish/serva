package cn.fish.cloud.serva.common.trace;

import com.alibaba.ttl.TransmittableThreadLocal;

public class Trace {
    private Trace() {
    }

    public static final TransmittableThreadLocal<LogTrace> LOG_TRACE = new TransmittableThreadLocal<>();
}

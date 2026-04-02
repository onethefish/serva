package cn.fish.cloud.serva.common.trace;

import lombok.Data;

@Data
public class LogTrace {

    private String traceId;         //跟踪id

    private Integer traceStep = 1;  //跟踪步长

    private String traceApp;        //跟踪应用

    private String userCode;        //用户编码


    public Integer getTraceStepWithAdd() {
        return traceStep++;
    }
}

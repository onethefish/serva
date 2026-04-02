package cn.fish.cloud.serva.web.exception;


import java.io.Serial;
import java.text.MessageFormat;


/**
 * REST服务异常
 */
public class RestException extends Exception {

    @Serial
    private static final long serialVersionUID = -4887740166645317800L;

    private String code;

    public RestException(String code, Object... args) {
        super(MessageFormat.format(code, args));
        this.code = code;
    }

    public RestException(String code, Exception e) {
        super(code, e);
        this.code = code;
    }

    public RestException(Exception e) {
        super(e);
    }

    public RestException(Throwable cause) {
        super(cause);
    }
}
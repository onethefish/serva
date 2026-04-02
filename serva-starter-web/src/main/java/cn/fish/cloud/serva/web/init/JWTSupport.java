package cn.fish.cloud.serva.web.init;

import cn.fish.cloud.serva.common.jwt.JWTUtils;
import org.springframework.stereotype.Component;

/**
 * 做成spring的bean在启动时加载，加载时初始化JWTSigner,JWTVerifier。这俩个特别费时间，大概要1秒，会导致第一次登陆时，时间比较长
 */
@Component
public class JWTSupport { // NOSONAR
    static {
        JWTUtils.init();
    }
}

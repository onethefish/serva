package cn.fish.cloud.serva.common.jwt;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JWTUtils {

    private JWTUtils() {
    }

    private static final String SECRET = "!@#$%^12345@$%^&*ertyuERTYUIfghjVBNGH";

    private static final String BODY = "body";

    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private static final JWTVerifier JWT_VERIFIER = JWT.require(algorithm).build();

    // 缓存用户编码和token的关系,减少cpu 压力
    private static final Cache<String, String> userCache =
            Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

    public static void init() {
        //do nothing，仅仅为JWTSupport启动时初始化JWTSingner使用
    }

    /**
     * token签名
     *
     * @param object        签名数据自定义对象
     * @param expirySeconds 过期时间，单位秒
     * @return token字符串
     */
    public static String sign(Map<String, Object> object, int expirySeconds) {
        return JWT.create().withClaim(BODY, object).withExpiresAt(Instant.now().plusSeconds(expirySeconds)).sign(algorithm);
    }

    /**
     * 解析签名
     *
     * @param token token字符串
     * @return 签名数据对象
     */
    public static Map<String, Object> unSign(String token) {
        return JWT_VERIFIER.verify(token).getClaim(BODY).asMap();
    }

    /**
     * 创建 JWT Token
     *
     * @param identity      身份信息（非敏感）
     * @param type          类型
     * @param expirySeconds 过期时间
     * @return token字符串
     */
    public static String createToken(String identity, String type, int expirySeconds) {
        Map<String, Object> map = new HashMap<>();
        //用于说明该JWT是由谁签发的
        map.put(JWTConstants.JWT_ISS, JWTConstants.FISH);
        //自定义性质
        map.put(JWTConstants.JWT_IDENTITY, identity);
        map.put(JWTConstants.TYPE, type);
        Instant now = Instant.now();
        long iatTime = now.toEpochMilli();
        Instant exp = now.plus(expirySeconds, ChronoUnit.SECONDS);
        long expTime = exp.toEpochMilli();
        //数字类型，说明该JWT何时被签发
        map.put(JWTConstants.JWT_IAT, iatTime);
        //说明标明JWT的唯一ID
        map.put(JWTConstants.JWT_JTI, UUID.randomUUID().toString());
        //数字类型，说明该JWT过期的时间
        map.put(JWTConstants.JWT_EXP, expTime);
        return sign(map, expirySeconds);
    }

    /**
     * 解析token，获取用户名
     *
     * @param token token字符串
     * @return 用户编码
     */
    public static String getUsernameFromToken(String token) {
        String usercode = userCache.getIfPresent(token);
        if (null == usercode) {
            try {
                usercode = getValueFromToken(token, JWTConstants.JWT_IDENTITY);
                userCache.put(token, usercode);
                return usercode;
            } catch (Exception e) {
//                log.error("JWT token parse error", e);
                return null;
            }
        }
        return usercode;
    }

    /**
     * 解析token，获取token类型
     *
     * @param token token字符串
     * @return 类型的值
     */
    public static String getTypeFromToken(String token) {
        return getValueFromToken(token, JWTConstants.TYPE);
    }

    /**
     * 解析token，获取自定义参数
     *
     * @param token token字符串
     * @param key   key
     * @return 值
     */
    public static String getValueFromToken(String token, String key) {
        if (StrUtil.isEmpty(key)) {
            throw new IllegalArgumentException("argument key can not be null or empty");
        }
        Map<String, Object> map = unSign(token);
        return MapUtil.getStr(map, key);
    }


    /**
     * 校验jwt token
     *
     * @param token token字符串
     * @return 用户编码
     */
    public static String validJWT(String token) {
        return getUsernameFromToken(token);
    }

}

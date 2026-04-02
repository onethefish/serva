package cn.fish.cloud.serva.common.jwt;

public class JWTConstants {
    private JWTConstants() {
    }

    public static final String FISH = "fish";
    // JWT 相关
    /**
     * Issuer —— 用于说明该JWT是由谁签发的
     */
    public static final String JWT_ISS = "iss";
    /**
     * 自定义性质
     */
    public static final String JWT_IDENTITY = "identity";
    /**
     * 类型
     */
    public static final String TYPE = "type";
    /**
     * Issued At —— 数字类型，说明该JWT何时被签发
     */
    public static final String JWT_IAT = "iat";
    /**
     * JWT ID —— 说明标明JWT的唯一ID
     */
    public static final String JWT_JTI = "jti";
    /**
     * Subject —— 用于说明该JWT面向的对象
     */
    public static final String JWT_SUB = "sub";
    /**
     * Audience —— 用于说明该JWT发送给的用户
     */
    public static final String JWT_AUD = "aud";
    /**
     * Expiration Time —— 数字类型，说明该JWT过期的时间
     */
    public static final String JWT_EXP = "exp";
    /**
     * Not Before —— 数字类型，说明在该时间之前JWT不能被接受与处理
     */
    public static final String JWT_NBF = "nbf";
}

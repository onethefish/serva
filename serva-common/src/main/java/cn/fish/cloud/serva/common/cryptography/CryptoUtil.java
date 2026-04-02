package cn.fish.cloud.serva.common.cryptography;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;

@SuppressWarnings({"java:S3329"})
public class CryptoUtil {

    private static final String GMNAME = "sm2p256v1";
    private static final String BYTE_ARRAY_STRING = "1234567812345678";
    private static final String SECURERANDOM_ALGORITHM = "SHA1PRNG";
    public static final int BLOCK_SIZE = 16;
    /**
     * 密钥 (需要前端和后端保持一致)十六位作为密钥
     */
    private static String KEY;
    /**
     * 密钥偏移量 (需要前端和后端保持一致)十六位作为密钥偏移量
     */
    private static final String IV = "FISH";
    /**
     * 算法
     */
    private static final String ALGORITHMSTR = "AES/ECB/PKCS7Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String CHARSET = "UTF-8";

    private CryptoUtil() {
    }

    static {
        Properties prop = new Properties();
        try (InputStream input = CryptoUtil.class.getClassLoader().getResourceAsStream("security.properties")) {
            if (input != null) {
                prop.load(input);
                KEY = prop.getProperty("KEY");
            }
        } catch (Exception ex) {
        }
    }

    /**
     * AES 加密操作
     *
     * @param plainText 明文
     * @return 密文
     */
    public static String encryptAES(String plainText) throws CryptoException {
        String cipherText = null;
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);
            SymmetricCrypto symmetricCrypto = new SymmetricCrypto(ALGORITHMSTR, key);
            //通过Base64转码返回
            cipherText = symmetricCrypto.encryptBase64(plainText, CHARSET);
        } catch (Exception e) {
            throw new CryptoException("Crypt error", e);
        }
        return cipherText;
    }

    /**
     * AES 解密操作
     *
     * @param cipherText 密文
     * @return 密文
     */
    public static String decryptAES(String cipherText) throws CryptoException {
        String plainText = null;
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), KEY_ALGORITHM);
            SymmetricCrypto symmetricCrypto = new SymmetricCrypto(ALGORITHMSTR, key);
            plainText = symmetricCrypto.decryptStr(cipherText);
        } catch (Exception e) {
            SecureUtil.disableBouncyCastle();
            throw new CryptoException("decrypt error", e);
        }
        return plainText;
    }

    /**
     * <p>生成公私钥对（压缩公钥）
     *
     * @return 密文
     */
    public static KeyPair genKeyPair() throws CryptoException {
        return genKeyPair(true);
    }

    /**
     * <p>生成公私钥对
     *
     * @param isCompressedPublicKey 是否压缩公钥
     * @return 密文
     */
    public static KeyPair genKeyPair(boolean isCompressedPublicKey) throws CryptoException {
        KeyPair keyPair = null;
        try {
            X9ECParameters ecParameters = GMNamedCurves.getByName(GMNAME);
            // 构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());
            // 创建密钥生成器
            ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
            keyPairGenerator.init(new ECKeyGenerationParameters(domainParameters, SecureRandom.getInstance(SECURERANDOM_ALGORITHM)));
            // 生成密钥对
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
            // 提取公钥点
            ECPoint ecPoint = ((ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getQ();
            //公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04
            String publicKey = Hex.toHexString(ecPoint.getEncoded(isCompressedPublicKey));
            BigInteger prikey = ((ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getD();
            String privatekey = prikey.toString(16);
            keyPair = new KeyPair(publicKey, privatekey);
        } catch (Exception e) {
            throw new CryptoException("Generate key pair error", e);
        }
        return keyPair;
    }

    /**
     * <p>国密SM2加密
     *
     * @param publicKey 公钥
     * @param plainText 明文
     * @return 密文
     */
    public static String encryptSM2(String publicKey, String plainText) throws CryptoException {
        String cipherText = null;
        try {
            // 获取一条曲线参数
            X9ECParameters ecParameters = GMNamedCurves.getByName(GMNAME);
            // 构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());

            // 提取公钥点
            ECPoint pukPoint = ecParameters.getCurve().decodePoint(Hex.decode(publicKey));
            // 公钥前面的02、03表示是压缩公钥，04表示未压缩公钥，04的时候，可以去掉前面的04
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

            byte[] plainTextByteteArray = plainText.getBytes();
            byte[] cipherTextByteteArray = sm2Engine.processBlock(plainTextByteteArray, 0, plainTextByteteArray.length);
            cipherText = Hex.toHexString(cipherTextByteteArray);
        } catch (Exception e) {
            throw new CryptoException("Crypt error", e);
        }
        return cipherText;
    }


    /**
     * <p>国密SM2解密
     *
     * @param privateKey 私钥
     * @param cipherText 密文
     * @return 密文
     */
    public static String decryptSM2(String privateKey, String cipherText) throws CryptoException {
        String plainText = null;
        try {
            byte[] cipherDataByte = Hex.decode(cipherText);
            // 获取一条曲线参数
            X9ECParameters ecParameters = GMNamedCurves.getByName(GMNAME);
            // 构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());

            BigInteger privateKeyD = new BigInteger(privateKey, 16);
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

            SM2Engine sm2Engine = new SM2Engine();
            sm2Engine.init(false, privateKeyParameters);

            byte[] plainTextByteArray = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
            plainText = new String(plainTextByteArray);
        } catch (Exception e) {
            throw new CryptoException("decrypt error", e);
        }
        return plainText;
    }

    /**
     * <p>国密SM3加密
     *
     * @param plainText 明文
     * @return 密文
     */
    public static String encryptSM3(String plainText) throws CryptoException {
        String cipherText = null;
        try {
            SM3Digest sm3Digest = new SM3Digest();
            byte[] plainTextByteArray = Hex.decode(plainText);
            sm3Digest.update(plainTextByteArray, 0, plainTextByteArray.length);
            byte[] cipherTextByteArray = new byte[sm3Digest.getDigestSize()];
            sm3Digest.doFinal(cipherTextByteArray, 0);
            cipherText = Hex.toHexString(cipherTextByteArray);
        } catch (Exception e) {
            throw new CryptoException("", e);
        }
        return cipherText;
    }

    /**
     * <p>国密SM3加密
     *
     * @param key       密钥
     * @param plainText 明文
     * @return 密文
     */
    public static String encryptSM3(String key, String plainText) throws CryptoException {
        String cipherText = null;
        try {
            byte[] plainTextByteArray = plainText.getBytes();
            byte[] keyByteArray = Hex.decode(strTo16(key));
            KeyParameter keyParameter = new KeyParameter(keyByteArray);
            SM3Digest digest = new SM3Digest();
            HMac mac = new HMac(digest);
            mac.init(keyParameter);
            mac.update(plainTextByteArray, 0, plainTextByteArray.length);
            byte[] cipherTextByteArray = new byte[mac.getMacSize()];
            mac.doFinal(cipherTextByteArray, 0);
            cipherText = Hex.toHexString(cipherTextByteArray);
        } catch (Exception e) {
            throw new CryptoException("", e);
        }
        return cipherText;
    }

    /**
     * <p>国密SM4 ECB加密
     *
     * @param key       密钥 长度必须为16
     * @param plainText 明文 长度必须大于等于16
     * @return 密文
     */
    public static String encryptSM4ECB(String key, String plainText) throws CryptoException {
        String cipherText = null;
        try {
            byte[] plainTextByteArray = Hex.decode(strTo16(plainText));
            SM4Engine sm4Engine = new SM4Engine();
            sm4Engine.init(true, new KeyParameter(Hex.decode(strTo16(key))));
            int length = plainTextByteArray.length;
            byte[] cipherTextByteArray = new byte[length];
            int times = length / BLOCK_SIZE;
            for (int i = 0; i < times; i++) {
                sm4Engine.processBlock(plainTextByteArray, i * BLOCK_SIZE, cipherTextByteArray, i * BLOCK_SIZE);
            }
            cipherText = Hex.toHexString(cipherTextByteArray);
        } catch (Exception e) {
            throw new CryptoException("", e);
        }
        return cipherText;
    }

    /**
     * <p>国密SM4 ECB解密
     *
     * @param key        密钥
     * @param cipherText 密文
     * @return 密文
     */
    public static String decryptSM4ECB(String key, String cipherText) throws CryptoException {
        String plainText = null;
        try {
            byte[] cipherTextByteArray = Hex.decode(cipherText);
            SM4Engine sm4Engine = new SM4Engine();
            sm4Engine.init(false, new KeyParameter(Hex.decode(strTo16(key))));
            int length = cipherTextByteArray.length;
            byte[] plainTextByteArray = new byte[length];
            int times = length / BLOCK_SIZE;
            for (int i = 0; i < times; i++) {
                sm4Engine.processBlock(cipherTextByteArray, i * BLOCK_SIZE, plainTextByteArray, i * BLOCK_SIZE);
            }
            plainText = hexStringToString(Hex.toHexString(plainTextByteArray));
        } catch (Exception e) {
            throw new CryptoException("", e);
        }
        return plainText;
    }


    /**
     * <p>私钥签名
     *
     * @param privateKey 私钥
     * @param content    待签名内容
     * @return 密文
     */
    public static String sign(String privateKey, String content) throws CryptoException {
        String sign = null;
        try {
            // 待签名内容转为字节数组
            byte[] contentByteArray = Hex.decode(content);
            // 获取一条SM2曲线参数
            X9ECParameters ecParameters = GMNamedCurves.getByName(GMNAME);
            // 构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(ecParameters.getCurve(), ecParameters.getG(), ecParameters.getN());
            BigInteger privateKeyD = new BigInteger(privateKey, 16);
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);
            // 创建签名实例
            SM2Signer sm2Signer = new SM2Signer();
            // 初始化签名实例，带上ID，国密的要求，ID默认值：1234567812345678
            sm2Signer.init(true, new ParametersWithID(new ParametersWithRandom(privateKeyParameters,
                    SecureRandom.getInstance(SECURERANDOM_ALGORITHM)), Strings.toByteArray(BYTE_ARRAY_STRING)));
            sm2Signer.update(contentByteArray, 0, contentByteArray.length);
            // 生成签名
            byte[] signBytes = sm2Signer.generateSignature();
            sign = Hex.toHexString(signBytes);
        } catch (Exception e) {
            throw new CryptoException("Sign error", e);
        }
        return sign;
    }

    /**
     * <p>公钥验签
     *
     * @param publicKey 公钥
     * @param content   待签名内容
     * @param sign      签名
     * @return 密文
     */
    public static boolean verify(String publicKey, String content, String sign) throws CryptoException {
        boolean verify = false;
        try {
            byte[] signByteArray = Hex.decode(sign);
            byte[] contentByteArray = Hex.decode(content);
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName(GMNAME);
            // 构造domain参数
            ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(),
                    sm2ECParameters.getN());
            // 提取公钥点
            ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(publicKey));
            // 公钥前面的02或者03表示是压缩公钥，04表示未压缩公钥, 04的时候，可以去掉前面的04
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);
            // 创建签名实例
            SM2Signer sm2Signer = new SM2Signer();
            // 初始化签名实例，带上ID，国密的要求，ID默认值：1234567812345678
            sm2Signer.init(false, new ParametersWithID(publicKeyParameters, Strings.toByteArray(BYTE_ARRAY_STRING)));
            sm2Signer.update(contentByteArray, 0, contentByteArray.length);
            verify = sm2Signer.verifySignature(signByteArray);
        } catch (Exception e) {
            throw new CryptoException("Verify sign error", e);
        }
        return verify;
    }

    /**
     * <p>字符串转16进制字符串
     *
     * @param string 字符串
     * @return 16进制字符串
     */
    public static String strTo16(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            int ch = (int) string.charAt(i);
            String s4 = Integer.toHexString(ch);
            sb.append(s4);
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转字符串
     *
     * @param hexString 16进制字符串
     * @return 字符串
     */
    public static String hexStringToString(String hexString) {
        String str = "";
        if (StrUtil.isNotBlank(hexString)) {
            byte[] byteArray = new byte[hexString.length() / 2];
            for (int i = 0; i < byteArray.length; i++) {
                byteArray[i] = (byte) (0xff & Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16));
            }
            str = new String(byteArray);
        }
        return str;
    }
}

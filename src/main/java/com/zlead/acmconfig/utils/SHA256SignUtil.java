package com.zlead.acmconfig.utils;

import org.apache.tomcat.util.codec.binary.Base64;
import sun.misc.BASE64Decoder;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shipp
 * @descript RSA256工具类
 * @create 2019-04-18 21:38
 */
public class SHA256SignUtil {

    private static final String ENCODING = "UTF-8";
    //签名算法
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    //加密算法
    public static final String KEY_ALGORITHM = "RSA";
    //获取公钥的key
    public static final String PUBLIC_KEY = "publicKey";
    //获取私钥的key
    public static final String PRIVATE_KEY = "privateKey";
    public static final int KEY_SIZE = 2048;

    public static void main(String[] args) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            SignatureException, UnsupportedEncodingException {

        // 公私钥对
        Map<String, byte[]> keyMap = generateKeyBytes();
        /*Map<String, byte[]> keyMap = new HashMap<>();
        keyMap.put(SHA256SignUtil.PRIVATE_KEY,"siyao".getBytes());
        keyMap.put(SHA256SignUtil.PUBLIC_KEY,"gongyao".getBytes());*/


        PublicKey publicKey = restorePublicKey(keyMap.get(SHA256SignUtil.PUBLIC_KEY));
        PrivateKey privateKey = restorePrivateKey(keyMap.get(SHA256SignUtil.PRIVATE_KEY));
        System.out.println("siyao=" + Base64.encodeBase64String(privateKey.getEncoded()));
        System.out.println("gongyao=" + Base64.encodeBase64String(publicKey.getEncoded()));
        //待签名字符
        String src = "accessKey=LTAIlZlRh7rVrTDA&app_key=qianjinjia&dataId=123&date=20190501&group=DEFAULT_GROUP&namespace=d67c59a6-2915-4c8f-b1c4-bf26b19c84dc&"+"secret="+Base64.encodeBase64String(publicKey.getEncoded())+"&secretKey=SzuDTZXZbGZuYh7h0WBavwtGAwbRhU";
        //签名
        byte[] sign256 = sign256(src, privateKey);
        String sign = encodeBase64(sign256);
        System.out.println("sign=" + sign);
        //验签
        boolean result = verify256(src, sign256, publicKey);
        System.out.println(result);
    }

    /**
     * @Description: 用私钥对信息生成数字签名
     * @Param: privateKey
     * @return:
     * @Author: shipeipei
     * @Date: 2019/4/19
     */
    public static byte[] sign256(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, SignatureException, UnsupportedEncodingException {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes(ENCODING));
        return signature.sign();
    }

    /**
     * @Description: 检验数字签名
     * @Param: data 已加密数据
     * @Param: sign 数字签名
     * @return:
     * @Author: shipeipei
     * @Date: 2019/4/19
     */
    public static boolean verify256(String data, byte[] sign, PublicKey publicKey) {
        if (data == null || sign == null || publicKey == null) {
            return false;
        }
        try {
            Signature signetcheck = Signature.getInstance(SIGNATURE_ALGORITHM);
            signetcheck.initVerify(publicKey);
            signetcheck.update(data.getBytes("UTF-8"));
            return signetcheck.verify(sign);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 二进制数据编码为BASE64字符串
     */
    public static String encodeBase64(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     * BASE64 解码
     */
    public static byte[] decodeBase64(byte[] bytes) {
        byte[] result = null;
        try {
            result = Base64.decodeBase64(bytes);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    /**
     * 生成密钥对 (公钥和私钥)
     */
    public static Map<String, byte[]> generateKeyBytes() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, byte[]> keyMap = new HashMap<String, byte[]>();
            keyMap.put(PUBLIC_KEY, publicKey.getEncoded());
            keyMap.put(PRIVATE_KEY, privateKey.getEncoded());
            return keyMap;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 还原公钥
     */
    public static PublicKey restorePublicKey(byte[] keyBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 还原私钥
     */
    public static PrivateKey restorePrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = factory
                    .generatePrivate(pkcs8EncodedKeySpec);
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description: String转公钥
     * @Param:
     * @return:
     * @Author: shipeipei
     * @Date: 2019/4/19
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * @Description: String转私钥
     * @Param:
     * @return:
     * @Author: shipeipei
     * @Date: 2019/4/19
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
}

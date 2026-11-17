package com.qinglan.example.device_point.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SignUtil {

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        //升序排序
        Map<String, String> sortMap = new TreeMap<>(String::compareTo);
        sortMap.putAll(map);
        return sortMap;
    }

    /**
     * 校验签名
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return 布尔值
     */
    public static boolean checkSignature(String signature,String timestamp,String nonce){
        String token = "ql202320248888";
        String checktext = null;
        if (null != signature) {
            //对ToKen,timestamp,nonce 按字典排序
            String[] paramArr = new String[]{token,timestamp,nonce};
            Arrays.sort(paramArr);
            //将排序后的结果拼成一个字符串
            String content = paramArr[0].concat(paramArr[1]).concat(paramArr[2]);

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                //对接后的字符串进行sha1加密
                byte[] digest = md.digest(content.toString().getBytes());
                checktext = byte2hex(digest);
            } catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        }
        //将加密后的字符串与signature进行对比
        return checktext != null && checktext.equals(signature.toUpperCase());
    }

    public static String sign(Map<String, Object> bodyParams, Map<String, String[]> params, String secretKey, String timestamp) {

        try {
            byte[] sha1Digest = getSHA1Digest(getSignStr(bodyParams, params, secretKey, timestamp));
            return byte2hex(sha1Digest);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 二进制转十六进制字符串
     *
     * @param bytes
     * @return
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    public static byte[] getSHA1Digest(String data) throws IOException {
        byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            bytes = md.digest(data.getBytes("UTF-8"));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse);
        }
        return bytes;
    }

    public static String sign(String str) {

        try {
            byte[] sha1Digest = getSHA1Digest(str);
            return byte2hex(sha1Digest);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSignStr(Map<String, Object> bodyParams, Map<String, String[]> params, String secretKey, String timestamp){
        StringBuilder sb = new StringBuilder();
        if (bodyParams != null && !bodyParams.isEmpty()){
            bodyParams.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(paramEntry -> {
                        sb.append(paramEntry.getKey()).append("=").append(toString(paramEntry.getValue())).append('#');
                    });
        }

        if (!CollectionUtils.isEmpty(params)) {
            params.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(paramEntry -> {
                        String paramValue = String.join(",", Arrays.stream(paramEntry.getValue()).sorted().toArray(String[]::new));
                        sb.append(paramEntry.getKey()).append("=").append(paramValue).append('#');
                    });
        }
        return String.join("#", secretKey, timestamp, sb.toString());
    }

//    private static void
    private static String toString(Object object) {
        if (Objects.isNull(object)) {
            return "null";
        }
        Class<?> type = object.getClass();
        if (type.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Array.getLength(object); ++i) {
                sb.append(toSplice(Array.get(object, i)));
                if (i != (Array.getLength(object) - 1)) {
                    sb.append("=");
                }
            }
            return sb.toString();
        }
        if (BeanUtils.isSimpleProperty(type)) {
            return object.toString();
        }
        if (ClassUtils.isAssignable(Collection.class, type)) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<?> iterator = ((Collection<?>) object).iterator(); iterator.hasNext(); ) {
                sb.append(toSplice(iterator.next()));
                if (iterator.hasNext()) {
                    sb.append("=");
                }
            }
            return sb.toString();
        }
        if (ClassUtils.isAssignable(Map.class, type)) {
            StringBuilder sb = new StringBuilder();
            /*for (Iterator<? extends Map.Entry<String, ?>> iterator = ((Map<String, ?>) object).entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, ?> entry = iterator.next();
                if (Objects.isNull(entry.getValue())) {
                    continue;
                }
                sb.append(entry.getKey()).append("=").append(toSplice(entry.getValue()));
                if (iterator.hasNext()) {
                    sb.append("=");
                }
            }
            return sb.toString();*/

            ((Map<String, ?>) object).entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(paramEntry -> {
                        sb.append(paramEntry.getKey()).append("=").append(toString(paramEntry.getValue())).append("=");
                    });
            if (sb.length() > 0){
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
        return null;
    }

    public static String toSplice(Object object) {
        if (Objects.isNull(object)) {
            return "";
        }
        return toString(object);
    }

    private static void ex(String url) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /**
         * ==========示例参数==========
         * ==========接口什么参数就传什么参数==========
         *
         * uid	String	是	设备uid
         * upperBreathe	number	是	呼吸过高阈值。设置范围0-255. 默认值为24.
         * upperHeartRate	number	是	心率过高阈值。设置范围0-255. 默认值为90.
         * lowerBreathe	number	是	呼吸过低阈值。设置范围0-255. 默认值为8.
         * lowerHeartRate	number	是	心率过低阈值。设置范围0-255. 默认值为50.
         * intensiveCare	number	否	持续测量开关 0是关1是开 默认关 不传采用默认值覆盖
         * suddenDuration	number	否	生命体征弱触发时长 单位分钟 默认10分钟 不传采用默认值覆盖
         *
         * {
         *     "event":[2,3,6,9],
         *     "eventUrl":"http://frp.cwspl.top:9002/ql/test",
         *     "humanUrl":"http://frp.cwspl.top:9002/ql/test"
         * }
         */
        params.put("event", new Integer[]{2,3,6,9});
        params.put("eventUrl", "http://frp.cwspl.top:9002/ql/test");
        params.put("humanUrl", "http://frp.cwspl.top:9002/ql/test");
        long timeMillis = System.currentTimeMillis() / 1000;
        String secret = getSignStr(params, null, "02ad82f4c45489bb4ded926fbe7da184f38270b9",  String.valueOf(timeMillis));
        byte[] signature = getSHA1Digest(secret);
        //创建post请求对象
        HttpPost httppost = new HttpPost(url);
        // 获取到httpclient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            //创建参数集合
            List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
            //添加请求头参数
            httppost.addHeader("appid", "ql17520240708135724cf10");
            httppost.addHeader("signature", byte2hex(signature));
            httppost.addHeader("timestamp", String.valueOf(timeMillis));
            // 设置请求的一些配置设置，主要设置请求超时，连接超时等参数
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(2000).setConnectionRequestTimeout(2000).setSocketTimeout(2000)
                    .build();
            httppost.setConfig(requestConfig);
            /**
             生产Cookie
             **/
//            httppost.setHeader("Cookie","xxxxxx");
            //添加参数
            httppost.setEntity(new StringEntity(JSONObject.toJSONString(params), ContentType.create("application/json", StandardCharsets.UTF_8)));

            // 请求结果
            String resultString = "";
            //启动执行请求，并获得返回值
            CloseableHttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 获取请求响应结果
                org.apache.http.HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 将响应内容转换为指定编码的字符串
                    resultString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    System.out.println(resultString);
                }
            } else {
                throw new ConnectTimeoutException("PATEST请求连接失败");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            httpclient.close();
        }
    }

}

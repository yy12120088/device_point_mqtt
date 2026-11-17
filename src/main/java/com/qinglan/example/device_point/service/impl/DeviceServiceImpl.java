package com.qinglan.example.device_point.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.qinglan.example.device_point.domain.DeviceMqttConfig;
import com.qinglan.example.device_point.domain.MqttDeviceAuthRequest;
import com.qinglan.example.device_point.domain.MqttDeviceAuthResponse;
import com.qinglan.example.device_point.domain.RadarVersionConfig;
import com.qinglan.example.device_point.mqtt.domain.AjaxResult;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;
import com.qinglan.example.device_point.service.DeviceService;
import com.qinglan.example.device_point.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {
    @Value("${spring.mqtt.radar.server}")
    private String server;

    @Value("${spring.mqtt.radar.port}")
    private int port;

    @Value("${spring.mqtt.radar.keepalive}")
    private int keepAlive;

    @Value("${spring.mqtt.radar.timeout}")
    private int timeout;

    @Value("${qinglan.secret}")
    private String qlSecret;

    @Value("${qinglan.appid}")
    private String qlAppid;

    @Value("${qinglan.url}")
    private String qlUrl;


    @Override
    public AjaxResult authenticateDevice(MqttDeviceAuthRequest request) {
        //todo 判断是否合法设备 + 鉴权


        //
        return AjaxResult.success(buildAuthResponse(request));
    }

    @Override
    public StartVoiceParam getRadarVoIpLicense(String uid) {
        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);

        long currentTimeMillis = System.currentTimeMillis() / 1000;
        String signature = SignUtil.sign(params, null, qlSecret, String.valueOf(currentTimeMillis));

        String url = qlUrl + "/prod-api/thirdparty/v2/getVoIPToken";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

            // 添加请求头
            httppost.addHeader("appid", qlAppid);
            httppost.addHeader("signature", signature);
            httppost.addHeader("timestamp", String.valueOf(currentTimeMillis));

            // 设置请求超时配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(5000)
                    .build();
            httppost.setConfig(requestConfig);

            // 设置请求体
            httppost.setEntity(new StringEntity(JSONObject.toJSONString(params),
                    ContentType.create("application/json", StandardCharsets.UTF_8)));

            // 执行请求
            CloseableHttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                org.apache.http.HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String resultString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    log.info("getRadarVoIpLicense response: {}", resultString);

                    JSONObject jsonResult = JSONObject.parseObject(resultString);
                    if (jsonResult != null && jsonResult.containsKey("data")) {
                        JSONObject data = jsonResult.getJSONObject("data");

                        StartVoiceParam voiceParam = new StartVoiceParam();
                        voiceParam.setUid(uid);
                        voiceParam.setToken(data.getString("token"));
                        voiceParam.setLicense(data.getString("license"));
                        voiceParam.setLicenseKey(data.getString("licenseKey"));
                        voiceParam.setChannel(data.getString("channelName"));
                        voiceParam.setAppId(data.getString("appId"));

                        return voiceParam;
                    } else {
                        log.error("getRadarVoIpLicense failed: response data is null or missing");
                    }
                }
            } else {
                log.error("getRadarVoIpLicense failed: HTTP status code {}", response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("getRadarVoIpLicense exception: {}", e.getMessage(), e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Failed to close httpclient: {}", e.getMessage());
            }
        }

        return null;
    }

    @Override
    public boolean releaseRadarVoIpLicense(String uid, String licenseKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("lisenseKey", licenseKey);

        long currentTimeMillis = System.currentTimeMillis() / 1000;
        String signature = SignUtil.sign(params, null, qlSecret, String.valueOf(currentTimeMillis));

        String url = qlUrl + "/prod-api/thirdparty/v2/releaseLisense";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

            // 添加请求头
            httppost.addHeader("appid", qlAppid);
            httppost.addHeader("signature", signature);
            httppost.addHeader("timestamp", String.valueOf(currentTimeMillis));

            // 设置请求超时配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(5000)
                    .build();
            httppost.setConfig(requestConfig);

            // 设置请求体
            httppost.setEntity(new StringEntity(JSONObject.toJSONString(params),
                    ContentType.create("application/json", StandardCharsets.UTF_8)));

            // 执行请求
            CloseableHttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                org.apache.http.HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String resultString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    log.info("releaseRadarVoIpLicense response: {}", resultString);

                    JSONObject jsonResult = JSONObject.parseObject(resultString);
                    if (jsonResult != null) {
                        int code = jsonResult.getIntValue("code", -1);
                        if (code == 200) {
                            return true;
                        } else {
                            log.error("releaseRadarVoIpLicense failed: code={}, msg={}", code, jsonResult.getString("msg"));
                        }
                    }
                }
            } else {
                log.error("releaseRadarVoIpLicense failed: HTTP status code {}", response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            log.error("releaseRadarVoIpLicense exception: {}", e.getMessage(), e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("Failed to close httpclient: {}", e.getMessage());
            }
        }

        return false;
    }

    /**
     * 构建雷达配置
     */
    private RadarVersionConfig buildRadarConfig() {
        RadarVersionConfig radar = new RadarVersionConfig();
        radar.setCap(15);
        return radar;
    }

    private MqttDeviceAuthResponse buildAuthResponse(MqttDeviceAuthRequest request) {

        MqttDeviceAuthResponse response = new MqttDeviceAuthResponse();
        response.setUid(request.getUid());
        response.setMqtt(buildMqttConfig(request, "88"));

        // 只有设备类型需要设置雷达配置
        response.setRadar(buildRadarConfig());

        return response;
    }

    /**
     * 构建MQTT配置
     */
    private DeviceMqttConfig buildMqttConfig(MqttDeviceAuthRequest request, String deviceType) {

        DeviceMqttConfig mqtt = new DeviceMqttConfig();
        mqtt.setClientId(deviceType + "-" + request.getUid());
        mqtt.setServer(server);
        mqtt.setPort(port);
        mqtt.setKeepalive(keepAlive);
        mqtt.setTimeout(timeout);
        mqtt.setAccount(request.getUid());
        mqtt.setPwd("666");
        mqtt.setProtocol("1");
        mqtt.setPrefix("");
        mqtt.setProductId(deviceType);

        return mqtt;
    }
}

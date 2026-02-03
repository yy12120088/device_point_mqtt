package com.qinglan.example.device_point.mqtt.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qinglan.example.device_point.mqtt.domain.MqttMsg;
import com.qinglan.example.device_point.mqtt.domain.OTADto;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;
import com.qinglan.example.device_point.mqtt.enums.QosEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MqttRadarMessageCore implements MqttRadarMessage{

    @Resource
    @Lazy
    private EmqxService emqxService;

    @Value("${spring.mqtt.server.apiKey:ee26b0dd4af7e749}")
    private String mqttApiKey;

    @Value("${spring.mqtt.server.apiSecret:HOJbWSbY9Csregd9BPwZ3jMYosuDjrCvHPxB2QdN9AjZNL}")
    private String mqttApiSecret;

    @Value("${spring.mqtt.server.apiUrl:http://localhost:18083/api/v5}")
    private String mqttApiUrl;

    private final String eqtType = "4";
    String pFuncTopic = "/func/";
    String pPropTopic = "/prop/";
    String pMonitorTopic = "/monitor/";
    String pNtpTopic = "/ntp/";

    @Override
    public boolean removeDevices(List<String> uids) {
        return true;
    }

    @Override
    public String OTA(OTADto otaDto) {
        String flag = "200";
        String uid = otaDto.getUid();
        String topic = pFuncTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("ota");
        JSONObject data = (JSONObject) JSON.toJSON(otaDto);
        mqttMsg.setData(data);
        try {
            emqxService.publishMsg(topic, mqttMsg, QosEnum.QoS0, uid, false);
            log.info("mqtt消息发送 -------------OTA升级发送-----------------");
        } catch (Exception e) {
            log.error("OTA调用失败 -------------OTA--{}-----------------", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean setProperty(String uid, String key, String value) {
        boolean flag = false;
        String topic = pPropTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("update");
        JSONObject data = new JSONObject();
        data.put(key, value);
        mqttMsg.setData(data);
        try {
            JSONObject jsonObject = emqxService.syncPublish(uid, topic, mqttMsg, QosEnum.QoS0, false, 5);
            if (200 == (jsonObject.getIntValue("code"))){
                flag = true;
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ----------60G-mqtt----setProperty--{}-----------------", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean batchSetProperty(String uid, Map<String, Object> properties){
        boolean flag = false;
        String topic = pPropTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("update");
        mqttMsg.setData(properties);
        try {
            JSONObject jsonObject = emqxService.syncPublish(uid, topic, mqttMsg, QosEnum.QoS0, false, 6);
            if (200 == (jsonObject.getIntValue("code"))){
                flag = true;
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ----------60G-mqtt----batchSetProperty--{}-----------------", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean getPositionBreathData(String uid, short seconds) {
        getMonitorDatas(uid, seconds, "0", 1);
        return true;
    }

    private void getMonitorDatas(String uid, short seconds, String content, int interval) {
        String topic = pMonitorTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("subscription");
        JSONObject data = new JSONObject();
        mqttMsg.setData(data);
        data.put("content", content);
        data.put("duration", seconds);
        data.put("interval", interval);
        try {
            emqxService.publishMsg(topic, mqttMsg, QosEnum.QoS0, false);
        } catch (Exception e) {
            log.error("rpc调用失败 ---------60G-mqtt-----getPositionBreathData--{}-----------------", e.getMessage());
        }
    }

    @Override
    public Map<String, String> getProperties(String uid, List<String> keys) {
        Map<String, String> props = new HashMap<>();
        String topic = pPropTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("read");
        JSONObject data = new JSONObject();
        if (StringUtils.isEmpty(keys)){
            data.put("key", new byte[]{});
        }
        mqttMsg.setData(data);
        try {
            JSONObject jsonObject = emqxService.syncPublish(uid, topic, mqttMsg, QosEnum.QoS0, false, 8);
            if (200 == (jsonObject.getIntValue("code"))){
                return jsonObject.getObject("data", Map.class);
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ---------60G-mqtt----getProperties--{}-----------------", e.getMessage());
        }
        return props;
    }

    @Override
    public boolean startVoice(StartVoiceParam startVoiceParam) {
        boolean flag = false;
        String topic = pFuncTopic + eqtType + "/" + startVoiceParam.getUid() + "/get";
        MqttMsg mqttMsg = new MqttMsg("call_start");
        mqttMsg.setData(startVoiceParam);
        try {
            JSONObject jsonObject = emqxService.syncPublish(startVoiceParam.getUid(), topic, mqttMsg, QosEnum.QoS0, false, 10);
            if (200 == (jsonObject.getIntValue("code"))){
                flag = true;
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ---------60G-mqtt----startVoice--{}-----------------", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean stopVoice(String uid) {
        boolean flag = false;
        String topic = pFuncTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("call_stop");
        try {
            JSONObject jsonObject = emqxService.syncPublish(uid, topic, mqttMsg, QosEnum.QoS0, false, 5);
            if (200 == (jsonObject.getIntValue("code"))){
                flag = true;
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ---------60G-mqtt----stopVoice--{}-----------------", e.getMessage());
        }
        return flag;
    }

    @Override
    public boolean restartDevice(String uid, byte type) {
        boolean flag = false;
        String topic = pFuncTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg("control");
        JSONObject data = new JSONObject();
        data.put("dev", type);
        mqttMsg.setData(data);
        try {
            JSONObject jsonObject = emqxService.syncPublish(uid, topic, mqttMsg, QosEnum.QoS0, false, 8);
            if (200 == (jsonObject.getIntValue("code"))){
                flag = true;
            }
        } catch (Exception e) {
            log.error("rpc调用失败 ----------60G-mqtt----restartDevice--{}-----------------", e.getMessage());
        }
        return flag;
    }

    public boolean alarm(String uid, String cmd, String alarmType) {
        String topic = pFuncTopic + eqtType + "/" + uid + "/get";
        MqttMsg mqttMsg = new MqttMsg(cmd);
        JSONObject data = new JSONObject();
        data.put("type", alarmType);
        mqttMsg.setData(data);
        try {
            emqxService.publishMsg(topic, mqttMsg, QosEnum.QoS0, uid, false);
            log.info("mqtt消息发送 -------------alarm发送-----------------");
        } catch (Exception e) {
            log.error("rpc调用失败 -------------4G版本-alarm--{}-----------------", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean accChat(String uid, Integer type, Integer alert) {
        return false;
    }

}

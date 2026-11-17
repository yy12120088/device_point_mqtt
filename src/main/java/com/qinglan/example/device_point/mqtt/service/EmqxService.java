package com.qinglan.example.device_point.mqtt.service;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qinglan.example.device_point.mqtt.EmqxClient;
import com.qinglan.example.device_point.mqtt.domain.AjaxResult;
import com.qinglan.example.device_point.mqtt.domain.MqttMsg;
import com.qinglan.example.device_point.mqtt.enums.QosEnum;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.*;


@Service
public class EmqxService {
    private static final Logger logger = LoggerFactory.getLogger(EmqxService.class);

    private EmqxClient mqttClient;

    @Autowired
    public void setMqttClient(EmqxClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    private final Random random = new Random();
    /**
     * 订阅的主题
     */
    String sInfoTopic =  "/info/+/+/post";
    String sNtpTopic =  "/ntp/+/+/post";
    String sPropertyTopic =  "/prop/+/+/post";
    String sFunctionTopic =  "/func/+/+/post";
    String sEventTopic =  "/event/+/+/post";
    String sAlarmTopic =  "/alarm/+/+/post";
    String sLogTopic =  "/log/+/+/post";
    String sStatTopic = "/stat/+/+/post";
    String sMonitorTopic =  "/monitor/+/+/post";

    /**
     * 发布的主题
     */
    String pStatusTopic = "/prop/";
    String pInfoTopic = "/info/";
    String pNtpTopic = "/ntp/";
    String pFunctionTopic = "/func/";

    public void subscribe(MqttAsyncClient client) throws MqttException {
        // 订阅设备信息
        client.subscribe(sInfoTopic, 0);//   /+/+/info/post
        // 订阅时钟同步
        client.subscribe(sNtpTopic, 0);//    /+/+/ntp/post
        // 订阅设备属性
        client.subscribe(sPropertyTopic, 0);//    /+/+/property/post
        // 订阅设备功能
        client.subscribe(sFunctionTopic, 0);//    /+/+/function/post
        // 订阅设备日志
        client.subscribe(sLogTopic, 0);//    /+/+/log/post
        // 订阅设备事件\日志
        client.subscribe(sEventTopic, 0);//     /+/+/event/post
        // 设备告警
        client.subscribe(sAlarmTopic, 0);//     /+/+/alarm/post
        // 订阅设备统计数据
        client.subscribe(sStatTopic,0);//       /+/+/stat/post
        // 订阅设备透传数据
        client.subscribe(sMonitorTopic,0);//    /+/+/monitor/post
    }

    /**
     * 消息回调方法
     *
     * @param topic       主题
     * @param mqttMessage 消息体
     */
    @Async("mqttTask")
    public void subscribeCallback(String topic, MqttMessage mqttMessage) throws InterruptedException {
//        System.err.println(Thread.currentThread().getName());
//        Thread.sleep(5000);
        logger.error("收到参数 - topic: {}, message: {}", topic, mqttMessage);
        if (mqttMessage == null) {
            logger.warn("收到空参数 - topic: {}, message: {}", topic, mqttMessage);
            return;
        }
        String msg = new String(mqttMessage.getPayload());
        JSONObject message = JSONObject.parseObject(msg);
        String[] topicItem = topic.substring(1).split("/");
        String name = topicItem[0];
        String eqtTypeId = topicItem[1];
        String deviceNum = topicItem[2];
        switch (name) {
            case "monitor":
                reportMonitor(message, deviceNum);
                break;
            case "prop":
                syncMsg(message, message.getString("requestId"));
                break;
            case "func":
                reportFunction(eqtTypeId, deviceNum, message);
                break;
            case "event":
                reportEvent(eqtTypeId, deviceNum, message);
                break;
            case "alarm":
                reportAlarm(eqtTypeId, deviceNum, message);
                break;
            case "stat":
                //统计数据
                reportStat(deviceNum, eqtTypeId, message);
                break;
            case "ntp":
                // 时间同步
                reportNtp(eqtTypeId, deviceNum);
                break;
            default:
                break;
        }
    }

    private void reportNtp(String eqtTypeId, String deviceNum) {
        String topic = pNtpTopic + eqtTypeId + "/" + deviceNum + "/get";
        MqttMsg mqttMsg = new MqttMsg("fetch_return");
        JSONObject data = new JSONObject();
        mqttMsg.setData(data);
        long ts = LocalDateTime.now()
                .atZone(ZoneOffset.UTC)   // 故意把本地时间放进 UTC 时区
                .toInstant()
                .getEpochSecond();
        data.put("srvTs", ts);
        Long timezone = getDeviceTimezone(deviceNum);
        if (timezone == null) {
            return;
        }
        data.put("devTs", ts + timezone);
        publishMsg(topic, mqttMsg, QosEnum.QoS0, false);
    }

    private void reportAlarm(String eqtTypeId, String deviceNum, JSONObject message) {
    }

    private void reportEvent(String eqtTypeId, String deviceNum, JSONObject message) {
        String cmd = message.getString("cmd");
        switch (cmd) {
            case "event":
                String type = message.getString("type");
                break;
            case "log":
                JSONObject log = message.getJSONObject("data");
                break;
            default:
                break;
        }
    }

    private void reportStat(String deviceNum, String eqtTypeId, JSONObject msg) {
    }

    private void reportMonitor(JSONObject msg, String uid) {
        JSONObject data = msg.getJSONObject("data");
        String track = data.getString("track");
        String bh = data.getString("bh");
    }


    /**
     * 上报功能
     *
     * @param message
     */
    private void reportFunction(String eqtTypeId, String deviceNum, JSONObject message) {
        String cmd = message.getString("cmd");
        String requestId = message.getString("requestId");
        switch (cmd) {
            case "ota_return": {
                // OTA进度上报
                JSONObject data = (JSONObject) message.get("data");
                String uid = data.getString("uid");
                int progress = data.getIntValue("progress");
                String errMsg = data.getString("errMsg");
                logger.info("uid:{}, ota---------{}, msg:{}", uid, progress, errMsg);
                break;
            }
            case "control_return":
                syncMsg(message, requestId);
                break;
            case "call_stop_return":
                syncMsg(message, requestId);
                break;
            case "call_start_return":
                syncMsg(message, requestId);
                break;
            case "timer_set_return":
                syncMsg(message, requestId);
                break;
            case "timer_mod_return":
                syncMsg(message, requestId);
                break;
            case "timer_del_return":
                syncMsg(message, requestId);
                break;
            case "timer_read_return":
                syncMsg(message, requestId);
                break;
            case "get_alarm_token":
            default:
                break;
        }
    }

    /**
     * 获取设备时间戳
     * @param deviceNum
     * @return
     */
    private Long getDeviceTimezone(String deviceNum) {
        Long timezone = 0L;
        return timezone;
    }

    public void publishMsg(String topic, MqttMsg msg, QosEnum qos, boolean retain) {
        mqttClient.publish(qos.value(), false, topic, JSON.toJSONString(msg));
    }

    public void publishMsg(String topic, MqttMsg msg, QosEnum qos, String uid, boolean retain) {
        msg.setRequestId(generateRequestId(uid));
        mqttClient.publish(qos.value(), false, topic, JSON.toJSONString(msg));
    }

    private final ConcurrentHashMap<String, CompletableFuture<JSONObject>> responseFutures = new ConcurrentHashMap<>();
    public JSONObject syncPublish(String uid, String topic, MqttMsg msg, QosEnum qos, boolean retain, int timeOut) {
        String requestId = generateRequestId(uid); // 生成唯一请求 ID
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        responseFutures.put(requestId, future);
        msg.setRequestId(requestId);
        mqttClient.publish(qos.value(), false, topic, JSON.toJSONString(msg));
        // 设置超时为 5 秒
        try {
            // 等待 5 秒后检查是否完成
            JSONObject result = future.get(timeOut, TimeUnit.SECONDS);
            // 成功处理响应
            return result;
        } catch (ExecutionException e) {
            // 处理异常
            responseFutures.remove(requestId);
            future.completeExceptionally(e.getCause());
        } catch (Exception e) {
            // 处理超时
            responseFutures.remove(requestId);
            if (e instanceof TimeoutException) {
                logger.error("mqtt-----------请求超时: {}", requestId);
            }
        }
        return new JSONObject(AjaxResult.error());
    }

    public void syncMsg(JSONObject msg, String reqId) {
        if (StringUtils.isEmpty(reqId)){
            return;
        }
        logger.error("syncMsg-------------reqId:{}", reqId);
        CompletableFuture<JSONObject> future = responseFutures.remove(reqId);
        if (future != null) {
            future.complete(msg);
        }
    }

    private String generateRequestId(String uid) {
        return uid.substring(uid.length()-4)  +  String.format("%.10f", random.nextDouble());
    }
}

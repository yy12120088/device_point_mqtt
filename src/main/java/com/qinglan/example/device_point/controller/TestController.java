package com.qinglan.example.device_point.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qinglan.example.device_point.mqtt.domain.AjaxResult;
import com.qinglan.example.device_point.mqtt.domain.OTADto;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;
import com.qinglan.example.device_point.mqtt.service.MqttRadarMessage;
import com.qinglan.example.device_point.server.msg.DeviceInfo;
import com.qinglan.example.device_point.server.msg.ServerLBSInfo;
import com.qinglan.example.device_point.server.session.DeviceRegSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    DeviceRegSession deviceRegSession;

    @Resource
    MqttRadarMessage mqttRadarMessage;

    /**
     * 订阅设备数据
     * @param uid
     * @return  Subscription device data
     * @throws InterruptedException
     */
    @GetMapping("/test")
    public Object test(@RequestParam("uid") String uid) throws InterruptedException {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        return mqttRadarMessage.getPositionBreathData(uid, (short) 30);
    }

    /**
     * 获取设备属性
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @GetMapping("/get/prop")
    public AjaxResult getProp(@RequestParam("uid") String uid) throws InterruptedException {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        Map<String, String> properties = mqttRadarMessage.getProperties(uid, null);
        return AjaxResult.success(properties);
    }

    /**
     * 设置设备属性
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @GetMapping("/set/prop")
    public AjaxResult getProp(@RequestParam("uid") String uid, @RequestParam("key") String key, @RequestParam("value") String value) throws InterruptedException {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        return AjaxResult.success(mqttRadarMessage.setProperty(uid, key, value));
    }

    /**
     * 开启通话
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @PostMapping("/startVoice")
    public AjaxResult startHdVoice(@RequestParam("uid") String uid) {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        //todo 远程调用清澜服务器获取license token appid

        StartVoiceParam startVoiceParam = new StartVoiceParam();
        startVoiceParam.setUid(uid);
        mqttRadarMessage.startVoice(startVoiceParam);
        return AjaxResult.success();
    }

    /**
     * ota
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @PostMapping("/ota")
    public AjaxResult startOta(@RequestParam("uid") String uid) {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        //todo 文件下载地址 需要有文件下载的接口能力 http/https

        OTADto otaDto = new OTADto()
                .setUid(uid)
                .setEspver("")
                .setEspfilesha256("")
                .setEspfilesize(1)
                .setEspfileUrl("")
                .setRadarver("")
                .setRadarfilesha256("")
                .setRadarfilesize(1)
                .setRadarfileUrl("");
        String ota = mqttRadarMessage.OTA(otaDto);
        return AjaxResult.success(ota);
    }


    /**
     * 设备上线通知
     * @param jsonObject
     */
    @PostMapping("/auth/connect")
    public void mqttClientConnect(@RequestBody JSONObject jsonObject) {
        //{"disconnected_at":1749799757707,"disconn_props":{"User-Property":{}},"proto_name":"MQTT","client_attrs":{},"proto_ver":4,"clientid":"ql_AD8A613B1F3F","username":"AD8A613B1F3F","event":"client.disconnected","metadata":{"rule_id":"webhook_WH_D"},
        // "sockname":"172.17.0.2:1883","peername":"192.168.1.172:63684","timestamp":1749799757707,"reason":"discarded","node":"emqx@172.17.0.2"}
        //{"receive_maximum":32,"conn_props":{"User-Property":{}},"expiry_interval":0,"clean_start":true,"mountpoint":"undefined","is_bridge":false,"connected_at":1749799757707,"proto_name":"MQTT","client_attrs":{},"proto_ver":4,
        // "clientid":"ql_AD8A613B1F3F","username":"AD8A613B1F3F","event":"client.connected","metadata":{"rule_id":"webhook_WH_D"},"keepalive":60,"sockname":"172.17.0.2:1883","peername":"192.168.1.172:62028","timestamp":1749799757707,"node":"emqx@172.17.0.2"}
        String clientid = jsonObject.getString("clientid");
        String event = jsonObject.getString("event");
        log.error("mqtt--------c:{}------e-{}", clientid, event);
        String peername = jsonObject.getString("peername");
        if (clientid.startsWith("radar")) {
            if ("client.connected".equals(event)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                deviceRegSession.onLine(clientid.split("_")[1], peername.split(":")[0]);
            } else if ("client.disconnected".equals(event)) {
                deviceRegSession.offLine(clientid.split("_")[1]);
            }
        }
    }
}

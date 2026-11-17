package com.qinglan.example.device_point.controller;

import com.alibaba.fastjson2.JSONObject;
import com.qinglan.example.device_point.domain.MqttDeviceAuthRequest;
import com.qinglan.example.device_point.domain.RadarProp;
import com.qinglan.example.device_point.mqtt.domain.AjaxResult;
import com.qinglan.example.device_point.mqtt.domain.OTADto;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;
import com.qinglan.example.device_point.mqtt.service.MqttRadarMessage;
import com.qinglan.example.device_point.mqtt.session.DeviceRegSession;
import com.qinglan.example.device_point.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/prod-api")
public class DeviceController {

    @Resource
    DeviceRegSession deviceRegSession;

    @Resource
    MqttRadarMessage mqttRadarMessage;

    @Autowired
    private DeviceService deviceService;

    /**
     * 订阅设备实时数据
     * @param uid
     * @return  Subscription device data
     * @throws InterruptedException
     */
    @GetMapping("/sub/data")
    public AjaxResult test(@RequestParam("uid") String uid) throws InterruptedException {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        return AjaxResult.success(mqttRadarMessage.getPositionBreathData(uid, (short) 60));
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
     */
    @PostMapping("/set/prop")
    public AjaxResult getProp(@RequestBody RadarProp radarProp) throws InterruptedException {
        if (deviceRegSession.isReg(radarProp.getUid())){
            return null;
        }
        return AjaxResult.success(mqttRadarMessage.batchSetProperty(radarProp.getUid(), radarProp.getProperties()));
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
        //远程调用清澜服务器获取license token appid

        StartVoiceParam startVoiceParam = deviceService.getRadarVoIpLicense(uid);
        if (startVoiceParam == null){
            return AjaxResult.error("获取license失败");
        }
        if (mqttRadarMessage.startVoice(startVoiceParam)) {
            return AjaxResult.success(startVoiceParam);
        }
        return AjaxResult.error();
    }

    /**
     * 关闭通话
     * @param uid
     * @return Device prop
     * @throws InterruptedException
     */
    @PostMapping("/stopVoice")
    public AjaxResult stopHdVoice(@RequestParam("uid") String uid, @RequestParam("licenseKey") String licenseKey) {
        if (deviceRegSession.isReg(uid)){
            return null;
        }
        //远程调用清澜服务器释放license
        //https://test.qinglanst.com/prod-api/thirdparty/v2/ReleaseVoIPToken
        boolean flag = deviceService.releaseRadarVoIpLicense(uid, licenseKey);
        if (!flag){
            return AjaxResult.error("释放license失败");
        }
        if (mqttRadarMessage.stopVoice(uid)) {
            return AjaxResult.success();
        }
        return AjaxResult.error("设备退出通话失败");
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

    @PostMapping("/thirdmqtt/v2/auth/device")
    public AjaxResult authenticateDevice(
            @RequestBody MqttDeviceAuthRequest request) {
        // 1. 验证请求参数
        if (request.getUid() == null || request.getType() == null || request.getAuth() == null) {
            return AjaxResult.error("params is not null");
        }
        try {
            return deviceService.authenticateDevice(request);
        } catch (Exception e) {
            log.error("authenticateDevice-------------------erro:{}", e.getMessage());
            return AjaxResult.error(e.getMessage());
        }
    }
}

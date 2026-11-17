package com.qinglan.example.device_point.service;

import com.qinglan.example.device_point.domain.MqttDeviceAuthRequest;
import com.qinglan.example.device_point.mqtt.domain.AjaxResult;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;
import org.springframework.stereotype.Service;

@Service
public interface DeviceService {
    AjaxResult authenticateDevice(MqttDeviceAuthRequest request);

    StartVoiceParam getRadarVoIpLicense(String uid);

    boolean releaseRadarVoIpLicense(String uid, String licenseKey);
}

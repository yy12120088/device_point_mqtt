package com.qinglan.example.device_point.domain;

import lombok.Data;

@Data
public class MqttDeviceAuthResponse {
    private String uid;

    private DeviceMqttConfig mqtt;

    private RadarVersionConfig radar;

}


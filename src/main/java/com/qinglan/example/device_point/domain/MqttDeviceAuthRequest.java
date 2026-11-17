package com.qinglan.example.device_point.domain;

import lombok.Data;

@Data
public class MqttDeviceAuthRequest {
    private String uid;
    private Integer type;
    private String auth;
    private String salt;

    private McuConfig mcu;

    private RadarVersionConfig radar;

    private SrConfig sr;
}


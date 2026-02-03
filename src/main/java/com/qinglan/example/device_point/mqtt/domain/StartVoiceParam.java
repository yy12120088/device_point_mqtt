package com.qinglan.example.device_point.mqtt.domain;

import lombok.Data;

@Data
public class StartVoiceParam {

    private String uid;

    private String token;

    private String license;

    private String channel;

    private String appid;
}

package com.qinglan.example.device_point.mqtt.domain;

import lombok.Data;

@Data
public class MqttMsg {

    private String cmd;

    private String requestId;

    private Object data;

    public MqttMsg(String cmd) {
        this.cmd = cmd;
    }

    public MqttMsg() {
    }
}

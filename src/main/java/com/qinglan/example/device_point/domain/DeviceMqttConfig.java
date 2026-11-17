package com.qinglan.example.device_point.domain;

import lombok.Data;

@Data
public class DeviceMqttConfig {
    private String clientId;
    private int timeout;
    private int keepalive;
    private String server;
    private Integer port;
    private String account;
    private String pwd;
    private String protocol;
    private String prefix;
    private String productId;
}

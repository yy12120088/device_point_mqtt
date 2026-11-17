package com.qinglan.example.device_point.domain;

import lombok.Data;

@Data
public class McuConfig {
    private String hw;
    private String sw;
    private String mac;
    private String iccid;
}

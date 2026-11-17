package com.qinglan.example.device_point.domain;

import lombok.Data;

import java.util.Map;

@Data
public class RadarProp {

    private String uid;

    private Map<String,Object> properties;
}

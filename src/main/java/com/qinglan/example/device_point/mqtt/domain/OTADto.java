package com.qinglan.example.device_point.mqtt.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OTADto {

    private String uid;
    //版本号
    private String espver;

    private String espfileUrl;

    private int espfilesize;

    private String espfilesha256;
    //版本号
    private String radarver;

    private String radarfileUrl;

    private int radarfilesize;

    private String radarfilesha256;

}

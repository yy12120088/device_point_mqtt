package com.qinglan.example.device_point.mqtt.service;

import com.qinglan.example.device_point.mqtt.domain.OTADto;
import com.qinglan.example.device_point.mqtt.domain.StartVoiceParam;

import java.util.List;
import java.util.Map;

public interface MqttRadarMessage {

    public abstract String OTA(OTADto dto);

    public abstract boolean setProperty(String uid, String key, String value) ;

    public abstract boolean batchSetProperty(String uid, Map<String, Object> properties);

    public abstract boolean getPositionBreathData(String uid, short seconds);

    public abstract Map<String, String> getProperties(String uid, List<String> keys);

    public abstract boolean startVoice(StartVoiceParam startVoiceParam) ;

    public abstract boolean stopVoice(String uid) ;

    //重启设备,重启加特兰和控制板,1:只重启加特兰,2:只重启控制板
    public abstract boolean restartDevice(String uid, byte type);

}

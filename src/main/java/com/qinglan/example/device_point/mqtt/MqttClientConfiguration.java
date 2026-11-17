package com.qinglan.example.device_point.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Slf4j
public class MqttClientConfiguration {

    @Autowired
    private ServerClientMqttConfig serverClientMqttConfig;

    @Bean
    EmqxClient mqttClient(){
        EmqxClient client = new EmqxClient(serverClientMqttConfig.getclientId(),
                serverClientMqttConfig.getusername(),
                serverClientMqttConfig.getpassword(),
                serverClientMqttConfig.gethostUrl(),
                serverClientMqttConfig.gettimeout(),
                serverClientMqttConfig.getkeepalive(),
                serverClientMqttConfig.isCleanStart(),
                serverClientMqttConfig.getSessionExpiryInterval(),
                serverClientMqttConfig.isEnable()
        );
        return client;
    }
}

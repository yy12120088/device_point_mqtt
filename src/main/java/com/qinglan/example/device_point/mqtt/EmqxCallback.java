package com.qinglan.example.device_point.mqtt;


import com.qinglan.example.device_point.mqtt.service.EmqxService;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @Classname MqttCallback
 * @Description 消费监听类
 */
@Component
public class EmqxCallback implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(EmqxCallback.class);

    @Lazy
    @Autowired
    private EmqxService emqxService;

    /*@Lazy
    @Autowired
    private EmqxClient emqxClient;*/


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        emqxService.subscribeCallback(topic, mqttMessage);
    }

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        logger.error("mqtt断开连接--");
//        emqxClient.refresh();

    }

    @Override
    public void mqttErrorOccurred(MqttException e) {

    }

    /**
     * 发布消息后，到达MQTT服务器，服务器回调消息接收
     * @param iMqttToken
     */
    @Override
    public void deliveryComplete(IMqttToken iMqttToken) {

    }

    /**
     * 监听mqtt连接消息
     * @param reconnect
     * @param serverURI
     */
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        //连接后，可以在此做初始化事件，或订阅
        try {
            emqxService.subscribe(EmqxClient.client);
            logger.info("[MQTT客户端订阅主题成功]");
        } catch (MqttException e) {
            logger.error("[MQTT客户端订阅主题失败]:{}",e.getMessage());
        }
    }

    @Override
    public void authPacketArrived(int i, MqttProperties mqttProperties) {

    }
}

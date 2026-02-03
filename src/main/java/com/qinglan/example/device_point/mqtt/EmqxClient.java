package com.qinglan.example.device_point.mqtt;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Classname EmqxClient
 * @Description mqtt推送客户端
 */
//@Component
public class EmqxClient {
    private static final Logger logger = LoggerFactory.getLogger(EmqxClient.class);

    @Autowired
    private EmqxCallback emqxCallback;


    /**MQTT异步客户端*/
    public static MqttAsyncClient client;

    /**连接配置*/
    private MqttConnectionOptions options;

    /**服务器地址url*/
    private String hostname;

    /**超时时间*/
    private int timeout;

    /**包活时间*/
    private int keepalive;

    /**客户端唯一ID*/
    private String clientId;

    /**用户名*/
    private String username;

    /**密码*/
    private String password;

    /**是否清除会话*/
    private boolean cleanStart;

    /**开关 */
    private boolean enable;

    /**会话过期时间*/
    private long sessionExpiryInterval;


    /**
     * 构造函数
     *
     * @param clientId
     * @param username
     * @param password
     * @param hostname
     * @param timeout
     * @param keepalive
     * @param cleanStart
     * @param sessionExpiryInterval
     * @param enable
     */
    public EmqxClient(String clientId, String username, String password, String hostname,
                      int timeout, int keepalive, boolean cleanStart, long sessionExpiryInterval, boolean enable){
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.timeout = timeout;
        this.keepalive = keepalive;
        this.cleanStart = cleanStart;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.enable = enable;
    }


    /**
     * 建立与EMQX的连接
     */
    public synchronized void connect(){
        if (!enable){
            return;
        }
        /*设置配置*/
        if (options == null){
            setOptions();
        }
        if (client == null){
            createClient();
        }
        while (!client.isConnected()){
            try {
                IMqttToken token = client.connect(options);
                token.waitForCompletion();
                logger.info("[2-MQTT客户端连接成功]");
            }catch (Exception e){
                logger.error("[MQTT客户端连接失败]:{},{}",e.getMessage(),"尝试连接中...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * 创建MQTT客户端
     */
    private void createClient(){
        if (client == null){
            try {
                //创建MQTT客户端，其中：host为主机名，clientId是连接MQTT的客户端ID，MemoryPersistence设置clientId的保存方式默认是以内存方式保存
                client = new MqttAsyncClient(hostname,clientId,new MemoryPersistence());
                //设置回调函数
                client.setCallback(emqxCallback);
                logger.info("[1-MQTT客户端创建成功]");
            }catch (MqttException e){
                logger.error("[MQTT客户端创建失败]:{}",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置连接属性
     */
    private void setOptions(){
        if (options != null){
            options = null;
        }
        options = new MqttConnectionOptions();
        options.setUserName(username);
        options.setPassword(password.getBytes());
        options.setConnectionTimeout(timeout);
        options.setKeepAliveInterval(keepalive);
        options.setCleanStart(cleanStart);
        options.setSessionExpiryInterval(sessionExpiryInterval);
        //设置自动重新连接
//        options.setAutomaticReconnect(true);
    }

    /**
     * 断开与mqtt的连接
     */
    public synchronized void disconnect(){
        //判断客户端是否null 是否连接
        if (client != null && client.isConnected()){
            try {
                IMqttToken token = client.disconnect();
                token.waitForCompletion();
            }catch (MqttException e){
                logger.error("====>>>>断开mqtt连接发生错误 message={}",e.getMessage());
            }
        }
        client = null;
    }

    /**
     * 重新连接MQTT
     */
    public synchronized void refresh(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        disconnect();
        setOptions();
        createClient();
        connect();
    }




    /**
     * 发布
     * @param qos         连接方式
     * @param retained    是否保留
     * @param topic       主题
     * @param pushMessage 消息体
     */
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        logger.info("[publish]：[{},{}]",topic,pushMessage);
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());

        try {
            if(client==null){
                return;
            }
            IMqttToken token = client.publish(topic,message);
            token.waitForCompletion();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            logger.error("=======>>>>>发布主题时发生错误 topic={},message={}",topic,e.getMessage());
        }
    }

    /**
     * 订阅某个主题
     * @param topic 主题
     * @param qos   消息质量
     *              Qos1：消息发送一次，不确保
     *              Qos2：至少分发一次，服务器确保接收消息进行确认
     *              Qos3：只分发一次，确保消息送达和只传递一次
     */
    public void subscribe(String topic, int qos){
        logger.info("=======>>>>>订阅了主题 topic={}",topic);
        try {
            IMqttToken token = client.subscribe(topic, qos);
            token.waitForCompletion();
        }catch (MqttException e){
            logger.error("=======>>>>>订阅主题 topic={} 失败 message={}",topic,e.getMessage());
        }
    }

    /**是否处于连接状态*/
    public boolean isConnected(){
        return client != null && client.isConnected();
    }

    public String getClientId() {return  clientId;};
}

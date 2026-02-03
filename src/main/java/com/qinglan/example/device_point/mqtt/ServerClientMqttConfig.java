package com.qinglan.example.device_point.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Classname ServerClientMqttConfig
 * @Description mqtt配置信息
 * @author kerwincui
 */
@Component
@ConfigurationProperties("spring.mqtt.server")
public class ServerClientMqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(ServerClientMqttConfig.class);

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 连接地址
     */
    private String hostUrl;
    /**
     * 客户Id
     */
    private String clientId;
    /**
     * 默认连接话题
     */
    private String defaultTopic;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 保持连接数
     */
    private int keepalive;

    /**是否共享订阅*/
    private boolean isShared;
    private boolean enable;
    /**分组共享订阅*/
    private boolean isSharedGroup;

    private boolean cleanStart;

    private long sessionExpiryInterval;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public void setCleanStart(boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public String getusername()
    {
        return username;
    }
    public void setusername(String username) {this.username = username;}

    public String getpassword()
    {
        return password;
    }
    public void setpassword(String password) {this.password = password;}

    public String gethostUrl()
    {
        return hostUrl;
    }
    public void sethostUrl(String hostUrl) {this.hostUrl = hostUrl;}

    public String getclientId()
    {
        return "server-"+clientId;
    }
    public void setclientId(String clientId) {this.clientId = clientId;}

    public String getdefaultTopic()
    {
        return defaultTopic;
    }
    public void setdefaultTopic(String defaultTopic) {this.defaultTopic = defaultTopic;}

    public int gettimeout()
    {
        return timeout;
    }
    public void settimeout(int timeout) {this.timeout = timeout;}

    public int getkeepalive()
    {
        return keepalive;
    }
    public void setkeepalive(int keepalive) {this.keepalive = keepalive;}

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public boolean isSharedGroup() {
        return isSharedGroup;
    }

    public void setSharedGroup(boolean sharedGroup) {
        isSharedGroup = sharedGroup;
    }
}

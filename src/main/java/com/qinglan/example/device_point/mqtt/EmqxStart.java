package com.qinglan.example.device_point.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 *项目启动执行
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class EmqxStart implements ApplicationRunner {

    @Autowired
    private EmqxClient emqxClient;

    @Override
    public void run(ApplicationArguments applicationArguments){
        //连接客户端
        emqxClient.connect();
    }
}

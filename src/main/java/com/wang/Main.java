package com.wang;

import com.wang.etcd.EtcdConfig;
import com.wang.etcd.EtcdUtil;
import com.wang.handler.CreateHandler;
import com.wang.server.httpServer;
import com.wang.service.EtcdService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {

        //1.创建spring的ioc容器对象

        ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        //2.从ioc容器中获取bean实例
        httpServer server = (httpServer) ctx.getBean("httpServer");
        server.run();
    }
}

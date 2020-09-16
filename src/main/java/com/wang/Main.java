package com.wang;

import com.wang.etcd.EtcdConfig;
import com.wang.etcd.EtcdUtil;
import com.wang.handler.CreateHandler;
import com.wang.server.httpServer;
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
        /*
        boolean success = true;

        String key = "zyh";
        String value = "zyh-value";
        String newValue = "zyh-value-new";

        System.out.println("**** 测试方法开始 ****");
        EtcdUtil.getEtcdClient();
        EtcdUtil.putEtcdValueByKey(key, value);
        String retValue = EtcdUtil.getEtcdValueByKey(key);
        // System.out.println("查询key " + key + " 对应的值是 " + retValue);
        if (value.equals(retValue))
        {
            System.out.println("数据插入成功。");
            System.out.println("数据查询成功。");
        }
        else
        {
            success = false;
            System.out.println("数据插入或查询失败！");
        }

        EtcdUtil.putEtcdValueByKey(key, newValue);
        retValue = EtcdUtil.getEtcdValueByKey(key);
        // System.out.println("查询key " + key + " 对应的值是 " + retValue);
        if (newValue.equals(retValue))
        {
            System.out.println("数据更新成功。");
        }
        else
        {
            success = false;
            System.out.println("数据更新失败！");
        }

        EtcdUtil.deleteEtcdValueByKey(key);
        retValue = EtcdUtil.getEtcdValueByKey(key);
        // System.out.println("查询key " + key + " 对应的值是 " + retValue);
        if (retValue == null)
        {
            System.out.println("数据删除成功。");
        }
        else
        {
            success = false;
            System.out.println("数据删除失败！");
        }

        // EtcdUtil.watchEtcdKey(key);

        if (success)
        {
            System.out.println("**** 测试方法全部通过。 ****");
        }
        else
        {
            System.out.println("**** 测试失败！ ****");
        }
        */
        //System.out.println("查询key " + key + " 对应的值是 " + retValue);
        EtcdUtil.getEtcdClient();
        EtcdUtil.getEtcdKeyValueByKeyPrefix("web");
        System.out.println(EtcdUtil.getEtcdKeyCountByKeyPrefix("web"));
        System.exit(1);
    }
}

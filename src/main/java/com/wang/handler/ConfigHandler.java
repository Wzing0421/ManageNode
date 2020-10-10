package com.wang.handler;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.wang.enumstatus.EnumHttpStatus;
import com.wang.etcd.EtcdConfig;
import com.wang.task.EtcdTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Random;

import com.wang.service.EtcdService;

import javax.annotation.Resource;

@Component
public class ConfigHandler extends BaseHttpHandler{

    @Resource
    private EtcdService etcdService;

    @Resource
    private CreateHandler createHandler;


    @Override
    protected EnumHttpStatus doHandlePut(Map<String, String> parameters) throws Exception {
        String ueid = parameters.get("ueid");
        String s_tmsi = parameters.get("s_tmsi");
        String ueidAndStmsiFromMap = createHandler.getSTMSIAndNodeIdFromMap(ueid);

        //1. 首先查看ueid，如果没用这个UEID则直接返回错误
        if(null == ueid) return EnumHttpStatus.UEIDNOTEXIST;

        //2. 检查map中缓存的ueid和stmsi和这次（第二次请求）是否一致;主要是根据ueid查询stmsi
        String strs[] = ueidAndStmsiFromMap.split("_");
        if(strs.length != 2) return EnumHttpStatus.UEIDSTMSINOTMATCH;
        if(!strs[0].equals(s_tmsi)) return EnumHttpStatus.UEIDSTMSINOTMATCH;
        Integer nodeId = Integer.parseInt(strs[1]);

        //3. 将ueid对应的 stmsi, nodeId, ueip和imsip存放为json
        String ueip = parameters.get("ueip");
        String imsip = parameters.get("imsip");
        JSONObject object = new JSONObject();
        object.put("STMSI", s_tmsi);
        object.put("NODEID", Integer.toString(nodeId));
        object.put("UEIP", ueip);
        object.put("IMSIP", imsip);

        //4. 在map缓存中删除这个表项
        createHandler.deleteSTMSIAndNodeIdByUeId(ueid);

        //5. 将其存放在etcd中; 需要注意delete handler会删除这这两个表项
        String valueStr = object.toJSONString();
        System.out.println("[向etcd写入]key= " + ueid + " , value = " + valueStr);
        etcdService.putUeidAndStmsiNodeIdUeIpImsIp(EtcdConfig.UeidInfo + ueid, valueStr);
        etcdService.putNodeIdANdUeIdIntoEtcd(nodeId, ueid);
        return EnumHttpStatus.SUCCESS;
    }

    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        return null;
    }
}

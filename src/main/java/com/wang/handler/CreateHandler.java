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
public class CreateHandler extends BaseHttpHandler implements InitializingBean {

    @Resource
    private EtcdService etcdService;

    @Resource
    private EtcdTask etcdTask;

    private Random ra;
    /**
     * 对于获取到的一个nodeId 查找他对应的callCount
     * 如果重试次数 >= 3则返回资源不足。
     */
    private final int RetryTimes = 3;

    /**
     * 获取请求头
     *
     * @param httpExchange
     * @return
     */
    private String getRequestHeader(HttpExchange httpExchange) {
        Headers headers = httpExchange.getRequestHeaders();
        return headers.entrySet().stream()
                .map((Map.Entry<String, List<String>> entry) -> entry.getKey() + ":" + entry.getValue().toString())
                .collect(Collectors.joining("<br/>"));
    }

    /**
     * 获取请求参数
     *
     * @param httpExchange
     * @return
     * @throws Exception
     */
    private String getRequestParam(HttpExchange httpExchange) throws Exception {
        String paramStr = "";

        if (httpExchange.getRequestMethod().equals("GET")) {
            //GET请求读queryString
            paramStr = httpExchange.getRequestURI().getQuery();
        } else {
            //非GET请求读请求体
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
            StringBuilder requestBodyContent = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                requestBodyContent.append(line);
            }
            paramStr = requestBodyContent.toString();
        }

        return paramStr;
    }


    /**
     * insert method for put
     * @param parameters
     * @throws Exception
     * @return
     */
    @Override
    protected EnumHttpStatus doHandlePut(Map<String, String> parameters) throws Exception {
        // 1. 获得6个参数并且校验
        String ueid = parameters.get("ueid");
        String s_tmsi = parameters.get("s_tmsi");
        String ueip = parameters.get("ueip");
        String ueport = parameters.get("ueport");
        String imsip = parameters.get("imsip");
        String imsport = parameters.get("imsport");

        if(ueid == null){
            System.out.println("[Error] Create Handler: get null ueid");
            return EnumHttpStatus.NULLPARAM;
        }
        if(s_tmsi == null){
            System.out.println("[Error] Create Handler: get null s_tmsi");
            return EnumHttpStatus.NULLPARAM;
        }
        if(ueip == null){
            System.out.println("[Error] Create Handler: get null ueip");
            return EnumHttpStatus.NULLPARAM;
        }
        if(ueport == null){
            System.out.println("[Error] Create Handler: get null ueport");
            return EnumHttpStatus.NULLPARAM;
        }
        if(imsip == null){
            System.out.println("[Error] Create Handler: get null imsip");
            return EnumHttpStatus.NULLPARAM;
        }
        if(imsport == null){
            System.out.println("[Error] Create Handler: get null imsport");
            return EnumHttpStatus.NULLPARAM;
        }

        //2. 获得一个空闲的节点
        for(int i = 0; i < RetryTimes; i++){
            Integer nodeId = getNodeId();

            //返回-1表示当前没有编码节点资源可用
            if(nodeId == -1){
                System.out.println("[Error] Create Handler: node count is 0!");
                return EnumHttpStatus.RESOURCENOTENOUGH;
            }
            Long callCount = etcdService.getCallCountFromEtcdByNodeId(nodeId);
            if(callCount < 200){

                System.out.println("[Info] Create Handler: Allocate nodeid is: " + nodeId);
                //3. 将所有数据写入etcd
                JSONObject object = new JSONObject();
                object.put("STMSI", s_tmsi);
                object.put("NODEID", Integer.toString(nodeId));
                object.put("UEIP", ueip);
                object.put("UEPORT", ueport);
                object.put("IMSIP", imsip);
                object.put("IMSPORT", imsport);

                String valueStr = object.toJSONString();
                System.out.println("[info]Create Handler: write to etcd: key= " + ueid + " , value = " + valueStr);
                etcdService.putUeidAndStmsiNodeIdUeIpImsIp(EtcdConfig.UeidInfo + ueid, valueStr);
                etcdService.putNodeIdANdUeIdIntoEtcd(nodeId, ueid);

                //返回状态码为200
                return EnumHttpStatus.AVAILABLE;
            }
        }
        return EnumHttpStatus.RESOURCENOTENOUGH;

    }

    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        return null;
    }

    /**
     * allocate a node id for one call
     * random one index
     * @return
     */
    private Integer getNodeId(){
        List<Integer> nodeList = etcdTask.getNodeList();
        if(nodeList == null || nodeList.size() == 0) return -1;
        Integer index = ra.nextInt(nodeList.size());
        return  nodeList.get(index);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ra = new Random();
    }

}

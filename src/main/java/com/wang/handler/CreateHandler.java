package com.wang.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.wang.enumstatus.EnumHttpStatus;
import com.wang.task.EtcdTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
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
        String ueid = parameters.get("ueid");
        String s_tmsi = parameters.get("s_tmsi");

        for(int i = 0; i < RetryTimes; i++){
            Integer nodeId = getNodeId();
            Long callCount = etcdService.getCallCountFromEtcdByNodeId(nodeId);
            if(callCount < 200){
                etcdService.putUeidAndStmsiAndNodeIdIntoEtcd(ueid, s_tmsi, nodeId);
                System.out.println(nodeId);
                System.out.println(callCount);
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
        Integer index = ra.nextInt(nodeList.size());
        return  nodeList.get(index);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ra = new Random();
    }
}

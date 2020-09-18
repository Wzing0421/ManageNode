package com.wang.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.wang.service.EtcdService;

import javax.annotation.Resource;

@Component
public class CreateHandler extends BaseHttpHandler{

    @Resource
    private EtcdService etcdService;
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
     */
    @Override
    protected void doHandlePut(Map<String, String> parameters) throws Exception {
        String ueid = parameters.get("ueid");
        String s_tmsi = parameters.get("s_tmsi");

        Integer nodeId = getNodeId();
        etcdService.putUeidAndStmsiAndNodeIdIntoEtcd(ueid, s_tmsi, nodeId);
    }

    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        return null;
    }

    /**
     * allocate a node id for one call
     * @return
     */
    private Integer getNodeId(){
        return 1;
    }
}

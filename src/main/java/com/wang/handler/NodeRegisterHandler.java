package com.wang.handler;

import com.wang.service.EtcdService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class NodeRegisterHandler extends BaseHttpHandler{
    @Resource
    private EtcdService etcdService;

    @Override
    protected void doHandlePut(Map<String, String> parameters) throws Exception {
        return;
    }

    /**
     * allocate a nodeId to the encoding node
     * @param parameters
     * @return
     * @throws Exception
     */
    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        List<Integer> nodeList = etcdService.getAllNodesFromEtcd();
        Integer nodeId = getAvaliableNodeId(nodeList);
        etcdService.putNodeIdIntoEtcd(nodeId);
        return nodeId;
    }

    private Integer getAvaliableNodeId(List<Integer> nodeList){
        int nodeId = 0;
        for(int i = 0; i < nodeList.size(); i++){
            if(i + 1 != nodeList.get(i)) return i + 1;
        }
        return nodeList.size() + 1;
    }

}

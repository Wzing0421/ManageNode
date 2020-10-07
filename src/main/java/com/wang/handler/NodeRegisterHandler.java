package com.wang.handler;

import com.wang.enumstatus.EnumHttpStatus;
import com.wang.etcd.EtcdConfig;
import com.wang.service.EtcdService;
import com.wang.task.EtcdTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class NodeRegisterHandler extends BaseHttpHandler{
    @Resource
    private EtcdService etcdService;

    @Resource
    private EtcdTask etcdTask;

    @Override
    protected EnumHttpStatus doHandlePut(Map<String, String> parameters) throws Exception {
        return null;
    }

    /**
     * allocate a nodeId to the encoding node
     * @param parameters
     * @return
     * @throws Exception
     */
    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        if(parameters.get("RemoteNodeIp") == null){
            System.out.println("No Node Ip from request!");
            return null;
        }
        List<Integer> nodeList = etcdService.getAllNodesFromEtcd();
        Integer nodeId = getAvailableNodeId(nodeList);
        etcdService.putNodeIdIntoEtcd(nodeId);

        //把NodeID 和其对应的NodeIP 也插入表中
        String keyStr = EtcdConfig.NodeId + Integer.toString(nodeId);
        String valueStr = EtcdConfig.NodeIp + parameters.get("RemoteNodeIp");
        etcdService.putNodeIdAndNodeIpIntoEtcd(keyStr, valueStr);

        etcdTask.updateTimestampByNodeId(nodeId);
        return nodeId;
    }

    private Integer getAvailableNodeId(List<Integer> nodeList){
        int nodeId = 0;
        for(int i = 0; i < nodeList.size(); i++){
            if(i + 1 != nodeList.get(i)) return i + 1;
        }
        return nodeList.size() + 1;
    }

}

package com.wang.handler;

import com.wang.enumstatus.EnumHttpStatus;
import com.wang.service.EtcdService;
import com.wang.task.EtcdTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class HeartBeatHandler extends BaseHttpHandler{

    @Resource
    private EtcdTask etcdTask;

    @Override
    protected EnumHttpStatus doHandlePut(Map<String, String> parameters) throws Exception {
        return null;
    }

    /**
     * 此http接口处理node 节点的心跳信息
     * 心跳信息默认每5s更新一次
     * @param parameters
     * @return
     * @throws Exception
     */
    @Override
    protected Integer doHandlePost(Map<String, String> parameters) throws Exception {
        String NodeIdStr = parameters.get("NodeId");
        Integer nodeId = Integer.valueOf(NodeIdStr);
        etcdTask.updateTimestampByNodeId(nodeId);
        return nodeId;
    }
}

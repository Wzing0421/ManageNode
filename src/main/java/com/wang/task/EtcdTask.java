package com.wang.task;

import com.wang.service.EtcdService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EtcdTask {
    private ConcurrentHashMap<Integer, Long> NodeID2UEIDTotalCount = new ConcurrentHashMap<Integer, Long>();

    @Resource
    private EtcdService etcdService;
    /**
     * 定时任务：每次从etcd中查询出有多少个节点信息，对于每个节点需要查询出这个节点上面有多少路通话并更新map
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 10)
    public void getAllNodeIDFromEtcdTask() throws Exception{
        List<Integer> nodeList = etcdService.getAllNodesFromEtcd();
        for(Integer id : nodeList){
            Long callCount = etcdService.getCallCountFromEtcdByNodeId(id);
            NodeID2UEIDTotalCount.put(id, callCount);
        }
    }
}

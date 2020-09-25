package com.wang.task;

import com.wang.service.EtcdService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class EtcdTask {

    private final List<Integer> nodeList = new ArrayList<>();

    @Resource
    private EtcdService etcdService;
    /**
     * 定时任务：每次从etcd中查询出有多少个节点信息
     * 9月25更新：目前我的想法不需要10s内更新一次 nodeId 对应的 callCount,而是更新一次所有的现有的nodeId就可以了;
     * 这是因为createHandler的接入策略：
     * 1. 从现有的nodeId中随机抽出一个nodeId获得它的callCount。
     * 2. callCount < 200 则返回这个nodeId
     *    callCount >= 200 则重试
     * 3. 当重试次数 >= 3 则说明资源不足，拒绝接入
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 10)
    public void getAllNodeIDFromEtcdTask() throws Exception{
        List<Integer> nodeListFromEtcd = etcdService.getAllNodesFromEtcd();
        synchronized (this){
            nodeList.clear();
            //Long callCount = etcdService.getCallCountFromEtcdByNodeId(id);
            nodeList.addAll(nodeListFromEtcd);
        }
    }

    public List<Integer> getNodeList(){
        return this.nodeList;
    }
}

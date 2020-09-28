package com.wang.task;

import com.wang.service.EtcdService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EtcdTask {

    private final List<Integer> nodeList = new ArrayList<>();

    //这个map记录了每一个node节点上一次心跳的时间戳。方便定时扫描的时候扫描出挂掉的节点
    private final ConcurrentHashMap<Integer, Date> NodeLastHeartBeatMap = new ConcurrentHashMap<>();
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

    /**
     * scan the map every 12 seconds
     * heart beat frequency is 5s/times.
     * If current_time - timestamp >= 12 it means node down, so delete it
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 12)
    public void scanNodeTimeout() throws Exception{
        long millisecond1 = new Date().getTime();
        List<Integer> timeoutNodeList = new ArrayList<>();
        for(Integer key : NodeLastHeartBeatMap.keySet()){
            if( ((millisecond1 - NodeLastHeartBeatMap.get(key).getTime()) / 1000) >= 12){
                timeoutNodeList.add(key);
            }
        }
        // 目前逻辑是删除 node table上面的nodeId节点
        for(Integer nodeId : timeoutNodeList){
            System.out.println("nodeId: " + Integer.toString(nodeId) + " timeout!!");
            NodeLastHeartBeatMap.remove(nodeId);
            etcdService.deleteNodeIdTableFromEtcd(nodeId);
        }
    }

    public List<Integer> getNodeList(){
        return this.nodeList;
    }

    public void updateTimestampByNodeId(Integer nodeId){
        NodeLastHeartBeatMap.put(nodeId, new Date());
    }



}

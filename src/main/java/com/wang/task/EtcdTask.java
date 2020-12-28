package com.wang.task;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.wang.etcd.EtcdConfig;
import com.wang.service.EtcdService;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EtcdTask implements InitializingBean {

    private final List<Integer> nodeList = new ArrayList<>();

    private Random ra;

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
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 1)
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
    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 20)
    public void scanNodeTimeout() throws Exception{
        long millisecond1 = new Date().getTime();
        List<Integer> timeoutNodeList = new ArrayList<>();
        for(Integer key : NodeLastHeartBeatMap.keySet()){
            if( ((millisecond1 - NodeLastHeartBeatMap.get(key).getTime()) / 1000) >= 20){
                timeoutNodeList.add(key);
            }
        }

        /**
         * 目前逻辑是删除 node table上面的nodeId节点, 以及对应的NodeId表上面的IP
         * 12月２８日更新：　新加入逻辑：将挂掉节点上的通话直接迁移至新的节点．
         * 选择的迁移方式是将此节点上面的通话随机迁移到其他可用节点上，而不是提前对每一路通话分配空闲节点，
         * 因为如果提前分配了，在当前节点挂掉的时候 不能保证之前分配的那个预备节点还在２００路之下
         */
        for(Integer nodeId : timeoutNodeList){

            //删除现有的宕机节点信息
            System.out.println("nodeId: " + Integer.toString(nodeId) + " timeout!!");
            NodeLastHeartBeatMap.remove(nodeId);
            etcdService.deleteNodeIdTableFromEtcd(nodeId);
            etcdService.deleteNodeIdAndNodeIpFromEtcd(nodeId);
            //对宕机节点做通话迁移
            ShiftConversationFromDowningNode(nodeId);
        }
    }

    public List<Integer> getNodeList(){
        return this.nodeList;
    }

    public void updateTimestampByNodeId(Integer nodeId){
        NodeLastHeartBeatMap.put(nodeId, new Date());
    }

    /**
     * 把宕机节点的通话迁移至其余可用节点，　步骤如下：
     * 1. 获得此节点对应的所有ueid
     * 2.　对每一个ueid(<uplinkueid><downlinkueid>) 寻找一个新的可用节点
     * 3.　删除数据库中的原来的NODEUEID_<nodeId>_<uplinkueid>_<downlinkueid>
     * 4. 将新的可用节点的数据插入数据库中
     * @param nodeId
     */
    private void ShiftConversationFromDowningNode(Integer nodeId) throws Exception {

        // <uplinkueid>_<downlinkueid>
        List<String> UEIDsList = etcdService.getAllUEIDsByNodeIdFromEtcd(nodeId);

        for (String ueid : UEIDsList) {

            //1 分配一路新的通话节点,不断轮询至分配成功
            Integer newNodeId = -1;
            String[] s = ueid.split("_");
            String uplinkueid = s[0];
            String downlinkueid = s[1];

            while(true){

                // 分配新的编解码id
                newNodeId = getNewNodeId(nodeId);
                if(newNodeId == -1){
                    System.out.println("[Error]: No nodeId available!");
                    break;
                }
                //必须小于200路通话，否则重新选取node
                if(etcdService.getCallCountFromEtcdByNodeId(newNodeId) < 200){

                    //只需要更新NODEID即可
                    JSONObject jsonObject = etcdService.getJsonObjectByUEID(uplinkueid, downlinkueid);

                    //删除NODEUEID之前的信息
                    etcdService.deleteNodeUEIDFromEtcd(nodeId, ueid);

                    //更新新的信息
                    etcdService.putNodeIdANdUeIdIntoEtcd(newNodeId, ueid);
                    if(jsonObject != null){
                        jsonObject.remove("NODEID");
                        jsonObject.put("NODEID", Integer.toString(newNodeId));
                        etcdService.putUeidAndStmsiNodeIdUeIpImsIp(EtcdConfig.UeidInfo + uplinkueid + "_" + downlinkueid, jsonObject.toJSONString());
                    }
                    break;
                }
            }
        }

    }

    /**
     * allocate a node id for one call, not the downing node
     * random one index
     * @return
     */
    private Integer getNewNodeId(Integer nodeId){
        if(nodeList == null || nodeList.size() == 0) return -1;

        //不能与已经挂掉的nodeId重复
        Integer index = ra.nextInt(nodeList.size());
        while (nodeList.get(index) == nodeId){
            index = ra.nextInt(nodeList.size());
        }
        return  nodeList.get(index);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ra = new Random();
    }
}

package com.wang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wang.etcd.EtcdConfig;
import com.wang.etcd.EtcdUtil;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.ByteSequence;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Charsets.UTF_8;

@Component
public class EtcdService {

    /**
     * get all available nodes from etcd
     *
     * @return
     */
    public List<Integer> getAllNodesFromEtcd() throws Exception {
        EtcdUtil.getEtcdClient();
        List<KeyValue> kvs = EtcdUtil.getEtcdKeyValueByKeyPrefix(EtcdConfig.NodeTable);
        List<Integer> nodeList = getNodeList(kvs);
        Collections.sort(nodeList);
        return nodeList;
    }

    /**
     *
     * @param nodeId
     * @return All UEIDs( uplink ueid and downlink ueid) on specific nodeId
     * @throws Exception
     */
    public List<String> getAllUEIDsByNodeIdFromEtcd(Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        List<KeyValue> kvs = EtcdUtil.getEtcdKeyValueByKeyPrefix(EtcdConfig.NodeUeId + Integer.toString(nodeId));
        return getUEIDs(kvs);
    }

    /**
     * get Uplink ueid and downlink ueid fron kvs list
     * @param kvs
     * @return
     */
    private List<String> getUEIDs(List<KeyValue> kvs){
        List<String> UEIDsList = new ArrayList<>();
        for (KeyValue kv : kvs) {
            String key = kv.getKey().toString(UTF_8);
            String[] value = key.split("_");
            if (value.length != 4) {
                System.out.println("Error: Node length != 4 key is : " + key);
            } else {

                // key is: NODEUEID_<nodeId>_<uplinkueid>_<downlinkueid>, so get the third and the forth params
                UEIDsList.add(value[2] + "_" + value[3]);
            }
        }
        return UEIDsList;
    }

    /**
     * get Integer from NodeList's key
     *
     * @param kvs
     * @return
     */
    private List<Integer> getNodeList(List<KeyValue> kvs) {
        List<Integer> NodeList = new ArrayList<>();
        for (KeyValue kv : kvs) {
            String key = kv.getKey().toString(UTF_8);
            String[] value = key.split("_");
            if (value.length != 2) {
                System.out.println("Error: Node length != 2 key is : " + key);
            } else {
                NodeList.add(Integer.parseInt(value[1]));
            }
        }
        return NodeList;
    }

    public String getValueFromEtcdByKey(String key) throws Exception {
        EtcdUtil.getEtcdClient();
        return EtcdUtil.getEtcdValueByKey(key);
    }

    /**
     * get total call counts from each node id
     *
     * @param id nodeId
     * @return
     * @throws Exception
     */
    public Long getCallCountFromEtcdByNodeId(Integer id) throws Exception {
        EtcdUtil.getEtcdClient();
        String prefix = EtcdConfig.NodeUeId + Integer.toString(id);
        // 注意: NODEUEID_<nodeId>_<uplinkueid>_<downlinkueid>实际上存放了两路的通话
        return 2 * EtcdUtil.getEtcdKeyCountByKeyPrefix(prefix);
    }

    /**
     * put ueid and stmsi and nodeid into etcd
     * key -> value is: UEID_<ueid> -> STMSI_<s_tmsi>_NODEID_<nodeid>
     *
     * @param ueId
     * @param stmsi
     * @param nodeId
     * @throws Exception
     */
    public void putUeidAndStmsiIntoEtcd(String ueId, String stmsi, Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.UeidInfo + ueId;
        String valueStr = EtcdConfig.S_Tmsi + stmsi + "_" + EtcdConfig.NodeId + Integer.toString(nodeId);
        EtcdUtil.putEtcdValueByKey(keyStr, valueStr);
    }

    /**
     * put nodeId and ueId into etcd
     * key -> value is: NODEUEID_<nodeId>_<ueid> -> "1"
     *
     * @param ueId
     * @param nodeId
     * @throws Exception
     */
    public void putUeidAndNodeIdIntoEtcd(String ueId, Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.NodeUeId + Integer.toString(nodeId) + "_" + ueId;
        String valueStr = "1";
        EtcdUtil.putEtcdValueByKey(keyStr, valueStr);
    }

    public void putNodeIdIntoEtcd(Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.NodeTable + Integer.toString(nodeId);
        String valueStr = "1";
        EtcdUtil.putEtcdValueByKey(keyStr, valueStr);
    }

    /**
     * delete from etcd, key is:
     * 1. UEID_<ueid>
     * 2. NODEUEID_<nodeId>_<ueid> -> "1"
     *
     * @param uplinkueid
     * @param downlinkueid
     * @throws Exception
     */
    public void deleteUeidAndStmsiFromEtcd(String uplinkueid, String downlinkueid) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.UeidInfo + uplinkueid + "_" + downlinkueid;
        String valueStr = EtcdUtil.getEtcdValueByKey(keyStr);
        //这种是当输入的key找不到的时候的处理；实际上信令网关根本不管管理节点是否删除成功，信令网关会删除，所以管理节点不论操作结果是什么都返回200OK就行
        if (valueStr == null) {
            return;
        }
        JSONObject jsonObject = JSON.parseObject(valueStr);
        String nodeId = (String) jsonObject.get("NODEID");
        //注意这里要删除两个表里面的数据
        EtcdUtil.deleteEtcdValueByKey(EtcdConfig.UeidInfo + uplinkueid + "_" + downlinkueid);
        EtcdUtil.deleteEtcdValueByKey(EtcdConfig.NodeUeId + nodeId + "_" + uplinkueid + "_" + downlinkueid);
        //EtcdUtil.deleteEtcdValueByKey(EtcdConfig.NodeUeId + nodeId + "_" + downlinkueid);
    }

    /**
     * 删除指定 nodeId 和ueid(uplinkueid, downlinkueid)的数据
     * @param nodeId
     * @param ueid
     * @throws Exception
     */
    public void deleteNodeUEIDFromEtcd(Integer nodeId, String ueid) throws Exception{
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.NodeUeId + Integer.toString(nodeId) + "_" + ueid;
        EtcdUtil.deleteEtcdValueByKey(keyStr);
    }

    public JSONObject getJsonObjectByUEID(String uplinkueid, String downlinkueid) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.UeidInfo + uplinkueid + "_" + downlinkueid;
        String valueStr = EtcdUtil.getEtcdValueByKey(keyStr);
        //这种是当输入的key找不到的时候的处理；实际上信令网关根本不管管理节点是否删除成功，信令网关会删除，所以管理节点不论操作结果是什么都返回200OK就行
        if (valueStr == null) {
            return null;
        }
        return JSON.parseObject(valueStr);
    }

    /**
     * put key and value into etcd
     * 1: UEID_<ueid> -> STMSI_<stmsi>_NODEID_<nodeid>
     * 2: NODEUEID_<nodeid>_<ueid> -> "1"
     *
     * @param ueid
     * @param stmsi
     * @param nodeId
     * @throws Exception
     */
    public void putUeidAndStmsiAndNodeIdIntoEtcd(String ueid, String stmsi, Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr1 = EtcdConfig.UeidInfo + ueid;
        String valueStr1 = EtcdConfig.S_Tmsi + stmsi + "_" + EtcdConfig.NodeId + Integer.toString(nodeId);
        String keyStr2 = EtcdConfig.NodeUeId + Integer.toString(nodeId) + "_" + ueid;
        String valueStr2 = "1";
        EtcdUtil.putEtcdValueByKey(keyStr1, valueStr1);
        EtcdUtil.putEtcdValueByKey(keyStr2, valueStr2);
    }

    public void putNodeIdANdUeIdIntoEtcd(Integer nodeId, String ueid) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr2 = EtcdConfig.NodeUeId + Integer.toString(nodeId) + "_" + ueid;
        String valueStr2 = "1";
        EtcdUtil.putEtcdValueByKey(keyStr2, valueStr2);
    }

    private String getStmsiFromValueStr(String valueStr) {
        int index1 = valueStr.indexOf("STMSI_");
        int index2 = valueStr.indexOf("_NODEID_");
        if (index1 == -1 || index2 == -1) {
            return null;
        }
        return valueStr.substring(index1 + EtcdConfig.S_Tmsi.length(), index2);
    }

    private Integer getNodeIdFromValueStr(String valueStr) {
        int index = valueStr.indexOf("_" + EtcdConfig.NodeId);
        if (index == -1) {
            return null;
        }
        return Integer.parseInt(valueStr.substring(index + EtcdConfig.NodeId.length() + 1));
    }

    /**
     * when a nodeId are scanned for timeout, delete nodeId table  from etcd
     * @param nodeId
     */
    public void deleteNodeIdTableFromEtcd(Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.NodeTable + Integer.toString(nodeId);
        EtcdUtil.deleteEtcdValueByKey(keyStr);
    }

    public void putNodeIdAndNodeIpIntoEtcd(String key, String value) throws Exception{
        EtcdUtil.getEtcdClient();
        EtcdUtil.putEtcdValueByKey(key, value);
    }

    public void deleteNodeIdAndNodeIpFromEtcd(Integer nodeId) throws Exception {
        EtcdUtil.getEtcdClient();
        String keyStr = EtcdConfig.NodeId + Integer.toString(nodeId);
        EtcdUtil.deleteEtcdValueByKey(keyStr);
    }

    public void putUeidAndStmsiNodeIdUeIpImsIp(String key, String value) throws Exception{
        EtcdUtil.getEtcdClient();
        EtcdUtil.putEtcdValueByKey(key, value);
    }
}

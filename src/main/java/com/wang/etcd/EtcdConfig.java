package com.wang.etcd;

public class EtcdConfig {

    public static final String IP = "http://162.105.85.184";

    public static final String port = "2379";

    /**
     * prefix for table: nodeId -> ueid
     */
    public static final String NodeUeId = "NODEUEID_";

    /**
     * prefix for table: all encoding nodes
     */
    public static final String NodeTable = "NODETABLE_";

    /**
     * prefix for table: ueid -> (s_tmsi, nodeId)
     */
    public static final String UeidInfo = "UEID_";

    /**
     * prefix for s_tmsi
     */
    public static final String S_Tmsi = "STMSI_";

    /**
     * prefix for node id
     */
    public static final String NodeId = "NODEID_";
}

package servicetest;

import com.wang.etcd.EtcdConfig;
import com.wang.etcd.EtcdUtil;
import com.wang.service.EtcdService;
import mockit.Mocked;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/applicationContext*.xml"})
/**
 * 单元测试
 */
public class EtcdServiceTest {
    @Tested
    private EtcdService etcdService;

    @Test
    public void TestEmpty(){
        String str = null;
        Assert.assertNull(str);
    }
    /*
    @Test
    public void TestGetAllNodesFromEtcd() throws Exception {
        List<Integer> list1 = new ArrayList<Integer>();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        list1.add(4);
        list1.add(5);
        list1.add(6);
        list1.add(7);
        list1.add(8);
        list1.add(9);
        list1.add(10);
        list1.add(11);
        List<Integer> kvList = etcdService.getAllNodesFromEtcd();
        Assert.assertEquals(list1, kvList);
    }

    @Test
    public void TestGetCallCountFromEtcdByNodeId() throws Exception{
        Integer num = 3;
        Long res = num.longValue();
        Long res1 = etcdService.getCallCountFromEtcdByNodeId(1);
        Assert.assertEquals(res, res1);

        num = 8;
        res = num.longValue();
        res1 = etcdService.getCallCountFromEtcdByNodeId(2);
        Assert.assertEquals(res, res1);

        num = 3;
        res = num.longValue();
        res1 = etcdService.getCallCountFromEtcdByNodeId(3);
        Assert.assertEquals(res, res1);

        num = 3;
        res = num.longValue();
        res1 = etcdService.getCallCountFromEtcdByNodeId(4);
        Assert.assertEquals(res, res1);

        num = 1;
        res = num.longValue();
        res1 = etcdService.getCallCountFromEtcdByNodeId(5);
        Assert.assertEquals(res, res1);
    }

    @Test
    public void TestPutUeidAndStmsiIntoEtcd() throws Exception{
        String ueid = "12345";
        String s_tmsi = "abcde";
        Integer nodeid = 1;
        etcdService.putUeidAndStmsiIntoEtcd(ueid, s_tmsi, nodeid);

        String res = EtcdUtil.getEtcdValueByKey("UEID_12345");
        Assert.assertEquals(res, "STMSI_abcde_NODEID_1");

    }

    @Test
    public void TestPutUeidAndNodeIdIntoEtcd() throws Exception{
        String ueid = "12345";
        int nodeid = 1;
        etcdService.putUeidAndNodeIdIntoEtcd(ueid, nodeid);

        String res = EtcdUtil.getEtcdValueByKey("NODEUEID_" + Integer.toString(nodeid) + "_" + ueid);
        Assert.assertEquals(res, "1");

        nodeid = 10;
        res = EtcdUtil.getEtcdValueByKey("NODEUEID_" + Integer.toString(nodeid) + "_" + ueid);
        Assert.assertNull(res);
    }

    @Test
    public void TestPutUeidAndStmsiAndNodeIdIntoEtcd() throws Exception{
        String ueid = "654321";
        String stmsi = "abcvds";
        int nodeid = 2;

        String keyStr1 = EtcdConfig.UeidInfo + ueid;
        String keyStr2 = EtcdConfig.NodeUeId + Integer.toString(nodeid) + "_" + ueid;

        etcdService.putUeidAndStmsiAndNodeIdIntoEtcd(ueid, stmsi, nodeid);
        String valueStr1 = etcdService.getValueFromEtcdByKey(keyStr1);
        String valueStr2 = etcdService.getValueFromEtcdByKey(keyStr2);
        String testValue1 = "STMSI_" + stmsi + "_NODEID_" + Integer.toString(nodeid);
        String testValue2 = "1";
        Assert.assertEquals(testValue1, valueStr1);
        Assert.assertEquals(testValue2,valueStr2);
    }

    @Test
    public void TestDeleteUeidAndStmsiFromEtcd() throws Exception{
        String ueid = "654321";
        int nodeid = 2;
        String stmsi = "abcvds";

        etcdService.putUeidAndStmsiAndNodeIdIntoEtcd(ueid, stmsi, nodeid);
        String res1 = etcdService.getValueFromEtcdByKey(EtcdConfig.UeidInfo + ueid);
        String res2 = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeUeId + Integer.toString(nodeid) + "_" + ueid);
        Assert.assertNotNull(res1);
        Assert.assertNotNull(res2);

        etcdService.deleteUeidAndStmsiFromEtcd(ueid, stmsi);
        res1 = etcdService.getValueFromEtcdByKey(EtcdConfig.UeidInfo + ueid);
        res2 = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeUeId + Integer.toString(nodeid) + "_" + ueid);
        Assert.assertNull(res1);
        Assert.assertNull(res2);
    }

    @Test
    public void TestPutNodeIdIntoEtcd() throws Exception{
        int nodeId = 100;
        String res1 = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeTable + Integer.toString(nodeId));
        Assert.assertNull(res1);

        etcdService.putNodeIdIntoEtcd(nodeId);
        res1 = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeTable + Integer.toString(nodeId));
        Assert.assertNotNull(res1);
    }

    @Test
    public void TestDeleteNodeIdTableFromEtcd() throws Exception{
        int nodeId = 100;
        String res = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeTable + Integer.toString(nodeId));
        Assert.assertNotNull(res);

        etcdService.deleteNodeIdTableFromEtcd(nodeId);
        res = etcdService.getValueFromEtcdByKey(EtcdConfig.NodeTable + Integer.toString(nodeId));
        Assert.assertNull(res);
    }

    @Test
    public void TestPutNodeIdAndNodeIpIntoEtcd() throws Exception{

        String key = "NODEID_3";
        String value = "NODEIP_162.105.85.184";
        etcdService.putNodeIdAndNodeIpIntoEtcd(key, value);
        String res = etcdService.getValueFromEtcdByKey("NODEID_3");
        Assert.assertNotNull(res);
    }

    @Test
    public void TestGetAllUEIDsByNodeIdFromEtcd() throws Exception {

        List<String> kvList = etcdService.getAllUEIDsByNodeIdFromEtcd(2);
        kvList.forEach(System.out::println);
    }
    */


}

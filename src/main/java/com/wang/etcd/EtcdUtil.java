package com.wang.etcd;

import static com.google.common.base.Charsets.UTF_8;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class EtcdUtil {
    
    public static Client etcdClient = null;

    /**
     * get a connected etcd client
     * @return
     */
    public static Client getEtcdClient() throws IOException {
        if(etcdClient == null){
            synchronized (EtcdUtil.class) {
                Properties properties = new Properties();
                InputStream inputStream = null;
                try{
                    properties.load((new FileInputStream("res/Config.properties")));
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                String ip = properties.getProperty("ETCDIp");
                String port = properties.getProperty("ETCDPort");
                etcdClient = Client.builder().endpoints(ip+':'+port).build();
            }
        }
        return etcdClient;
    }

    /**
     * get etcd value by key
     * @param key etcd key
     * @return
     * @throws Exception
     */
    public static String getEtcdValueByKey(String key) throws Exception{
        KeyValue kv = getEtcdKeyValueByKey(key);
        if(kv == null){
            return null;
        }
        else{
            return kv.getValue().toString(UTF_8);
        }
    }

    public static KeyValue getEtcdKeyValueByKey(String key) throws Exception {
        List<KeyValue> kvsList = etcdClient.getKVClient().get(ByteSequence.from(key, UTF_8)).get().getKvs();
        if (kvsList.size() > 0) {
            return kvsList.get(0);
        } else {
            return null;
        }
    }

    /**
     * TODO: get all with key prefix
     * @param prefix
     * @return
     * @throws Exception
     */
    public static List<KeyValue> getEtcdKeyValueByKeyPrefix(String prefix) throws Exception {
        GetOption getOption = GetOption.newBuilder().withPrefix(ByteSequence.from(prefix, UTF_8)).build();
        List<KeyValue> kvsList = etcdClient.getKVClient().get(ByteSequence.from(prefix, UTF_8), getOption).get().getKvs();
        return kvsList;
    }

    public static Long getEtcdKeyCountByKeyPrefix(String prefix) throws Exception {
        GetOption getOption = GetOption.newBuilder().withPrefix(ByteSequence.from(prefix, UTF_8)).withCountOnly(true).build();
        return etcdClient.getKVClient().get(ByteSequence.from(prefix, UTF_8), getOption).get().getCount();
    }

    /**
     * delete etcd value by key
     * @param key etcd key
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void deleteEtcdValueByKey(String key) throws  InterruptedException, ExecutionException{
        etcdClient.getKVClient().delete(ByteSequence.from(key, UTF_8)).get();
    }

    /**
     * put key and value into etcd
     * @param key etcd key
     * @param value
     * @throws Exception
     */
    public static void putEtcdValueByKey(String key, String value) throws Exception{
        etcdClient.getKVClient().put(ByteSequence.from(key,UTF_8), ByteSequence.from(value, UTF_8)).get();
    }

}

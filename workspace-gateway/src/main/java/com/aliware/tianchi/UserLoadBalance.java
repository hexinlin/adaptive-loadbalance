package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    private int total = 16383;
    private int num = 3;
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int large = CallbackListenerImpl.largeMemorySize;
        int medium = CallbackListenerImpl.mediumMemorySize;
        int small = CallbackListenerImpl.mediumMemorySize;
        int a = 0;
        int b = 0;
        int c = large+medium+small;
        int temp = 0;
        if(large==0) {
            //初始化值，客户端还未收到服务端内存数据。
            temp = total/num;
            a = temp<<1;
            b = a +(temp>>1);
        }else {
            //根据实时内存，分配虚拟槽
            temp = total/c;
            a =temp*large;
            b = a + (temp*medium);

        }
        //1.生成随机字符串key
        String key = UUID.randomUUID().toString();
        //2.获取key的hash值
        int hash = getCrc(key.getBytes());
        int index = 0;
        if(hash<=a) {
        }else if(hash>a&&hash<=b){
            index = 1;
        }else {
            index = 2;
        }
        return invokers.get(index);
    }


    private static int getCrc(byte[] data) {
        int high;
        int flag;
        int wcrc = 0xffff;
        for (int i = 0; i < data.length; i++) {
            high = wcrc >> 8;
            wcrc = high ^ data[i];
            for (int j = 0; j < 8; j++) {
                flag = wcrc & 0x0001;
                wcrc = wcrc >> 1;
                if (flag == 1)
                    wcrc ^= 0xa001;
            }
        }
        return wcrc;
    }



}

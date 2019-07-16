package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    private static int total = 16383;
    private static int num = 2;

    public final static int smallPort = 20880;
    public final static int mediumPort = 20870;
    public final static int largePort = 20890;

    private int[] array = null;
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        if(CallbackListenerImpl.initSmallMemorySize==0) {
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }else {
            //计算权值
            int tempsmall = CallbackListenerImpl.initSmallMemorySize-CallbackListenerImpl.smallMemorySize;
            float small = CallbackListenerImpl.initSmallMemorySize/(float)(tempsmall<=0?1:tempsmall);
            int tempmedium = CallbackListenerImpl.initMediumMemorySize-CallbackListenerImpl.mediumMemorySize;
            float medium = CallbackListenerImpl.initMediumMemorySize/(float)(tempmedium<=0?1:tempmedium);
            int templarge = CallbackListenerImpl.initLargeMemorySize-CallbackListenerImpl.largeMemorySize;
            float large = CallbackListenerImpl.initLargeMemorySize/(float)(templarge<=0?1:templarge);
            if(CallbackListenerImpl.lowestSmallMemorySize!=null&&CallbackListenerImpl.smallMemorySize<=CallbackListenerImpl.lowestSmallMemorySize) {

                small = 0;
            }
            if(CallbackListenerImpl.lowestMediumMemorySize!=null&&CallbackListenerImpl.mediumMemorySize<=CallbackListenerImpl.lowestMediumMemorySize) {

                medium = 0;
            }
            if(CallbackListenerImpl.lowestLargeMemorySize!=null&&CallbackListenerImpl.largeMemorySize<=CallbackListenerImpl.lowestLargeMemorySize) {

                large = 0;
            }

            Float smallScope =  CallbackListenerImpl.initSmallMemorySize*small;
            Float mediumScope = CallbackListenerImpl.initMediumMemorySize*medium;
            Float largeScope = CallbackListenerImpl.initLargeMemorySize*large;

            System.out.println("small:"+small+",medium:"+medium+",large:"+large);
            System.out.println("smallScope:"+smallScope+",mediumScope:"+mediumScope+",largeScope:"+largeScope);

            int total = smallScope.intValue()+mediumScope.intValue()+largeScope.intValue();
            if(total==0) {
                return null;
            }
            int temp = ThreadLocalRandom.current().nextInt(total);
            int index = 0;
            if(temp<largeScope) {
                index =2;
            }else if(temp<largeScope+mediumScope) {
                index =1;
            }else {
                index = 0;
            }
            return invokers.get(index);
        }
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

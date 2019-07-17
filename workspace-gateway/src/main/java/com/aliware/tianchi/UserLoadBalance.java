package com.aliware.tianchi;

import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

    private int nstime = 50*1000000;//假设已知平均处理时间为500ms

    public static HashMap<Integer,Integer> maxCon  = new HashMap<>();//最大并发数。
    public static HashMap<Integer,AtomicInteger> statis = new HashMap<>();
    public static HashMap<Integer,Long> statisStartTime = new HashMap<>();

    private ReentrantLock smallLock = new ReentrantLock();
    private ReentrantLock mediumLock = new ReentrantLock();
    private ReentrantLock largeLock = new ReentrantLock();



    static {
        statis.put(20880,new AtomicInteger(0));
        statis.put(20870,new AtomicInteger(0));
        statis.put(20890,new AtomicInteger(0));

        maxCon.put(20880,200);
        maxCon.put(20870,450);
        maxCon.put(20890,650);
    }




    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int small = CallbackListenerImpl.smallMemorySize;
        int medium = CallbackListenerImpl.mediumMemorySize;
        int large = CallbackListenerImpl.largeMemorySize;
        if(small==0||medium==0||large==0) {
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }else {

            int temp  = ThreadLocalRandom.current().nextInt(small+medium+large);
            int index = 0;
            int port = 0;
            System.out.println(small+","+medium+","+large);

            if(temp<large) {
                index =2;
                port = 20890;
                int result =0 ;
                int num = 0;
                int size = CallbackListenerImpl.largeMemorySize;
                long startTime = statisStartTime.get(port);
                largeLock.lock();
                if((num=statis.get(port).incrementAndGet())<=size) {
                    System.out.println("直接通过请求,port:"+port);
                }else {
                    if(((System.nanoTime()-startTime)/nstime)*maxCon.get(port)>=(num-size)) {
                        System.out.println("对比速率通过,port:"+port);
                    }else {
                        statis.get(port).decrementAndGet();
                        System.out.println("对比速率决绝请求,port:"+port);
                        return null;
                    }
                }
                largeLock.unlock();


            }else if(temp<(large+medium)) {
                index =1;
                port = 20870;

                int result =0 ;
                int num = 0;
                int size = CallbackListenerImpl.mediumMemorySize;
                long startTime = statisStartTime.get(port);
                mediumLock.lock();
                if((num=statis.get(port).incrementAndGet())<=size) {
                    System.out.println("直接通过请求,port:"+port);
                }else {
                    if(((System.nanoTime()-startTime)/nstime)*maxCon.get(port)>=(num-size)) {
                        System.out.println("对比速率通过,port:"+port);
                    }else {
                        statis.get(port).decrementAndGet();
                        System.out.println("对比速率决绝请求,port:"+port);
                        return null;
                    }
                }
                mediumLock.unlock();

            }else {
                index = 0;
                port = 20880;
                int result =0 ;
                int num = 0;
                int size = CallbackListenerImpl.smallMemorySize;
                long startTime = statisStartTime.get(port);
                smallLock.lock();
                if((num=statis.get(port).incrementAndGet())<=size) {
                    System.out.println("直接通过请求,port:"+port);
                }else {
                    if(((System.nanoTime()-startTime)/nstime)*maxCon.get(port)>=(num-size)) {
                        System.out.println("对比速率通过,port:"+port);
                    }else {
                        statis.get(port).decrementAndGet();
                        System.out.println("对比速率决绝请求,port:"+port);
                        return null;
                    }
                }
                smallLock.unlock();

            }







            return invokers.get(index);
            /*//计算权值
            int tempsmall = CallbackListenerImpl.initSmallMemorySize-CallbackListenerImpl.smallMemorySize;
            float small = CallbackListenerImpl.initSmallMemorySize/(float)(tempsmall<=0?1:tempsmall);
            int tempmedium = CallbackListenerImpl.initMediumMemorySize-CallbackListenerImpl.mediumMemorySize;
            float medium = CallbackListenerImpl.initMediumMemorySize/(float)(tempmedium<=0?1:tempmedium);
            int templarge = CallbackListenerImpl.initLargeMemorySize-CallbackListenerImpl.largeMemorySize;
            float large = CallbackListenerImpl.initLargeMemorySize/(float)(templarge<=0?1:templarge);
            if(CallbackListenerImpl.lowestSmallMemorySize!=null&&CallbackListenerImpl.smallMemorySize<=CallbackListenerImpl.lowestSmallMemorySize) {

                small = 1;
            }
            if(CallbackListenerImpl.lowestMediumMemorySize!=null&&CallbackListenerImpl.mediumMemorySize<=CallbackListenerImpl.lowestMediumMemorySize) {

                medium = 1;
            }
            if(CallbackListenerImpl.lowestLargeMemorySize!=null&&CallbackListenerImpl.largeMemorySize<=CallbackListenerImpl.lowestLargeMemorySize) {

                large = 1;
            }

            Float smallScope =  CallbackListenerImpl.initSmallMemorySize*small;
            Float mediumScope = CallbackListenerImpl.initMediumMemorySize*medium;
            Float largeScope = CallbackListenerImpl.initLargeMemorySize*large;

            //System.out.println("small:"+small+",medium:"+medium+",large:"+large);
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
            return invokers.get(index);*/
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

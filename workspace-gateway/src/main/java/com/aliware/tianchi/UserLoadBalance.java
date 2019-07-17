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

    public static  int nstime = 400*1000000;//假设已知平均处理时间为500ms
    public static  int mstime = 400;//假设已知平均处理时间为500ms

    public static HashMap<Integer,Integer> maxCon  = new HashMap<>();//最大并发数。
    public static volatile HashMap<Integer,Integer> statis = new HashMap<>();
    public static HashMap<Integer,Long> statisStartTime = new HashMap<>();

    public static HashMap<Integer,Timer> timers = new HashMap<>();

    private ReentrantLock smallLock = new ReentrantLock();
    private ReentrantLock mediumLock = new ReentrantLock();
    private ReentrantLock largeLock = new ReentrantLock();

    public static  int smallCount = 0;
    public static  int mediumCount = 0;
    public static  int largeCount = 0;



    static {
        statis.put(20880,0);
        statis.put(20870,0);
        statis.put(20890,0);

        maxCon.put(20880,200);
        maxCon.put(20870,450);
        maxCon.put(20890,650);


    }




    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        if(true){

            int small = maxCon.get(20880)-(TestClientFilter.beforeMap.get(20880).get()-TestClientFilter.afterMap.get(20880).get());
            int medium = maxCon.get(20870)-(TestClientFilter.beforeMap.get(20870).get()-TestClientFilter.afterMap.get(20870).get());
            int large = maxCon.get(20890)-(TestClientFilter.beforeMap.get(20890).get()-TestClientFilter.afterMap.get(20890).get());

            int total = small+medium+large;
            if(total==0) {
                System.out.println("无资源，拒绝");
            }
            int temp = ThreadLocalRandom.current().nextInt(total);
            int index = 0;
            if(temp<small) {
                index = 0;
            }else if(temp<(small+medium)) {
                index = 1;
            }else  {
                index = 2;
            }

            return invokers.get(index);
        }

        long startTime = System.nanoTime();
        int small = CallbackListenerImpl.smallMemorySize;
        int medium = CallbackListenerImpl.mediumMemorySize;
        int large = CallbackListenerImpl.largeMemorySize;
        if(small==0||medium==0||large==0) {
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }else {
            if(true){
                System.out.println((System.nanoTime()-startTime)/1000000+"ms");
                return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
            }

            int temp  = ThreadLocalRandom.current().nextInt(small+medium+large);
            int index = 0;
            int port = 0;
            System.out.println(small+","+medium+","+large);

            if(temp<large) {
                index =2;
                port = 20890;
                int size = CallbackListenerImpl.largeMemorySize;
                int num = 0;
                if((largeCount++)<size) {
                    //CallbackListenerImpl.delayMap.get(port).put(new DelayedItem(nstime));
                    System.out.println("通过请求,port:"+port+",size:"+size+",num:"+num);
                }else {
                    System.out.println("拒绝");
                    return null;
                }


            }else if(temp<(large+medium)) {
                index =1;
                port = 20870;

                int size = CallbackListenerImpl.largeMemorySize;
                int num = 0;
                if((mediumCount++)<size) {
                   // CallbackListenerImpl.delayMap.get(port).put(new DelayedItem(nstime));
                    System.out.println("通过请求,port:"+port+",size:"+size+",num:"+num);
                }else {
                    System.out.println("拒绝");
                    return null;
                }
            }else {
                index = 0;
                port = 20880;
                int result =0 ;
                int num = 0;
                int size = CallbackListenerImpl.smallMemorySize;
                if((smallCount++)<size) {
                   // CallbackListenerImpl.delayMap.get(port).put(new DelayedItem(nstime));
                    System.out.println("通过请求,port:"+port+",size:"+size+",num:"+num);
                }else {
                    System.out.println("拒绝");
                    return null;
                }

            }


            System.out.println((System.nanoTime()-startTime)/1000000+"ms");


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


    class DelayedItem implements Delayed {

        private long liveTime ;
        private long removeTime;

        public DelayedItem(long liveTime){
            this.liveTime = liveTime;
            this.removeTime = liveTime + System.nanoTime();
        }
        @Override
        public int compareTo(Delayed o) {
            if (o == null) return 1;
            if (o == this) return  0;
            long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            return diff > 0 ? 1:diff == 0? 0:-1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(removeTime - System.nanoTime(), unit);
        }

    }



}

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

    public static volatile Map<Integer,Integer> serviceRates = new HashMap<>();//服务的速率，每秒的并发数

    private long timeDuration = 1000000000;//1000ms

    public static volatile HashMap<Integer,ConcurrentLinkedQueue<Long>> currentRate = null;//计算当前速率，1秒钟内请请求个数

    private static volatile HashMap<Integer,Long> startMap = null;

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static HashMap<Integer,Integer> maxCon = null;//各个服务的最大并发数。1秒钟
    public static HashMap<Integer,AtomicInteger> realCon = null;//各个服务的最近一秒钟的并发数


    private static int large = CallbackListenerImpl.largeMemorySize;
    private static int medium = CallbackListenerImpl.mediumMemorySize;
    private static int small = CallbackListenerImpl.smallMemorySize;
    private static int a = 0;
    private static int b = 0;
    static {


        currentRate = new HashMap<>();
        currentRate.put(20870,new ConcurrentLinkedQueue <>());
        currentRate.put(20880,new ConcurrentLinkedQueue <>());
        currentRate.put(20890,new ConcurrentLinkedQueue <>());

        startMap = new HashMap<>();
        startMap.put(20870,0l);
        startMap.put(20880,0l);
        startMap.put(20890,0l);


        maxCon = new HashMap<>();
        maxCon.put(20880,2770);
        maxCon.put(20870,6230);
        maxCon.put(20890,9000);

        /*maxCon.put(20880,307);
        maxCon.put(20870,692);
        maxCon.put(20890,1000);*/

        realCon = new HashMap<>();
        realCon.put(20880,new AtomicInteger(0));
        realCon.put(20870,new AtomicInteger(0));
        realCon.put(20890,new AtomicInteger(0));



        int c = large+medium+small;
        int temp = 0;
        if(large==0) {
            //初始化值，客户端还未收到服务端内存数据。
            temp = total/num;
            a = temp;
            b = a +(temp/3)*2;
        }else {
            //根据实时内存，分配虚拟槽
            temp = total/c;
            a =temp*large;
            b = a + (temp*medium);

        }

       /* serviceRates.put(20880,200);
        serviceRates.put(20870,450);
        serviceRates.put(20890,650);*/
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        //1.生成随机字符串key
        String key = (String)invocation.getArguments()[0];
        //2.获取key的hash值
        int hash = getCrc(key.getBytes())&total;
        //int hash = ThreadLocalRandom.current().nextInt(total);
        int index = 0;
        if(hash<=a) {
            index = 2;
            //System.out.println("large:"+largeNum.incrementAndGet());
        }else if(hash>a&&hash<=b){
            index = 1;
            //System.out.println("medium:"+mediumNum.incrementAndGet());

        }else {
            //System.out.println("small:"+smallNum.incrementAndGet());

        }


       Invoker invoker1 = invokers.get(index);
       int port = invoker1.getUrl().getPort();
       int[] array = new int[]{0,1,2};
       Integer finalPort = null;
       if(realCon.get(port).incrementAndGet()<=maxCon.get(port)) {
           //通过，直接请求
           finalPort = port;
           System.out.println("小于maxCon，直接请求:"+port);
       }else {
           System.out.println("寻找新途径");
           for(int i:array) {
               if(i==index) {
                   continue;
               }
               invoker1 = invokers.get(i);
               port = invoker1.getUrl().getPort();
               if(realCon.get(port).incrementAndGet()<=maxCon.get(port)) {
                   finalPort = port;
                   System.out.println("寻找到新途径:"+port);
                   break;
               }
           }
       }
       if(null!=finalPort) {
           return invoker1;
       }else {
           System.out.println("超出最大速率，拒绝请求");
           return null;
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


    class Task implements Runnable {

        int port;

        Task(int port) {
            this.port = port;
        }

        public void run() {
            startMap.put(port,currentRate.get(port).poll());
        }
    }

}

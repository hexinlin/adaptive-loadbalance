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


    private Integer[] indexs = new Integer[]{0,0,0,0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2};
    private static Integer[] indexs1 = new Integer[]{0,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2};
    private static Integer[] indexs2= new Integer[]{0,0,0,0,1,2,2,2,2,2,2,2,2,2,2,2,2,2};
    private static Integer[] indexs3= new Integer[]{0,0,0,0,1,1,1,1,1,1,1,1,1,2};
    private static Integer[] indexs4= new Integer[]{0,1,2,2,2,2,2,2,2,2,2,2,2,2,2};
    private static Integer[] indexs5= new Integer[]{0,1,1,1,1,1,1,1,1,1,2};
    private static Integer[] indexs6= new Integer[]{0,0,0,0,1,2};

    private static HashMap<Integer,Integer[]> indexMap = new HashMap<>();
    public static volatile HashMap<Integer,Byte> serviceStateMap = new HashMap<>();

    public static volatile ConcurrentHashMap<String,Byte> downMaps = new ConcurrentHashMap<>();

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

        maxCon.put(20880,260);
        maxCon.put(20870,600);
        maxCon.put(20890,840);

        realCon = new HashMap<>();
        realCon.put(20880,new AtomicInteger(0));
        realCon.put(20870,new AtomicInteger(0));
        realCon.put(20890,new AtomicInteger(0));





        serviceStateMap.put(20880,(byte)1);
        serviceStateMap.put(20870,(byte)1);
        serviceStateMap.put(20890,(byte)1);


       /* serviceRates.put(20880,200);
        serviceRates.put(20870,450);
        serviceRates.put(20890,650);*/
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
       Integer[] tempIndexs = null;
        if(serviceStateMap.get(20880)==1&&serviceStateMap.get(20870)==1&&serviceStateMap.get(20890)==1) {
            tempIndexs = indexs;
        }else if(serviceStateMap.get(20880)==0&&serviceStateMap.get(20870)==1&&serviceStateMap.get(20890)==1) {
            tempIndexs = indexs1;
        }else if(serviceStateMap.get(20880)==1&&serviceStateMap.get(20870)==0&&serviceStateMap.get(20890)==1) {
            tempIndexs = indexs2;
        }else if(serviceStateMap.get(20880)==1&&serviceStateMap.get(20870)==1&&serviceStateMap.get(20890)==0) {
            tempIndexs = indexs3;
        }else if(serviceStateMap.get(20880)==0&&serviceStateMap.get(20870)==0&&serviceStateMap.get(20890)==1) {
            tempIndexs = indexs4;
        }else if(serviceStateMap.get(20880)==0&&serviceStateMap.get(20870)==1&&serviceStateMap.get(20890)==0) {
            tempIndexs = indexs5;
        }else if(serviceStateMap.get(20880)==1&&serviceStateMap.get(20870)==0&&serviceStateMap.get(20890)==0) {
            tempIndexs = indexs6;
        }

        if(null==tempIndexs) {
            return null;
        }

       int index = tempIndexs[ThreadLocalRandom.current().nextInt(tempIndexs.length)];
       Invoker invoker1 = invokers.get(index);

       if(serviceStateMap.get(invoker1.getUrl().getPort())==0) {
           System.out.println("请求到宕机服务，检测状态");
           downMaps.put((String)invocation.getArguments()[0],(byte)0);
       }

       return invoker1;

      /* int port = invoker1.getUrl().getPort();
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
       }*/
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

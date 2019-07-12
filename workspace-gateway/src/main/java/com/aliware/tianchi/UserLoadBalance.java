package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
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

    private int total = 16383;
    private int num = 2;

    public static volatile Map<Integer,Integer> serviceRates = new HashMap<>();//服务的速率，每秒的并发数

    private long timeDuration = 1000000000;//1000ms

    public static volatile ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Long>> currentRate = null;//计算当前速率，1秒钟内请请求个数

    private static volatile ConcurrentHashMap<Integer,Long> startMap = null;
    static {

        currentRate = new ConcurrentHashMap<>();
        currentRate.put(20870,new ConcurrentLinkedQueue <>());
        currentRate.put(20880,new ConcurrentLinkedQueue <>());
        currentRate.put(20890,new ConcurrentLinkedQueue <>());

        startMap = new ConcurrentHashMap<>();
        startMap.put(20870,0l);
        startMap.put(20880,0l);
        startMap.put(20890,0l);

       /* serviceRates.put(20880,200);
        serviceRates.put(20870,450);
        serviceRates.put(20890,650);*/
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {


        long teststart = System.nanoTime();

        int large = CallbackListenerImpl.largeMemorySize;
        int medium = CallbackListenerImpl.mediumMemorySize;
        int small = CallbackListenerImpl.smallMemorySize;
        int a = 0;
        int b = 0;
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


       long start = System.nanoTime();
       Invoker invoker1 = invokers.get(index);
       int port = invoker1.getUrl().getPort();
       Integer finalPort = null;

       if(serviceRates.get(port)==null) {
           finalPort = port;
           ConcurrentLinkedQueue<Long> queue =  currentRate.get(port);
           if(startMap.get(port)==0) {
               queue.add(start);
               startMap.put(port,start);
           }else {
               if(start-startMap.get(port)<=timeDuration) {
                   queue.add(start);
               }else {
                   queue.add(start);

                   startMap.put(port,queue.poll());
               }
           }
           System.out.println("port:"+port+",速率还未计算出来,queueSize:"+queue.size());
       }else {
           int portMax = serviceRates.get(port);
           System.out.println("port:"+port+"速率为："+portMax+"/s");
           //有速率需要根据情况控制流速
           ConcurrentLinkedQueue<Long> queue =  currentRate.get(port);
           int queueSize = queue.size();
           if(queueSize<portMax) {
               //通过
               finalPort = port;
               queue.add(start);
               System.out.println("当前队列长度为："+queueSize+",通过");
           }else {
               if(start-startMap.get(port)>timeDuration) {
                   //通过
                   System.out.println("当前队列长度为："+queueSize+",start-queue.peek()>timeDuration,start:"+start+",queue-head:"+startMap.get(port)+",通过");
                   finalPort = port;
                   queue.add(start);

                   startMap.put(port,queue.poll());
               }else {
                   //被限流，寻找其他路径
                   //通过
                   System.out.println("当前队列长度为："+queueSize+",start-queue.peek()<timeDuration,start:"+start+",queue-head:"+startMap.get(port)+",寻找新途径");
                   Enumeration<Integer> enums  = currentRate.keys();
                   Integer kk = null;

                   while(enums.hasMoreElements()) {
                       if(null!=finalPort) {
                           break;
                       }
                       kk = enums.nextElement();
                       if(kk==port) {
                           continue;
                       }else {
                           ConcurrentLinkedQueue<Long> kkqueue =  currentRate.get(kk);
                           if(null==serviceRates.get(kk)||kkqueue.size()<serviceRates.get(kk)) {
                               //通过
                               finalPort = kk;
                               kkqueue.add(start);
                           }else {
                               if(start-startMap.get(port)>timeDuration) {
                                   //通过
                                   finalPort = kk;
                                   kkqueue.add(start);

                                  startMap.put(finalPort,kkqueue.poll());
                               }
                           }
                       }
                   }
               }
           }

       }

       if(port!=finalPort) {
           System.out.println("port："+port+"切换为："+finalPort);
           System.out.println("port:"+finalPort+",队列长度为："+currentRate.get(finalPort).size());
       }

       if(finalPort==null) {
           //拒绝请求
           System.out.println("拒绝请求");
           Enumeration<Integer> ee = currentRate.keys();
           int pp ;
           while(ee.hasMoreElements()) {
               pp = ee.nextElement();
               System.out.println("port:"+port+",num:"+currentRate.get(pp).size());
           }
           return null;
       }
       for(Invoker invoker:invokers) {
           if(invoker.getUrl().getPort()==finalPort) {
               port = finalPort;
               invoker1 = invoker;
           }
       }


        System.out.println((System.nanoTime()-teststart)/1000000+"ms");
        return invoker1;
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

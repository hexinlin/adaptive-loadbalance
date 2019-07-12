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

       /* boolean flag = false;
        ConcurrentHashMap invokerRates = TestClientFilter.invokerRates;
        if(invokerRates.size()<3) {
            flag = true;

        }else {
            //RTT速率已经生成控制请求速率
            Set< Map.Entry<String,Long>> set = invokerRates.entrySet();
            for(Map.Entry<String,Long> entry :set) {
                System.out.println("速率：key:"+entry.getKey()+",val:"+entry.getValue());
                serviceRate.put(entry.getKey(),1000000000/entry.getValue());
            }
        }
*/
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
        /*if(flag) {
            //还未获取到速率，直接请求
            return invoker1;
        }

        String k = invoker1.getUrl().getPort()+"";
        long start = System.nanoTime();
        String realKey = k;
        if(null==serviceNums.get(k)) {
            System.out.println("-----1-----");
            serviceNums.put(k,new ArrayList<>());
            serviceNums.get(k).add(start);
        }else {
            System.out.println("-----2-----");
            if(start-serviceNums.get(k).get(0)<=timeUnit) {
                //小于单位时间，计算是否超量
                int size = serviceNums.get(k).size();
                System.out.println("size:"+size+",serviceRate:"+serviceRate.get(k));
                if(size<serviceRate.get(k)) {
                    //成功请求，
                    serviceNums.get(k).add(start);
                }else {
                    realKey = null;
                    //超量了，寻找新途径，查看另外的invoker是否还有余量
                    Set<Map.Entry<String,List<Long>>> serviceNumsSet = serviceNums.entrySet();

                    for(Map.Entry<String,List<Long>> entry : serviceNumsSet) {
                        if(entry.getKey().equals(k)) {
                            //自身的容量已满，跳过
                            continue;
                        }else {
                            if(entry.getValue().size()<serviceRate.get(entry.getKey())) {
                                realKey = entry.getKey();
                                serviceNums.get(realKey).add(start);
                            }
                        }
                    }

                }
            }else {
                System.out.println("-----3-----");
                //大于时间段，清空列表，重启计数
                serviceNums.get(k).clear();
                serviceNums.get(k).add(start);
            }
        }

        if(null==realKey) {
            //如果容量都满了，直接驳回
            System.out.println("容量已满 ，无法请求成功");
            return null;
        }else if(realKey.equals(k)) {
            System.out.println("容量未满，直接请求成功");
            return invoker1;
        }else {
            for(Invoker invoker:invokers) {
                if(invoker.getUrl().getPort()==Integer.parseInt(realKey)) {
                    System.out.println("容量已满，更换invoker请求成功");
                    return invoker;
                }
            }
            return null;
        }*/

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
                   while(start-queue.peek()>timeDuration) {
                       queue.poll();
                   }
                   startMap.put(port,queue.peek());
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
                   while(start-queue.peek()>timeDuration) {
                       queue.poll();
                   }
                   startMap.put(port,queue.peek());
                   //startMap.put(port,queue.poll());
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
                                   while(start-kkqueue.peek()>timeDuration) {
                                       kkqueue.poll();
                                   }
                                   startMap.put(finalPort,kkqueue.peek());
                                  // startMap.put(finalPort,kkqueue.poll());
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




       /* ConcurrentLinkedQueue<Long> list = serviceSize.get(port);
       if(serviceSize.get(port).size()==0) {
           serviceSize.get(port).add(start);
       } else {
           Long[] array = new Long[list.size()];
           list.toArray(array);
           if(start-array[array.length-1]>=timeDuration) {
               //大于时间跨度，清空list，从新计数
               list.clear();
               list.add(start);
           }else {
               list.add(start);
           }


       }*/
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

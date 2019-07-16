package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.remoting.TimeoutException;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    private long rate = 0;//RTT速率

    public static volatile ConcurrentHashMap<String,Long> invokerRates = new ConcurrentHashMap<String,Long>();

    private volatile ConcurrentHashMap<String,List<RequestTime>> map = new ConcurrentHashMap<>();

    private long timeUnit = 1000000000;//1秒
    private long maxDuration = 0;
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
       /* try{
            String key = invoker.getUrl().getPort()+"";
            long start = 0;
            long duration = 0;
            boolean first = false;
            if(map.get(key)==null) {
                map.put(key,new ArrayList<RequestTime>());
                first = true;
            }
            start = System.nanoTime();
            Result result = invoker.invoke(invocation);
            duration = System.nanoTime()-start;
            System.out.println("result.value0:"+result.getValue());
            System.out.println("最大duration："+(maxDuration=Math.max(duration,maxDuration)));
            if(first) {
                map.get(key).add(new RequestTime(start,duration));
            }else {
                if(start-map.get(key).get(0).getRequestTime()>=timeUnit) {
                    //达到单位时间，计算平均响应时间
                    long sum = 0;
                    long size = map.get(key).size();
                    for(RequestTime request:map.get(key)) {
                        sum = sum +request.getRequestDuration();
                    }
                    invokerRates.put(key,sum/size);
                    map.get(key).clear();
                    map.get(key).add(new RequestTime(start,duration));
                }else {
                    map.get(key).add(new RequestTime(start,duration));
                }
            }
            return result;
        }catch (Exception e){
            System.out.println("clientFilter异常:"+e);
            throw e;
        }*/

       try {
           Result result = invoker.invoke(invocation);
           return result;
       }catch (Exception e) {
           throw e;
       }


    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if(result.hasException()) {
           if(!(result.getException() instanceof TimeoutException)) {
               String str = result.getException().toString();
               System.out.println("---"+str);
               //:20890!
               String[] temps = str.split(":");
               int port = Integer.parseInt(temps[temps.length-1].substring(0,temps[temps.length-1].length()-1));
               if(str.indexOf("EXHAUSTED")>-1) {
                  //分析内存情况
                   if(port==UserLoadBalance.smallPort) {
                       if(CallbackListenerImpl.lowestSmallMemorySize==null) {
                           int small = 0;
                           for(CallbackListenerImpl.MemoryNode node:CallbackListenerImpl.smallList) {
                               if(small==0) {
                                   small = node.getSize();
                               }else {
                                   small = small<node.getSize()?small:node.getSize();
                               }

                           }
                           CallbackListenerImpl.lowestSmallMemorySize=small;
                           CallbackListenerImpl.smallMemorySize = small;
                           System.out.println("small-init:"+CallbackListenerImpl.initSmallMemorySize+",small-lowest:"+CallbackListenerImpl.lowestSmallMemorySize);

                       }
                   }else if(port==UserLoadBalance.mediumPort) {
                       if(CallbackListenerImpl.lowestMediumMemorySize==null) {
                           int medium = 0;
                           for(CallbackListenerImpl.MemoryNode node:CallbackListenerImpl.mediumList) {
                               if(medium==0) {
                                   medium=node.getSize();
                               }else {
                                   medium = medium<node.getSize()?medium:node.getSize();
                               }

                           }
                           CallbackListenerImpl.lowestMediumMemorySize=medium;
                           CallbackListenerImpl.mediumMemorySize = medium;
                           System.out.println("medium-init:"+CallbackListenerImpl.initMediumMemorySize+",medium-lowest:"+CallbackListenerImpl.lowestMediumMemorySize);
                       }
                   }else {
                       if(CallbackListenerImpl.lowestLargeMemorySize==null) {
                           int large = 0;
                           for(CallbackListenerImpl.MemoryNode node:CallbackListenerImpl.largeList) {
                               if(large==0) {
                                   large = node.getSize();
                               }else {
                                   large = large<node.getSize()?large:node.getSize();
                               }

                           }
                           CallbackListenerImpl.lowestLargeMemorySize=large;
                           CallbackListenerImpl.largeMemorySize=large;
                           System.out.println("large-init:"+CallbackListenerImpl.initLargeMemorySize+",large-lowest:"+CallbackListenerImpl.lowestLargeMemorySize);

                       }
                   }


               }
           }


        }
        return result;
    }


    class RequestTime {
        private long requestTime;//发起请求的时间
        private long requestDuration;//请求的RTT

        RequestTime(long requestTime,long requestDuration) {
            this.requestTime = requestTime;
            this.requestDuration = requestDuration;
        }

        public long getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(long requestTime) {
            this.requestTime = requestTime;
        }

        public long getRequestDuration() {
            return requestDuration;
        }

        public void setRequestDuration(long requestDuration) {
            this.requestDuration = requestDuration;
        }
    }
}

package com.aliware.tianchi;

import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.transport.RequestLimiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 服务端限流
 * 可选接口
 * 在提交给后端线程池之前的扩展，可以用于服务端控制拒绝请求
 */
public class TestRequestLimiter implements RequestLimiter {

    private volatile int maxConcurrency = 0;
    private final long time = 5000000000l;//统计时间段
    private volatile long start = 0;

    /**
     * @param request 服务请求
     * @param activeTaskCount 服务端对应线程池的活跃线程数
     * @return  false 不提交给服务端业务线程池直接返回，客户端可以在 Filter 中捕获 RpcException
     *          true 不限流
     */
    @Override
    public boolean tryAcquire(Request request, int activeTaskCount) {
       /* if(start==0) {
            start = System.nanoTime();
            if(activeTaskCount<20) {
                start = 0;
            }
        }else {
            if(start>0) {
                if(System.nanoTime()-start<time) {
                   // System.out.println("activeTaskCount:"+activeTaskCount);
                    maxConcurrency = Math.max(maxConcurrency,activeTaskCount);
                    return true;
                }else {
                    System.out.println("maxConcurrency:"+maxConcurrency);
                    if(activeTaskCount<maxConcurrency) {
                        return true;
                    }else {
                        return false;
                    }
                }
            }
        }


       return true;*/


       return true;

    }


}

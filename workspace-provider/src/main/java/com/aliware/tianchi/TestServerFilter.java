package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
   /* private long maxRTT = 500000000;//设定MAXRTT为0.5S,单位纳秒
    private List<Long> requests = new ArrayList<Long>(1000);
    private long timeUnit = 0;//单位时间，纳秒
    public static volatile long  maxConcurrency = 0;//单位时间内的最大并发量
    public static volatile byte flag = 1;
    public static volatile AtomicInteger num0 = new AtomicInteger(0);
    public static volatile AtomicInteger num1 = new AtomicInteger(0);
    public static long initMemory = 0;*/
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{

            long start = System.nanoTime();
            Result result = invoker.invoke(invocation);

            long duration = System.nanoTime()-start;
            //requests.add(start);
            //System.out.println("duration:"+duration);

            /* if(result!=null) {
                System.out.println("result:"+(null==result.getException()?"":result.getException().toString())+","+result.getValue()+","+result.getAttachments().toString());
            }*/
            return result;
        }catch (Exception e){
            System.out.println("ServerFilter:"+e);
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}

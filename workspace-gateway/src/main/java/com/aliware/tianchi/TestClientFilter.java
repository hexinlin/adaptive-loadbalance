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
import java.util.concurrent.*;
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

    public static HashMap<Integer,AtomicInteger[]> beforeMap = new HashMap<>();
    public static HashMap<Integer,AtomicInteger[]> afterMap = new HashMap<>();
    static {
        beforeMap.put(20880,new AtomicInteger[4]);
        beforeMap.put(20870,new AtomicInteger[8]);
        beforeMap.put(20890,new AtomicInteger[12]);

        afterMap.put(20880,new AtomicInteger[4]);
        afterMap.put(20870,new AtomicInteger[8]);
        afterMap.put(20890,new AtomicInteger[12]);
    }


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
       try {

           int port = invoker.getUrl().getPort();
           beforeMap.get(port)[ThreadLocalRandom.current().nextInt(beforeMap.get(port).length)].incrementAndGet();
           Result result = invoker.invoke(invocation);
           return result;
       }catch (Exception e) {
           throw e;
       }


    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        int port = invoker.getUrl().getPort();
        afterMap.get(port)[ThreadLocalRandom.current().nextInt(afterMap.get(port).length)].incrementAndGet();
        if(result.hasException()) {
           if(!(result.getException() instanceof TimeoutException)) {
               String str = result.getException().toString();
               System.out.println("---"+str);
           }


        }
        return result;
    }



}

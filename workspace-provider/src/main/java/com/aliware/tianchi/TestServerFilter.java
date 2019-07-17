package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.ArrayList;
import java.util.HashMap;
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


    public static volatile AtomicInteger num0 = new AtomicInteger(0);
    public static volatile AtomicInteger num1 = new AtomicInteger(0);

    public static volatile HashMap<Integer,AtomicInteger> statis = new HashMap<>();

    static {
        statis.put(20880,new AtomicInteger(0));
        statis.put(20870,new AtomicInteger(0));
        statis.put(20890,new AtomicInteger(0));
    }




    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{

           // statis.get(invoker.getUrl().getPort()).incrementAndGet();
           // long start = System.nanoTime();
            Result result = invoker.invoke(invocation);

           // long duration = System.nanoTime()-start;
            //requests.add(start);
           // System.out.println("duration:"+duration);

            /* if(result!=null) {
                System.out.println("result:"+(null==result.getException()?"":result.getException().toString())+","+result.getValue()+","+result.getAttachments().toString());
            }*/
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
       // statis.get(invoker.getUrl().getPort()).decrementAndGet();
        if(result.hasException()) {
            System.out.println("server:"+result.getException().toString());
        }
        return result;
    }

}

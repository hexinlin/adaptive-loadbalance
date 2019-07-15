package com.aliware.tianchi;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
//@Service(timeout = 10000)
public class CallbackListenerImpl implements CallbackListener {

    public static int smallMemorySize = 0;//单位M
    public static int mediumMemorySize = 0;//单位M
    public static int largeMemorySize = 0;//单位M
    @Override
    public void receiveServerMsg(String msg) {
        String msgs [] = msg.split(",");
       /* if("small".equals(msgs[0])) {
            smallMemorySize = Integer.parseInt(msgs[1]);
        }else if("medium".equals(msgs[0])){
            mediumMemorySize = Integer.parseInt(msgs[1]);
        }else if("large".equals(msgs[0])) {
            largeMemorySize = Integer.parseInt(msgs[1]);
        }*/
      //  System.err.println("receive msg from server :" + msg);
        Set<Map.Entry<Integer,AtomicInteger>> set = UserLoadBalance.realCon.entrySet();
        for(Map.Entry<Integer,AtomicInteger> entry:set) {
            entry.getValue().set(0);
        }
        //System.err.println("receive msg from server :" + msg);
    }

}

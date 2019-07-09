package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    public static int smallMemorySize = 0;//单位M
    public static int mediumMemorySize = 0;//单位M
    public static int largeMemorySize = 0;//单位M
    @Override
    public void receiveServerMsg(String msg) {
        String msgs [] = msg.split(",");
        if("small".equals(msgs[0])) {
            smallMemorySize = Integer.parseInt(msgs[1]);
        }else if("medium".equals(msgs[0])){
            mediumMemorySize = Integer.parseInt(msgs[1]);
        }else if("large".equals(msgs[0])) {
            largeMemorySize = Integer.parseInt(msgs[1]);
        }
       // System.out.println("receive msg from server :" + msg);
    }

}

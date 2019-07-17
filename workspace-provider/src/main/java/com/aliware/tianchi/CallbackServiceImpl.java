package com.aliware.tianchi;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
//@Service(timeout = 10000)
public class CallbackServiceImpl implements CallbackService {



    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!listeners.isEmpty()) {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            //entry.getValue().receiveServerMsg(System.getProperty("quota") + " " + new Date().toString());
                           // entry.getValue().receiveServerMsg(System.getProperty("quota") + "," + (Runtime.getRuntime().freeMemory()>>20));
                            String quota = System.getProperty("quota");
                            int port = 0;
                            if("small".equals(quota)) {
                                port = 20880;
                            }else if("medium".equals(quota)) {
                                port = 20870;
                            }else {
                                port = 20890;
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append(quota).append(",").append((Runtime.getRuntime().freeMemory()>>20)).append(",").append(0);
                            entry.getValue().receiveServerMsg(sb.toString());
                        } catch (Throwable t1) {
                            //System.out.println("异常了"+t1.toString());
                            //listeners.remove(entry.getKey());
                        }
                    }
                }
            }
        }, 0, 5000);
    }

    private Timer timer = new Timer();

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        //listener.receiveServerMsg(new Date().toString()); // send notification for change
        String quota = System.getProperty("quota");
        int port = 0;
        if("small".equals(quota)) {
            port = 20880;
        }else if("medium".equals(quota)) {
            port = 20870;
        }else {
            port = 20890;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(quota).append(",").append((Runtime.getRuntime().freeMemory()>>20)).append(",").append("0");
        listener.receiveServerMsg(sb.toString());
    }
}

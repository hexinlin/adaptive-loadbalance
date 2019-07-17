package com.aliware.tianchi;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.listener.CallbackListener;

import javax.jws.soap.SOAPBinding;
import java.util.*;
import java.util.concurrent.*;
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


    public static volatile int initSmallMemorySize = 0;//单位M
    public static volatile int initMediumMemorySize = 0;//单位M
    public static volatile int initLargeMemorySize = 0;//单位M


    public static volatile Integer lowestSmallMemorySize = null;//单位M
    public static volatile Integer lowestMediumMemorySize = null;//单位M
    public static volatile Integer lowestLargeMemorySize = null;//单位M

    public static volatile int smallMemorySize = 0;//单位M
    public static volatile int mediumMemorySize = 0;//单位M
    public static volatile int largeMemorySize = 0;//单位M

    public static HashMap<Integer,DelayQueue> delayMap = new HashMap<>();

    public static final int baseSize = 10000;
    private int port = 0;


    static {
        delayMap.put(20880,new DelayQueue());
        delayMap.put(20870,new DelayQueue());
        delayMap.put(20890,new DelayQueue());

   /*   new Thread(new Runnable() {
            @Override
            public void run() {
                DelayQueue delayQueue = delayMap.get(20880);
                while(delayQueue.poll()!=null) {
                    System.out.println("smallMemorySize++");
                    smallMemorySize++;
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DelayQueue delayQueue = delayMap.get(20870);
                while(delayQueue.poll()!=null) {
                    System.out.println("mediumMemorySize++");
                    mediumMemorySize++;
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DelayQueue delayQueue = delayMap.get(20890);
                while(delayQueue.poll()!=null) {
                    System.out.println("largeMemorySize++");
                    largeMemorySize++;
                }
            }
        }).start();*/
    }


    @Override
    public void receiveServerMsg(String msg) {
       /* String msgs [] = msg.split(",");
        long time = System.nanoTime();
        System.out.println(msg);
        int current = 0;

       if("small".equals(msgs[0])) {
            //smallMemorySize = Integer.parseInt(msgs[1]);
           port = 20880;
           current = Integer.parseInt(msgs[2]);
           smallMemorySize = UserLoadBalance.maxCon.get(port)-current;
           //delayMap.get(port).clear();
           for(int i=0;i<current;i++) {
               //delayMap.get(port).put(new DelayedItem(UserLoadBalance.nstime));
           }
           UserLoadBalance.smallCount=0;


        }else if("medium".equals(msgs[0])){
            //mediumMemorySize = Integer.parseInt(msgs[1]);
           port = 20870;
           current = Integer.parseInt(msgs[2]);
           mediumMemorySize = 450-Integer.parseInt(msgs[2]);
          // delayMap.get(port).clear();
           for(int i=0;i<current;i++) {
              // delayMap.get(port).put(new DelayedItem(UserLoadBalance.nstime));
           }
           UserLoadBalance.mediumCount=0;

        }else if("large".equals(msgs[0])) {
           port = 20890;
           current = Integer.parseInt(msgs[2]);
           largeMemorySize = 650-Integer.parseInt(msgs[2]);
           //delayMap.get(port).clear();
           for(int i=0;i<current;i++) {
               //delayMap.get(port).put(new DelayedItem(UserLoadBalance.nstime));
           }
           UserLoadBalance.largeCount=0;

        }*/



    }

    class DelayedItem implements Delayed {

        private long liveTime ;
        private long removeTime;

        public DelayedItem(long liveTime){
            this.liveTime = liveTime;
            this.removeTime = liveTime + System.nanoTime();
        }
        @Override
        public int compareTo(Delayed o) {
            if (o == null) return 1;
            if (o == this) return  0;
            long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            return diff > 0 ? 1:diff == 0? 0:-1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(removeTime - System.nanoTime(), unit);
        }

    }




}

package com.aliware.tianchi;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.LinkedList;
import java.util.List;
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


    public static volatile int initSmallMemorySize = 0;//单位M
    public static volatile int initMediumMemorySize = 0;//单位M
    public static volatile int initLargeMemorySize = 0;//单位M


    public static volatile Integer lowestSmallMemorySize = null;//单位M
    public static volatile Integer lowestMediumMemorySize = null;//单位M
    public static volatile Integer lowestLargeMemorySize = null;//单位M

    public static volatile int smallMemorySize = 0;//单位M
    public static volatile int mediumMemorySize = 0;//单位M
    public static volatile int largeMemorySize = 0;//单位M

    public static volatile LinkedList<MemoryNode> smallList = new LinkedList();
    public static volatile LinkedList<MemoryNode> mediumList = new LinkedList();
    public static volatile LinkedList<MemoryNode> largeList = new LinkedList();

    public static final int baseSize = 10000;

    @Override
    public void receiveServerMsg(String msg) {
        String msgs [] = msg.split(",");
        long time = System.nanoTime();
        System.out.println(msg);
       if("small".equals(msgs[0])) {
            //smallMemorySize = Integer.parseInt(msgs[1]);
           smallMemorySize = 200-Integer.parseInt(msgs[2]);
           UserLoadBalance.statis.get(20880).set(0);
           UserLoadBalance.statisStartTime.put(20880,time);
            /*if(lowestSmallMemorySize==null) {
                initSmallMemorySize = Math.max(initSmallMemorySize,smallMemorySize);
                smallList.add(new MemoryNode(time,smallMemorySize));
            }*/
        }else if("medium".equals(msgs[0])){
            //mediumMemorySize = Integer.parseInt(msgs[1]);
            mediumMemorySize = 450-Integer.parseInt(msgs[2]);
           UserLoadBalance.statis.get(20870).set(0);
           UserLoadBalance.statisStartTime.put(20870,time);
          /* if(lowestMediumMemorySize==null) {
               initMediumMemorySize = Math.max(initMediumMemorySize,mediumMemorySize);
               mediumList.add(new MemoryNode(time,mediumMemorySize));
           }*/
        }else if("large".equals(msgs[0])) {
            //largeMemorySize = Integer.parseInt(msgs[1]);
            largeMemorySize = 650-Integer.parseInt(msgs[2]);
           UserLoadBalance.statis.get(20890).set(0);
           UserLoadBalance.statisStartTime.put(20890,time);
          /* if(lowestLargeMemorySize==null) {
               initLargeMemorySize = Math.max(initLargeMemorySize,largeMemorySize);
               largeList.add(new MemoryNode(time,largeMemorySize));
           }*/
        }
    }




    class MemoryNode {
        private long time;//时间
        private int size;//内存余量大小

        MemoryNode(long time,int size) {
            this.time = time;
            this.size = size;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}

package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent implements Runnable {
    public static final String FLAG_THREAD_1 = "1";
    public static final String FLAG_THREAD_2 = "2";
    public static final String FLAG_THREAD_3 = "3";
    public static final String FLAG_THREAD_4 = "4";
    private int agentId;
    private int currentPosition;
    private int targetPosition;
    private int nextPosition;
    private Map map;
    private Message message;
    private ReentrantLock lock;
    private Condition condition1;
    private Condition condition2;
    private Condition condition3;
    private Condition condition4;
    private boolean isMainThread;


    public Agent(int agentId, int currentPosition, int targetPosition, Map map, Message message, ReentrantLock lock, Condition condition1, Condition condition2, Condition condition3, Condition condition4) {
        this.lock = lock;
        this.condition1 = condition1;
        this.condition2 = condition2;
        this.condition3 = condition3;
        this.condition4 = condition4;
        this.agentId = agentId;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
        this.map = map;
        this.message = message;
        this.isMainThread = false;
    }
    private void stopCurrentThread() throws InterruptedException {
        switch (Thread.currentThread().getName()) {
            case FLAG_THREAD_1 -> {
                condition1.await();
            }
            case FLAG_THREAD_2 -> {
                condition2.await();
            }
            case FLAG_THREAD_3 -> {
                condition3.await();
            }
            case FLAG_THREAD_4 -> {
                condition4.await();
            }
        }
    }
    private void changeThread(int receiverId) throws InterruptedException {
        switch (receiverId){
            case 1->{
                stopCurrentThread();
                condition1.signal();
            }
            case 2->{
                stopCurrentThread();
                condition2.signal();
            }
            case 3->{
                stopCurrentThread();
                condition3.signal();
            }
            case 4->{
                stopCurrentThread();
                condition4.signal();
            }
        }

    }

    // 线程的run方法，执行代理逻辑
    @Override
    public void run() {
        while (true) {
            try {
                lock.lock();
                System.out.println("Agent"+ Thread.currentThread().getName() + "开始行动");
                isMainThread = true;
                Thread.sleep(1000);
                // todo 在这里实现代理的逻辑，例如移动和与其他代理交互
                if (!message.isRead()){
                    System.out.println("Agent"+ Thread.currentThread().getName() + "收到消息：" + message.getContent());
                    message.setRead(true);
                }
                int mainAgentId = 0;
                if (!isMainThread){
                    mainAgentId = message.getSenderId();
                }
                message.setMessage(agentId, 2,"hello agent2", false);
                int receiverId = message.getReceiverId();
                changeThread(receiverId);
                if (!isMainThread){
                    changeThread(mainAgentId);
                }
                // 该回合结束，唤醒下一个线程
                switch (Thread.currentThread().getName()) {
                    case FLAG_THREAD_1 -> {
                        //唤醒线程2 自身线程挂起阻塞
                        isMainThread = false;
                        condition2.signal();
                        condition1.await();
                    }
                    case FLAG_THREAD_2 -> {
                        //唤醒线程3 自身线程挂起阻塞
                        isMainThread = false;
                        condition3.signal();
                        condition2.await();
                    }
                    case FLAG_THREAD_3 -> {
                        //唤醒线程4 自身线程挂起阻塞
                        isMainThread = false;
                        condition4.signal();
                        condition3.await();
                    }
                    case FLAG_THREAD_4 -> {
                        //唤醒线程1 自身线程挂起阻塞
                        isMainThread = false;
                        condition1.signal();
                        condition4.await();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                lock.unlock();
            }
        }


    }

    // 示例方法：获取地图信息
    public int getMapValue(int index) {
        return map.get(index);
    }

    // 示例方法：设置地图信息
    public void setMapValue(int index, int value) {
        map.set(index, value);
    }

    // 其他方法，例如发送请求、接收请求、移动等
    public void sendRequest(int targetId, String message) {

    }

    public void receiveRequest() {
    }

    public String receiveResponse() {
        return null;
    }

    public void processResponse() {
    }

    public void move() {

    }
}



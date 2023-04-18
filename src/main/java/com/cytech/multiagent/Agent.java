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
    private boolean initFlag;


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
        this.initFlag = true;
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
        switch (receiverId) {
            case 1 -> {
                condition1.signal();
                stopCurrentThread();

            }
            case 2 -> {
                condition2.signal();
                stopCurrentThread();

            }
            case 3 -> {
                condition3.signal();
                stopCurrentThread();

            }
            case 4 -> {
                condition4.signal();
                stopCurrentThread();

            }
        }

    }

    private void sequenceRun() throws InterruptedException {
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
    }

    // 线程的run方法，执行代理逻辑
    @Override
    public void run() {
        while (true) {
            try {
                lock.lock();
                Thread.sleep(1000);
                if (!initFlag) {
                    isMainThread = true;
                    int receiverId = 0;
                    System.out.println("Agent" + Thread.currentThread().getName() + "开始行动");
                    // todo 在这里实现代理的逻辑，例如移动和与其他代理交互
                    if (!message.isRead() && message.getReceiverId() == agentId) {
                        System.out.println("Agent" + Thread.currentThread().getName() + "收到消息：" + message.getContent());
                        message.setRead(true);
                    }
                    int mainAgentId = message.getReceiverId();
                    if (!isMainThread) {
                        mainAgentId = message.getSenderId();
                    }
                    if (message.isRead()) {

                        receiverId = agentId + 2;
                        receiverId = receiverId > 4 ? receiverId - 4 : receiverId;
                        message.setMessage(agentId,receiverId , "hello agent"+ receiverId, false);
                        System.out.println("Agent" + Thread.currentThread().getName() + "发送消息：" + message.getContent());
                    }
                    if (receiverId != agentId) {
                        changeThread(receiverId);
                        if (!isMainThread) {
                            changeThread(mainAgentId);
                        }
                    }
                    // 该回合结束，唤醒下一个线程

                    sequenceRun();
                } else {
                    System.out.println("Agent" + Thread.currentThread().getName() + "初始化完成");
                    initFlag = false;
                    switch (Thread.currentThread().getName()) {
                        case FLAG_THREAD_2 -> {
                            isMainThread = false;
                            condition2.await();
                        }
                        case FLAG_THREAD_3 -> {
                            //唤醒线程4 自身线程挂起阻塞
                            isMainThread = false;
                            condition3.await();
                        }
                        case FLAG_THREAD_4 -> {
                            //唤醒线程1 自身线程挂起阻塞
                            isMainThread = false;
                            condition4.await();
                        }
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




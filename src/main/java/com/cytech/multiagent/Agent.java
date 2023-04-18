package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private GameMap map;
    private Message message;
    private ReentrantLock lock;
    private Condition condition1;
    private Condition condition2;
    private Condition condition3;
    private Condition condition4;
    private boolean initFlag;
    private int[] requestAgents;
    private Map<Integer, String> agentResponse;
    private AgentStatus agentStatus;
    private volatile boolean running = true;


    public Agent(int agentId, int currentPosition, int targetPosition, GameMap map, Message message, AgentStatus agentStatus, ReentrantLock lock, Condition condition1, Condition condition2, Condition condition3, Condition condition4) {
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
        this.agentStatus = agentStatus;
        this.initFlag = true;
        this.requestAgents = new int[]{0, 0, 0, 0};
        this.agentResponse = new HashMap<>();
        agentResponse.put(1, "ALLOW_MOVE");
        agentResponse.put(2, "ALLOW_MOVE");
        agentResponse.put(3, "ALLOW_MOVE");
        agentResponse.put(4, "ALLOW_MOVE");
    }

    private void updateResponse(int agentId, String response) {
        agentResponse.put(agentId, response);
    }

    private void stopCurrentThread() throws InterruptedException {
        System.out.println("Agent " + agentId + " stop current thread");
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
        System.out.println("Agent " + agentId + " change thread to " + receiverId);
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
        int nextAgent = switch (Thread.currentThread().getName()) {
            case FLAG_THREAD_1 -> agentStatus.getAgentStatus(2) == 1 ? (agentStatus.getAgentStatus(3) == 1 ? (agentStatus.getAgentStatus(4) == 1 ? 1 : 4) : 3) : 2;
            case FLAG_THREAD_2 -> agentStatus.getAgentStatus(3) == 1 ? (agentStatus.getAgentStatus(4) == 1 ? (agentStatus.getAgentStatus(1) == 1 ? 2 : 1) : 4) : 3;
            case FLAG_THREAD_3 -> agentStatus.getAgentStatus(4) == 1 ? (agentStatus.getAgentStatus(1) == 1 ? (agentStatus.getAgentStatus(2) == 1 ? 3 : 2) : 1) : 4;
            case FLAG_THREAD_4 -> agentStatus.getAgentStatus(1) == 1 ? (agentStatus.getAgentStatus(2) == 1 ? (agentStatus.getAgentStatus(3) == 1 ? 4 : 3) : 2) : 1;
            default -> throw new IllegalStateException("Invalid thread name");
        };

        if ( nextAgent != agentId) {
            agentStatus.setMainAgentId(nextAgent);
            getConditionByAgentId(nextAgent).signal();
            getCurrentCondition().await();
        }
    }

    private Condition getConditionByAgentId(int agentId) {
        return switch (agentId) {
            case 1 -> condition1;
            case 2 -> condition2;
            case 3 -> condition3;
            case 4 -> condition4;
            default -> throw new IllegalArgumentException("Invalid agent ID");
        };
    }

    private Condition getCurrentCondition() {
        return getConditionByAgentId(Integer.parseInt(Thread.currentThread().getName()));
    }


    // 线程的run方法，执行代理逻辑
        @Override
        public void run () {
            while (running) {
                try {
                    lock.lock();
                    Thread.sleep(1000);
                    if (!initFlag) {
                        if (currentPosition == targetPosition) {
                            if (agentStatus.getAgentStatus(agentId) == 1) {
                                System.out.println("Agent" + Thread.currentThread().getName() + "已经到达目标位置，线程结束");
                                stopThread();
                            }
                        } else {
                            if (agentStatus.getMainAgentId() == agentId) {
                                System.out.println("----------------------------------------");
                                System.out.println("Agent" + Thread.currentThread().getName() + "开始行动");
                                move();
                                // 该回合结束，唤醒下一个线程
                                System.out.println("Agent" + Thread.currentThread().getName() + "行动结束");
                                sequenceRun();
                            } else {
                                receiveRequest();
                            }
                        }
                    } else {
                        System.out.println("Agent" + Thread.currentThread().getName() + "初始化完成 " + "当前位置：" + currentPosition + " 目标位置：" + targetPosition);
                        initFlag = false;
                        switch (Thread.currentThread().getName()) {
                            case FLAG_THREAD_2 -> condition2.await();
                            case FLAG_THREAD_3 -> condition3.await();
                            case FLAG_THREAD_4 -> condition4.await();
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

        private int[] direction () {
            //根据当前位置和目标位置，计算可能行动的位置
            int[] direction = {-1, -1, -1, -1};
            //归零，对应 上 下 左 右

            int positionX = currentPosition % 5;
            int positionY = currentPosition / 5;
            int targetPositionX = targetPosition % 5;
            int targetPositionY = targetPosition / 5;
            //计算当前和目标位置的行和列

            //当前与目标位置是否在同一行
            if (targetPositionY - positionY != 0) {
                if (targetPositionY > positionY) {
                    //目标位置的行数大于当前位置的行数，则可以下移
                    direction[1] = currentPosition + 5;
                } else {
                    //上移
                    direction[0] = currentPosition - 5;
                }
            }
            if (targetPositionX - positionX != 0) {
                if (targetPositionX > positionX) {
                    //右移
                    direction[3] = currentPosition + 1;
                } else {
                    //左移
                    direction[2] = currentPosition - 1;
                }
            }
            return direction;
        }

        // 其他方法，例如发送请求、接收请求、移动等
        public void sendRequest ( int receiverId, int senderPosition) throws InterruptedException {
            message.setMessage(agentId, receiverId, "REQUEST_MOVE " + senderPosition, MessageTypeEnum.REQUEST, false);
            changeThread(receiverId);
        }

        public void receiveRequest () throws InterruptedException {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.REQUEST) {
                System.out.println("Agent" + Thread.currentThread().getName() + "收到请求：" + message.getContent());
                message.setRead(true);
                handleRequest();
            }
        }

        public boolean move () throws InterruptedException {
            //得到可能移动的位置
            int[] direction = this.direction();
            //初始化可能请求的agence

            for (int i = 0; i < 4; i++) {
                //不为-1则可能移动
                if (direction[i] != -1) {
                    //该位置没有棋子，则直接移动返回true
                    if (map.get(direction[i]) == 0) {
                        move(agentId, direction[i]);
                        return true;
                    } else {
                        //有棋子阻挡且没有拒绝让路，则记录
                        if (requestAgents[i] != -1) {
                            requestAgents[i] = map.get(direction[i]);
                        }
                    }
                }
            }
            //在函数没有结束的情况下，向所有可能代理发送请求
            for (int i = 0; i < 4; i++) {
                if (requestAgents[i] != 0 && agentStatus.getAgentStatus(requestAgents[i]) == 0 && !Objects.equals(agentResponse.get(requestAgents[i]), "REFUSE_MOVE")) {
                    System.out.println("Agent" + Thread.currentThread().getName() + "向Agent" + requestAgents[i] + "发送让路请求");
                    sendRequest(requestAgents[i], currentPosition);//等待消息，交由handrequest控制
                    receiveResponse();
                } else if (i == 3) {
                    System.out.println("Agent" + Thread.currentThread().getName() + " 动不了，死循环，寄！");
                }
            }
            return false;
        }

        private void move ( int agentId, int position){
            map.set(currentPosition, 0);
            map.set(position, agentId);
            System.out.println("Agent" + Thread.currentThread().getName() + " 从 " + currentPosition + " 移动到 " + position);
            map.printMap();
            currentPosition = position;
            if (currentPosition == targetPosition) {
                agentStatus.setAgentStatus(agentId, 1);
                System.out.println("Agent" + Thread.currentThread().getName() + "已经到达目标位置, 线程结束");
                stopThread();
            }
        }

        private boolean handleRequest () throws InterruptedException {
            //从消息中获得棋子id和其位置
            int senderPosition = 0;
            String message = this.message.getContent();
            if (message.startsWith("REQUEST_MOVE")) {
                String[] messageArray = message.split(" ");
                senderPosition = Integer.parseInt(messageArray[1]);
            }
            int[] requestAgents = {0, 0, 0, 0};
            int[] direction = direction();
            //第一种情况，让路的路径恰巧也在移动方向上且没有被阻塞，则允许让路
            for (int i = 0; i < 4; i++) {
                if (direction[i] < 0 || direction[i] > 24) {
                    if (i == 3) {
                        sendResponse(this.message.getSenderId(), "REFUSE_MOVE");
                        return true;
                    }
                } else if (direction[i] != 0 && direction[i] != senderPosition) {
                    if (map.get(direction[i]) == 0) {
                        move(agentId, direction[i]);
                        sendResponse(this.message.getSenderId(), "ALLOW_MOVE");
                        return true;
                    }
                } else if (direction[i] != senderPosition) {
                    //如果被阻塞,且阻塞棋子不是请求棋子,则记录该棋子id
                    requestAgents[i] = map.get(direction[i]);
                }
            }
            int req = 0;
            for (int i = 0; i < 4; i++) {
                if (requestAgents[i] != 0) {
                    req = 1;
                    sendRequest(requestAgents[i], currentPosition);// 请求，等待，交由handleRequest处理
                    receiveResponse();
                    if (agentResponse.get(requestAgents[i]).equals("ALLOW_MOVE")) {
                        return true;
                    }
                }
            }
            if (req == 0) {
                sendResponse(this.message.getSenderId(), "REFUSE_MOVE");// 不允许，说明唯一的阻塞棋子也是请求棋子，那么就不允许移动
            }
            return true;
        }

        private void sendResponse ( int receiverId, String content) throws InterruptedException {
            message.setMessage(agentId, receiverId, content, MessageTypeEnum.RESPONSE, false);
            changeThread(receiverId);
        }

        private void receiveResponse () throws InterruptedException {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.RESPONSE) {
                System.out.println("Agent" + Thread.currentThread().getName() + "收到回复：" + message.getContent());
                message.setRead(true);
                handleResponse(message.getContent());
            }
        }

        private void handleResponse (String content) throws InterruptedException {
            updateResponse(message.getSenderId(), content);
            move();
        }

        private void stopThread () {
            running = false;
        }
    }
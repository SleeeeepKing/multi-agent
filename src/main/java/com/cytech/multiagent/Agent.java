package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent extends Thread {
    private int agentId;
    private int currentPosition;
    private int targetPosition;
    private int nextPosition;
    private GameMap map;
    private MessageList messageList;
    private boolean initFlag;
    private int[] requestAgents;
    private Map<Integer, String> agentResponse;
    private AgentStatus agentStatus;
    private int lastPosition;
    private volatile boolean running = true;


    public Agent(int agentId, int currentPosition, int targetPosition, GameMap map, MessageList messageList, AgentStatus agentStatus) {
        this.agentId = agentId;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
        this.map = map;
        this.messageList = messageList;
        this.agentStatus = agentStatus;
        this.initFlag = true;
        this.requestAgents = new int[]{0, 0, 0, 0};
        this.agentResponse = new HashMap<>();
        agentResponse.put(1, "");
        agentResponse.put(2, "");
        agentResponse.put(3, "");
        agentResponse.put(4, "");
    }

    private void resetResponse() {
        agentResponse.put(1, "");
        agentResponse.put(2, "");
        agentResponse.put(3, "");
        agentResponse.put(4, "");
    }

    private void updateResponse(int agentId, String response) {
        agentResponse.put(agentId, response);
    }

    private boolean allAgentsReachedTarget() {
        return agentStatus.getAgentStatus(1) == 1 && agentStatus.getAgentStatus(2) == 1 &&
                agentStatus.getAgentStatus(3) == 1 && agentStatus.getAgentStatus(4) == 1;
    }

    // 线程的run方法，执行代理逻辑
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
                if (currentPosition == targetPosition) {
                    if (agentStatus.getAgentStatus(agentId) == 1) {
                        System.out.println("Agent" + agentId + " has reached the target location and the thread has ended");
                        stopThread();
                    }
                } else {
                    receiveRequest();// 要判断该函数是否导致了棋子移动，如果移动了，则下个函数move需要跳过，保证每秒最多移动一次 wy
                    move();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (allAgentsReachedTarget()) {
            System.out.println("All Agents have reached the target location.");
        }
    }

    private int[] direction() {
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
    public void sendRequest(int receiverId, int senderPosition) {
        messageList.addMessage(new Message(messageList.getMessages().size(), agentId, receiverId, "REQUEST_MOVE " + senderPosition, MessageTypeEnum.REQUEST, false));
    }

    public void receiveRequest() {
        //遍历消息列表，找到自己的消息
        for (Message message : messageList.getUnreadMessageList(agentId)) {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.REQUEST) {
                System.out.println("Agent" + agentId + " received request: " + message.getContent());
                messageList.setRead(message.getId());
                handleRequest(message);
            }
        }
    }

    public void move() {
        //得到可能移动的位置
        int[] direction = this.direction();

        for (int i = 0; i < 4; i++) {
            //不为-1则可能移动
            if (direction[i] != -1) {
                //该位置没有棋子，则直接移动返回true
                if (map.get(direction[i]) == 0) {
                    move(agentId, direction[i]);
                    return;
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
                System.out.println("Agent" + agentId + " sends give way request to " + requestAgents[i]);
                sendRequest(requestAgents[i], currentPosition);//等待消息，交由handrequest控制
                if (receiveResponse()) {
                    resetResponse();
                    break;
                }
            } else if (i == 3) {
                Detour();
            }
        }
        // 这个写成两部分，先循环发送请求。然后进入死循环等待消息，或者路被让开执行move移动，或者没有让路执行detour移动后跳出。wy
        // 写在一起，如果在其它棋子等待的1s内发送请求，是不会立刻得到响应。wy

    }

    private void move(int agentId, int position) {
        map.set(currentPosition, 0);
        map.set(position, agentId);
        // 在普通move中，重置 lastPosition =-1 wy
        System.out.println("Agent" + agentId + " moves from " + currentPosition + " to " + position);
        map.printMap();
        currentPosition = position;
        if (currentPosition == targetPosition) {
            System.out.println("Agent" + agentId + " has reached the target position, thread ended");
            stopThread();
        }
    }

    private boolean handleRequest(Message message) {
        //从消息中获得棋子id和其位置
        int senderPosition = -1;
        String content = message.getContent();
        if (content.startsWith("REQUEST_MOVE")) {
            String[] messageArray = content.split(" ");
            senderPosition = Integer.parseInt(messageArray[1]);
        }
        int[] requestAgents = {0, 0, 0, 0};
        int[] direction = direction();
        //第一种情况，让路的路径恰巧也在移动方向上且没有被阻塞，则允许让路

        for (int i = 0; i < 4; i++) {
            if (direction[i] < 0 || direction[i] > 24) {
                if (i == 3) {
                    sendResponse(message.getSenderId(), "REFUSE_MOVE");
                    return true;
                }
            } else if (direction[i] != -1 && direction[i] != senderPosition) {
                if (map.get(direction[i]) == 0) {
                    move(agentId, direction[i]);  // 记录信息，由run中的move实现移动 wy
                    sendResponse(message.getSenderId(), "ALLOW_MOVE");
                    return true;
                }
            } else if (direction[i] != senderPosition) {
                //如果被阻塞,且阻塞棋子不是请求棋子,则记录该棋子id
                /**
                 *        for(int i=0;i<4;i++) {
                 *             if (direction[i] != -1 && direction[i] != position) {
                 *                 if (MapChess.getInstant()[direction[i]] == 0) {
                 *                     this.give_way_position=direction[i];
                 *                     this.allow = true;
                 *                     // 记录可让路的位置，向父棋子发送"回复请求"
                 *                     // return;结束函数
                 *                 }else
                 *                     request_agence[i] = MapChess.getInstant()[direction[i]];
                 *                     //如果被阻塞,且阻塞棋子不是请求棋子,则记录该棋子id
                 *             }
                 *         }
                 *
                 *   注意 ，这里判断是否记录id的条件是该位置是否有棋子 wy
                 *
                 *
                 *
                 *
                 *
                 * */
                requestAgents[i] = map.get(direction[i]);
            }
        }
        int response = 0;
        for (int i = 0; i < 4; i++) {
            if (requestAgents[i] != 0) {
                response = 1;
                sendRequest(requestAgents[i], currentPosition);// 请求，等待，交由handleRequest处理

                // 最好循环等待，收到消息跳出，标记移动位置，由run中的move实现移动 wy
                receiveResponse();
                if (agentResponse.get(requestAgents[i]).equals("ALLOW_MOVE")) {
                    return true;
                }
            }
        }
        if (response == 0) {
            sendResponse(message.getSenderId(), "REFUSE_MOVE");
            // 不允许，说明唯一的阻塞棋子也是请求棋子，那么就不允许移动
        }
        return true;
    }

    private void sendResponse(int receiverId, String content) {
        messageList.addMessage(new Message(messageList.getMessages().size(), agentId, receiverId, content, MessageTypeEnum.RESPONSE, false));
    }

    private boolean receiveResponse() {
        //遍历消息列表，找到自己的消息
        for (Message message : messageList.getUnreadMessageList(agentId)) {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.RESPONSE) {
                System.out.println("Agent" + agentId + " received response: " + message.getContent());
                messageList.setRead(message.getId());
                return handleResponse(message);
            }
        }
        return false;
    }

    private boolean handleResponse(Message message) {
        updateResponse(message.getSenderId(), message.getContent());
        return message.getContent().equals("ALLOW_MOVE");
    }

    private void Detour() {
        System.out.println("Agent" + agentId + " is detouring");
        int[] direction = this.direction();
        int nextPosition = -1;
        for (int i = 0; i < 4; i++) {
            //绕路也不走回头路
            if (direction[i] == -1 && direction[i] != lastPosition) {
                switch (i) {
                    case 0 -> {
                        System.out.println("Agnet" + agentId + " Detour to the up");
                        nextPosition = currentPosition - 5;
                    }
                    case 1 -> {
                        System.out.println("Agnet" + agentId + " Detour to the down");
                        nextPosition = currentPosition + 5;
                    }
                    case 2 -> {

                        if (currentPosition % 5 != 0) {
                            System.out.println("Agnet" + agentId + " Detour to the left");
                            nextPosition = currentPosition - 1;
                        }
                    }
                    case 3 -> {
                        if ((currentPosition + 1) % 5 != 0) {
                            System.out.println("Agnet" + agentId + " Detour to the right");
                            nextPosition = currentPosition + 1;
                        }
                    }
                }
                if (nextPosition >= 0 && nextPosition <= 24)
                    break;
            }
        }
        if (nextPosition != -1) {
            // 绕路，并记录当前位置，避免下次行动回到该位置
            move(agentId, nextPosition);
            lastPosition = currentPosition;
        }
    }

    private void stopThread() {
        agentStatus.setAgentStatus(agentId, 1);
        running = false;
    }
}
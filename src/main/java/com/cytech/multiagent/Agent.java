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
        agentResponse.put(1, "ALLOW_MOVE");
        agentResponse.put(2, "ALLOW_MOVE");
        agentResponse.put(3, "ALLOW_MOVE");
        agentResponse.put(4, "ALLOW_MOVE");
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
                        System.out.println("Agent" + Thread.currentThread().getName() + " has reached the target location and the thread has ended");
                        stopThread();
                    }
                } else {
                    if (agentStatus.getMainAgentId() == agentId) {
                        System.out.println("----------------------------------------");
                        move();
                        // 该回合结束，唤醒下一个线程
                    } else {
                        receiveRequest();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (allAgentsReachedTarget()) {
                System.out.println("All Agents have reached the target location, game over");
            }
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

    public void receiveRequest() throws InterruptedException {
        //遍历消息列表，找到自己的消息
        for (Message message : messageList.getUnreadMessageList(agentId)) {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.REQUEST) {
                System.out.println("Agent" + Thread.currentThread().getName() + " received request: " + message.getContent());
                messageList.setRead(message.getId());
                handleRequest(message);
            }
        }
    }

    public boolean move() throws InterruptedException {
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
                System.out.println("Agent" + Thread.currentThread().getName() + " sends give way request to " + requestAgents[i]);
                sendRequest(requestAgents[i], currentPosition);//等待消息，交由handrequest控制
                receiveResponse();
            } else if (i == 3) {
                System.out.println("Agent" + Thread.currentThread().getName() + " cannot move, game failed");
            }
        }
        return false;
    }

    private void move(int agentId, int position) {
        map.set(currentPosition, 0);
        map.set(position, agentId);
        System.out.println("Agent" + Thread.currentThread().getName() + " moves from " + currentPosition + " to " + position);
        map.printMap();
        currentPosition = position;
        if (currentPosition == targetPosition) {
            agentStatus.setAgentStatus(agentId, 1);
            System.out.println("Agent" + Thread.currentThread().getName() + " has reached the target position, thread ended");
            stopThread();
        }
    }

    private boolean handleRequest(Message message) throws InterruptedException {
        //从消息中获得棋子id和其位置
        int senderPosition = 0;
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
            } else if (direction[i] != 0 && direction[i] != senderPosition) {
                if (map.get(direction[i]) == 0) {
                    move(agentId, direction[i]);
                    sendResponse(message.getSenderId(), "ALLOW_MOVE");
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
//            messageList.addMessage(new Message(messageList.getMessages().size(), message.getSenderId(), "REFUSE_MOVE");// 不允许，说明唯一的阻塞棋子也是请求棋子，那么就不允许移动
        }
        return true;
    }

    private void sendResponse(int receiverId, String content) {
        messageList.addMessage(new Message(messageList.getMessages().size(), agentId, receiverId, content, MessageTypeEnum.RESPONSE, false));
    }

    private void receiveResponse() throws InterruptedException {
        //遍历消息列表，找到自己的消息
        for (Message message : messageList.getUnreadMessageList(agentId)) {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.RESPONSE) {
                System.out.println("Agent" + Thread.currentThread().getName() + " received response: " + message.getContent());
                messageList.setRead(message.getId());
                handleResponse(message.getContent());
            }
        }
    }

    private void handleResponse(String content) throws InterruptedException {
        //是否接受让路应以informer形式通知请求棋子，
        //接受一个"回复请求通知"后，req参数-1
        String informertype = "";

        if (informertype == "回复请求") {
            this.req = -1;
            //向this.give_way_permit中添加对方是否同意让路
            int id = 0;
            boolean response = true;
            this.give_way_permit.put(id, response);
            if (this.req != 0) {
                //TODO
                //继续等待所有结果返回
            } else {
                //判断是否有结果为允许让路
                if (this.give_way_permit.containsValue(true)) {

                    for (Map.Entry<Integer, Boolean> entry : this.give_way_permit.entrySet()) {
                        if (entry.getValue()) {
                            this.allow_id = entry.getKey();
                            break;
                        }
                    }
                    //得到一个可让路的子棋子id

                    // TODO
                    if (this.fatherchess >= 0) {
                        // 如果有父棋子，则先向父棋子发送"回复请求"，并等待
                    } else {
                        // 如果没有父棋子，向其发送"确认选择通知"(有多个棋子可以让路时只选择一个)。并等待得到对方"移动完成通知"(对方让路径后，再移动)。
                    }


                } else {

                    if (this.fatherchess >= 0)
                        this.Detour();
                        //如果没有父棋子实现
                        //绕路逻辑
                    else
                        this.allow = false;
                    //如果有父棋子，向父棋子发送拒绝的"回复通知"
                }

            }
        }
        if (informertype == "确认移动") {
            //收到确认移动，有两种情况，第一种可以直接让路，第二种让路路径被阻塞
            //因为让路路径被阻塞，才会发送让路请求，才会产生子棋子，所以以此作为判断依据
            if (this.childrenchess.isEmpty()) {
                //对于第一种，执行移动，然后向父棋子发出"移动完成通知"
            } else {
                //对于第二种，先向子棋子发出"确认移动通知"，然后等待
            }


        }
        if (informertype == "移动完成") {
            //收到"移动完成通知"后，执行移动，如果还有父棋子，则向其发送移动完成通知，没有则结束
            this.give_way_move();
            if (this.fatherchess >= 0) {
                //通知父棋子
            } else {
                //没有父棋子，说明是初始棋子，已经完成移动，可以让下一个棋子行动
            }
        }
        updateResponse(message.getSenderId(), content);
        move();
    }

    private void handleinformer() {
        //是否接受让路应以informer形式通知请求棋子，
        //接受一个"回复请求通知"后，req参数-1
        String informertype = "";

        if (informertype == "回复请求") {
            this.req = -1;
            //向this.give_way_permit中添加对方是否同意让路
            int id = 0;
            boolean response = true;
            this.give_way_permit.put(id, response);
            if (this.req != 0) {
                //TODO
                //继续等待所有结果返回
            } else {
                //判断是否有结果为允许让路
                if (this.give_way_permit.containsValue(true)) {

                    for (Map.Entry<Integer, Boolean> entry : this.give_way_permit.entrySet()) {
                        if (entry.getValue()) {
                            this.allow_id = entry.getKey();
                            break;
                        }
                    }
                    //得到一个可让路的子棋子id

                    // TODO
                    if (this.fatherchess >= 0) {
                        // 如果有父棋子，则先向父棋子发送"回复请求"，并等待
                    } else {
                        // 如果没有父棋子，向其发送"确认选择通知"(有多个棋子可以让路时只选择一个)。并等待得到对方"移动完成通知"(对方让路径后，再移动)。
                    }


                } else {

                    if (this.fatherchess >= 0)
                        this.Detour();
                        //如果没有父棋子实现
                        //绕路逻辑
                    else
                        this.allow = false;
                    //如果有父棋子，向父棋子发送拒绝的"回复通知"
                }

            }
        }
        if (informertype == "确认移动") {
            //收到确认移动，有两种情况，第一种可以直接让路，第二种让路路径被阻塞
            //因为让路路径被阻塞，才会发送让路请求，才会产生子棋子，所以以此作为判断依据
            if (this.childrenchess.isEmpty()) {
                //对于第一种，执行移动，然后向父棋子发出"移动完成通知"
            } else {
                //对于第二种，先向子棋子发出"确认移动通知"，然后等待
            }


        }
        if (informertype == "移动完成") {
            //收到"移动完成通知"后，执行移动，如果还有父棋子，则向其发送移动完成通知，没有则结束
            this.give_way_move();
            if (this.fatherchess >= 0) {
                //通知父棋子
            } else {
                //没有父棋子，说明是初始棋子，已经完成移动，可以让下一个棋子行动
            }
        }
    }

    private void give_way_move() {
        if (childrenchess.isEmpty()) {
            MapChess.getInstant()[this.give_way_position] = this.id;
            MapChess.getInstant()[this.position] = 0;
        }
        if (this.allow_id != 0) {
            MapChess.getInstant()[this.request_position.get(this.allow_id)] = this.id;
            MapChess.getInstant()[this.position] = 0;
        }
        this.reset_variable();
    }

    private void Detour() {
        int[] direction = this.direction();
        int next_position = -1;
        for (int i = 0; i < 4; i++) {
            //绕路也不走回头路
            if (direction[i] == 0 && direction[i] != this.last_position) {
                switch (i) {
                    case 0 -> next_position = this.position - 5;
                    case 1 -> next_position = this.position + 5;
                    case 2 -> {
                        if (this.position % 5 != 0)
                            next_position = this.position - 1;
                    }
                    case 3 -> {
                        if ((this.position + 1) % 5 != 0)
                            next_position = this.position + 1;
                    }
                }
                if (next_position >= 0 && next_position <= 24)
                    break;
            }
        }
        // 绕路，并记录当前位置，避免下次行动回到该位置
        this.last_position = this.position;
        MapChess.getInstant()[this.position] = 0;
        MapChess.getInstant()[next_position] = this.id;
    }

    private void stopThread() {
        running = false;
    }
}
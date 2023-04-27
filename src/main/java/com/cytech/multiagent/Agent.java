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
    private int lastPosition=-2;
    private volatile boolean running = true;
    private boolean ismoved=false;
    private Map<Integer,Integer> record;


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
        this.record=new HashMap<>();
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

    // The thread's run method, which executes the proxy logic
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

                    receiveRequest();// To determine if the function causes a piece to move, if it does,
                                     // then the next move needs to be skipped, guaranteeing a maximum of one move per second
                    if(!this.ismoved){
                        move();}
                    this.ismoved=false;
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
        //Calculation of the position of possible actions based on the current position and the target position
        int[] direction = {-1, -1, -1, -1};
        //Zeroed, corresponding to Up Down Left Right

        int positionX = currentPosition % 5;
        int positionY = currentPosition / 5;
        int targetPositionX = targetPosition % 5;
        int targetPositionY = targetPosition / 5;
        //Calculation of rows and columns for the current and target positions

        //Is the current and target position on the same line
        if (targetPositionY - positionY != 0) {
            if (targetPositionY > positionY) {
                //The number of rows in the target position is greater than the number of rows in the current position,
                // then you can move down
                direction[1] = currentPosition + 5;
            } else {
                //Move up
                direction[0] = currentPosition - 5;
            }
        }
        if (targetPositionX - positionX != 0) {
            if (targetPositionX > positionX) {
                //move right
                direction[3] = currentPosition + 1;
            } else {
                //move left
                direction[2] = currentPosition - 1;
            }
        }
        return direction;
    }

    public void sendRequest(int receiverId, int senderPosition) {
        messageList.addMessage(new Message(messageList.getMessages().size(), agentId, receiverId, "REQUEST_MOVE " + senderPosition, MessageTypeEnum.REQUEST, false));
    }

    public void receiveRequest() {
        //Iterate through the list of messages to find your own unread messages
        for (Message message : messageList.getUnreadMessageList(agentId)) {
            if (!message.isRead() && message.getReceiverId() == agentId && message.getType() == MessageTypeEnum.REQUEST) {
                System.out.println("Agent" + agentId + " received request: " + message.getContent());
                messageList.setRead(message.getId());
                handleRequest(message);
            }
        }
    }

    public void move() {
        //Get the location of possible moves
        int[] direction = this.direction();
        for (int i = 0; i < 4; i++) {
            //Possible movement if not -1
            if (direction[i] != -1) {
                //If there are no pieces in that position, move directly back to true
                if (map.get(direction[i]) == 0&&direction[i]!=this.lastPosition) {
                    move(agentId, direction[i]);

                    this.lastPosition=-2;
                    return;
                } else {
                    //If a piece is in the way and does not refuse to give way, record
                    if (requestAgents[i] != -1) {
                        requestAgents[i] = map.get(direction[i]);
                    }
                }
            }
        }
        //Send requests to all possible agents without the function ending
        for (int i = 0; i < 4; i++) {
            if (requestAgents[i] != 0 && agentStatus.getAgentStatus(requestAgents[i]) == 0 && !Objects.equals(agentResponse.get(requestAgents[i]), "REFUSE_MOVE")) {
                System.out.println("Agent" + agentId + " sends give way request to " + requestAgents[i]);
                sendRequest(requestAgents[i], currentPosition);//Waiting for messages, left to handrequest control
                if (receiveResponse()) {
                    resetResponse();
                    break;
                }
            } else if (i == 3) {
                Detour();
            }
        }

    }

    private void move(int agentId, int position) {
        map.set(currentPosition, 0);
        map.set(position, agentId);
        this.ismoved=true;

        System.out.println("Agent" + agentId + " moves from " + currentPosition + " to " + position);
        map.printMap();
        currentPosition = position;
        if (currentPosition == targetPosition) {
            System.out.println("Agent" + agentId + " has reached the target position, thread ended");
            stopThread();
        }
    }

    private boolean handleRequest(Message message) {
        //Get the piece id and its position from the message
        int senderPosition = -1;
        String content = message.getContent();
        if (content.startsWith("REQUEST_MOVE")) {
            String[] messageArray = content.split(" ");
            senderPosition = Integer.parseInt(messageArray[1]);
        }
        int[] requestAgents = {0, 0, 0, 0};
        int[] direction = direction();
        //In the first case, where the path to give way also happens to be in the direction of movement and is not blocked, giving way is allowed

        for (int i = 0; i < 4; i++) {
            if (direction[i] < 0 || direction[i] > 24) {
                if (i == 3) {
                    sendResponse(message.getSenderId(), "REFUSE_MOVE");
                    return true;
                }
            } else if (direction[i] != -1 && direction[i] != senderPosition) {
                if (map.get(direction[i]) == 0) {
                    move(agentId, direction[i]);  // Record information, move by move in run
                    this.lastPosition=-2;
                    sendResponse(message.getSenderId(), "ALLOW_MOVE");
                    return true;
                }else {
                    requestAgents[i] = map.get(direction[i]);
                }
            }



        }
        int response = 0;
        for (int i = 0; i < 4; i++) {
            if (requestAgents[i] != 0) {
                response = 1;
                sendRequest(requestAgents[i], currentPosition);// Request, wait, hand over to handleRequest

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
        //Iterate through the list of messages to find your own
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
        int avoid=-2;
        if(this.record.containsKey(this.currentPosition)){
            avoid=this.record.get(this.currentPosition);
            System.out.println("avoid,"+avoid);
            this.record=new HashMap<>();
        }
        int nextPosition = -1;
        for (int i = 0; i < 4; i++) {
            //detours but no backtracking
            if (direction[i] == -1) {
                switch (i) {
                    case 0 -> {
                        System.out.println("Agnet" + agentId + " Detour to the up");
                        nextPosition = currentPosition - 5;
                        if(nextPosition==avoid){
                            nextPosition=-1;
                        }
                    }
                    case 1 -> {
                        System.out.println("Agnet" + agentId + " Detour to the down");
                        nextPosition = currentPosition + 5;
                        if(nextPosition==avoid){
                            nextPosition=-1;
                        }
                    }
                    case 2 -> {

                        if (currentPosition % 5 != 0) {
                            System.out.println("Agnet" + agentId + " Detour to the left");
                            nextPosition = currentPosition - 1;
                            if(nextPosition==avoid){
                                nextPosition=-1;
                            }
                        }
                    }
                    case 3 -> {
                        if ((currentPosition + 1) % 5 != 0) {
                            System.out.println("Agnet" + agentId + " Detour to the right");
                            nextPosition = currentPosition + 1;
                            if(nextPosition==avoid){
                                nextPosition=-1;
                            }
                        }
                    }
                }
                if (nextPosition >= 0 && nextPosition <= 24)
                    break;
            }
        }
        if (nextPosition!=-1&&nextPosition <25) {
            // Detour and record current position to avoid returning to that position for the next action
            this.record.put(currentPosition,nextPosition);
            lastPosition = currentPosition;
            move(agentId, nextPosition);


        }
    }

    private void stopThread() {
        agentStatus.setAgentStatus(agentId, 1);
        running = false;
    }
}
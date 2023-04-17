package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent extends Thread {
    private int agentId;
    private int currentPosition;
    private int targetPosition;
    private int nextPosition;
    private Map map;



    public Agent(int agentId, int currentPosition, int targetPosition, Map map) {
        this.agentId = agentId;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
        this.map = map;
    }

    // 线程的run方法，执行代理逻辑
    @Override
    public void run() {
        // 在这里实现代理的逻辑，例如移动和与其他代理交互
        if (agentId == 1) {
            // Agent1向其他Agent发送消息
            sendRequest(2, "Hello, Agent2!");
            sendRequest(3, "Hello, Agent3!");
            sendRequest(4, "Hello, Agent4!");
        } else {
            // 其他Agent收到Agent1的消息后，回复给Agent1
            String message = receiveResponse();
            if (message != null) {
                System.out.println("Agent" + agentId + " received: " + message);
                sendRequest(1, "Hello, Agent1! This is Agent" + agentId + ".");
            }
        }

        if (agentId == 1) {
            // Agent1接收其他Agent的回复
            for (int i = 0; i < 3; i++) {
                String reply = receiveResponse();
                if (reply != null) {
                    System.out.println("Agent1 received: " + reply);
                }
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

    public void receiveRequest(){}

    public String receiveResponse() {
        return null;
    }

    public void processResponse(){}

    public void move(){

    }
}




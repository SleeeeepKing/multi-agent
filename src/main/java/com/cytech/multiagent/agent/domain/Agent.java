package com.cytech.multiagent.agent.domain;

import com.cytech.multiagent.agent.domain.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
    public void sendRequest(){}

    public void receiveRequest(){}

    public void receiveResponse(){}

    public void processResponse(){}

    public void move(){}
}




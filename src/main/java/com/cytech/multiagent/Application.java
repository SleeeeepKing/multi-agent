package com.cytech.multiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        GameMap map = GameMap.getInstance();
        MessageList messageList = MessageList.getInstance();
        AgentStatus agentStatus = AgentStatus.getInstance();

        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, 0, 2, map, messageList, agentStatus);
        Agent agent2 = new Agent(2, 2, 23, map, messageList, agentStatus);
        Agent agent3 = new Agent(3, 1, 22, map, messageList, agentStatus);
        Agent agent4 = new Agent(4, 3, 21, map, messageList, agentStatus);

        map.set(agent1.getCurrentPosition(), agent1.getAgentId());
        map.set(agent2.getCurrentPosition(), agent2.getAgentId());
        map.set(agent3.getCurrentPosition(), agent3.getAgentId());
        map.set(agent4.getCurrentPosition(), agent4.getAgentId());
        map.printMap();

        agent1.start();
        agent2.start();
        agent3.start();
        agent4.start();

        // 等待所有代理线程完成
        try {
            agent1.join();
            agent2.join();
            agent3.join();
            agent4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 这里所有代理线程已完成
        System.out.println("Game Over");
    }
}

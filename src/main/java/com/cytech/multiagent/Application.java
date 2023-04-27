package com.cytech.multiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        GameMap map = GameMap.getInstance();
        MessageList messageList = MessageList.getInstance();
        AgentStatus agentStatus = AgentStatus.getInstance();
        Random r = new Random();
        int[] start = new int[5];
        int[] end = new int[5];
        for (int i = 1; i < 5; i++) {
            while (true) {
                int position = r.nextInt(25);
                if (map.get(position) == 0) {
                    start[i] = position;
                    map.set(position, i);
                    break;
                }
            }
        }
        for (int i = 1; i < 5; i++) {
            while (true) {
                int position = r.nextInt(25);
                if (start[i] != position && Arrays.stream(end).noneMatch(x -> x == position)) {
                    end[i] = position;
                    break;
                }
            }
        }
        System.out.println("Agent1 start: " + start[1] + " end: " + end[1]);
        System.out.println("Agent2 start: " + start[2] + " end: " + end[2]);
        System.out.println("Agent3 start: " + start[3] + " end: " + end[3]);
        System.out.println("Agent4 start: " + start[4] + " end: " + end[4]);
        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, start[1], end[1], map, messageList, agentStatus);
        Agent agent2 = new Agent(2, start[2], end[2], map, messageList, agentStatus);
        Agent agent3 = new Agent(3, start[3], end[3], map, messageList, agentStatus);
        Agent agent4 = new Agent(4, start[4], end[4], map, messageList, agentStatus);


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

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
        ReentrantLock lock = new ReentrantLock(true);
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();
        Condition condition3 = lock.newCondition();
        Condition condition4 = lock.newCondition();

        GameMap map = GameMap.getInstance();
        Message message = Message.getInstance();
        AgentStatus agentStatus = AgentStatus.getInstance();

        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, 0, 2, map, message, agentStatus, lock, condition1, condition2, condition3, condition4);
        Agent agent2 = new Agent(2, 2, 23, map, message, agentStatus, lock, condition1, condition2, condition3, condition4);
        Agent agent3 = new Agent(3, 1, 22, map, message, agentStatus, lock, condition1, condition2, condition3, condition4);
        Agent agent4 = new Agent(4, 3, 21, map, message, agentStatus, lock, condition1, condition2, condition3, condition4);

        map.set(agent1.getCurrentPosition(), agent1.getAgentId());
        map.set(agent2.getCurrentPosition(), agent2.getAgentId());
        map.set(agent3.getCurrentPosition(), agent3.getAgentId());
        map.set(agent4.getCurrentPosition(), agent4.getAgentId());
        map.printMap();

        Thread thread1 = new Thread(agent1, Agent.FLAG_THREAD_1);
        Thread thread2 = new Thread(agent2, Agent.FLAG_THREAD_2);
        Thread thread3 = new Thread(agent3, Agent.FLAG_THREAD_3);
        Thread thread4 = new Thread(agent4, Agent.FLAG_THREAD_4);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        // 等待所有代理线程完成
        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 这里所有代理线程已完成
        System.out.println("All agents have finished.");
    }
}

package com.cytech.multiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        Map map = Map.getInstance();

        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, 0, 24, map);
        Agent agent2 = new Agent(2, 1, 23, map);
        Agent agent3 = new Agent(3, 2, 22, map);
        Agent agent4 = new Agent(4, 3, 21, map);

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
        System.out.println("All agents have finished.");
    }
}

package com.cytech.multiagent;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        Map map = Map.getInstance();

        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, 1, 0, 0, map);
        Agent agent2 = new Agent(2, 3, 1, 0, map);
        Agent agent3 = new Agent(3, 5, 5, 0, map);
        Agent agent4 = new Agent(4, 6, 6, 0, map);

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

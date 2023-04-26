package com.cytech.multiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

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
        Random r=new Random();
        int[] initial_position=new int[5];
        int[] final_position=new int[5];
        int[] final_map=new int[25];
        for (int i=0;i<25;i++)
            final_map[i]=0;
        for(int i=1;i<5;i++){
            while (true){
                int position=r.nextInt(25);
                if(map.get(position)==0){
                    initial_position[i]=position;
                    System.out.print(position+"\n");
                    map.set(position,i);
                    break;
                }
            }
        }
        for(int i=1;i<5;i++){
            while (true){
                int position=r.nextInt(25);
                if(final_map[position]==0){
                    final_position[i]=position;
                    final_map[position]=i;
                    System.out.print(position+"\n");
                    break;
                }
            }
        }
        // 创建并启动四个代理线程
        Agent agent1 = new Agent(1, initial_position[1], final_position[1], map, messageList, agentStatus);
        Agent agent2 = new Agent(2, initial_position[2], final_position[2], map, messageList, agentStatus);
        Agent agent3 = new Agent(3, initial_position[3], final_position[3], map, messageList, agentStatus);
        Agent agent4 = new Agent(4, initial_position[4], final_position[4], map, messageList, agentStatus);


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

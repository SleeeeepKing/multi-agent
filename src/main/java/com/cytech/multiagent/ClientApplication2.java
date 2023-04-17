package com.cytech.multiagent;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;
import com.cytech.multiagent.commen.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ClientApplication2 {
    public static void main(String[] args) throws IOException {
        // 初始化代理列表
        List<Agent> agents = new ArrayList<>();
        int boardSize = 5;
        Board board = Board.getInstance(boardSize);
        int serverPort = 8090;

        // 生成不重复的随机初始位置
        Set<Position> positions = new HashSet<>();
//        positions.add(new Position(0, 1));
        positions.add(new Position(0, 0));

        int agentId = 2;
        for (Position position : positions) {
            // 创建一个目标位置，这里我们只设置为右下角
            Position targetPosition = new Position(0, 1);
            Cell cell = new Cell(agentId, position, targetPosition);
            board.getCells()[position.getRow()][position.getCol()] = cell;

            Socket socket = null;
            Client client = new Client("localhost", serverPort);
            socket = client.getSocket();

            Agent agent = new Agent(cell, board, new Semaphore(1),socket, client);
            agents.add(agent);
        }
        // 创建一个线程池来管理代理线程
        ExecutorService executorService = Executors.newFixedThreadPool(agents.size());

        // 提交代理任务到线程池
        for (Agent agent : agents) {
            executorService.submit(agent::start);
        }

        // 关闭线程池并等待所有任务完成
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


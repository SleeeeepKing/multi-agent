package com.cytech.multiagent;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ClientApplication4 {
    public static void main(String[] args) {
        // 初始化代理列表
        List<Agent> agents = new ArrayList<>();
        int boardSize = 5;
        Board board = Board.getInstance(boardSize);
        int serverPort = 8090;

        // 生成不重复的随机初始位置
        Set<Position> positions = new HashSet<>();
        positions.add(new Position(0, 1));

        int agentId = 4;
        for (Position position : positions) {
            // 创建一个目标位置，这里我们只设置为右下角
            Position targetPosition = new Position(0, 0);
            Cell cell = new Cell(agentId, position, targetPosition);
            board.getCells()[position.getRow()][position.getCol()] = cell;

            Agent agent = new Agent(cell, board, new Semaphore(1), serverPort);
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


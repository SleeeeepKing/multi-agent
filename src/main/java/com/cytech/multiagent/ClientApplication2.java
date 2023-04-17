package com.cytech.multiagent;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ClientApplication2 {
    public static void main(String[] args) {
        // 初始化代理列表
        List<Agent> agents = new ArrayList<>();
        int boardSize = 5;
        Board board = Board.getInstance(boardSize);
        int serverPort = 8090;

        // 生成不重复的随机初始位置
        Set<Position> positions = new HashSet<>();
        /*Random random = new Random();
        while (positions.size() < 4) {
            int row = random.nextInt(boardSize);
            int col = random.nextInt(boardSize);
            positions.add(new Position(row, col));
        }*/
        positions.add(new Position(0, 2));

        int agentId = 2;
        for (Position position : positions) {
            // 创建一个目标位置，这里我们只设置为右下角
            Position targetPosition = new Position(0, 1);
            Cell cell = new Cell(agentId, position, targetPosition);
            board.getCells()[position.getRow()][position.getCol()] = cell;

            Agent agent = new Agent(cell, board, new Semaphore(1), serverPort);
            agents.add(agent);
            agentId++;
        }

        // 启动代理并等待它们完成
        agents.forEach(Agent::start);
        agents.forEach(agent -> {
            try {
                agent.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}


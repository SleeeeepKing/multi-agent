package com.cytech.multiagent.agent.service;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;

@Service
public class GameService {
    private static final int BOARD_SIZE = 4;
    private static final int AGENT_START_PORT = 8081;
    private Board board;
    private List<Agent> agents;
    private Semaphore semaphore;
    private CountDownLatch agentsFinished;

    public GameService() {
        board = Board.getInstance(BOARD_SIZE);
        agents = new ArrayList<>();
        semaphore = new Semaphore(1);
        agentsFinished = new CountDownLatch(4);
    }

    public void run() {
        initializeAgents();

        agents.forEach(agent -> {
            agent.start();
            System.out.println("Agent " + agent.getCell().getId() + " started on port " + agent.getAgentPort());
        });

        try {
            agentsFinished.await(); // 等待所有代理线程完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printBoard();
        printMoveHistory();
    }

    private void initializeAgents() {
        int boardSize = board.getCells().length;
        for (int i = 0; i < 4; i++) {
            Position currentPosition = new Position(i, 0);
            Position targetPosition = new Position(i, boardSize - 1);
            Cell cell = new Cell(i, currentPosition, targetPosition);
            board.getCells()[currentPosition.getRow()][currentPosition.getCol()] = cell;

            // 为每个 Agent 分配一个不同的端口
            int serverPort = AGENT_START_PORT + i;
            Agent agent = new Agent(cell, board, semaphore, serverPort);
            agent.setAgentsFinished(agentsFinished); // 将 CountDownLatch 传递给每个代理
            agents.add(agent);
        }
    }

    private void printBoard() {
        for (Cell[] row : board.getCells()) {
            for (Cell cell : row) {
                if (cell == null) {
                    System.out.print(" - ");
                } else {
                    System.out.print(" " + cell.getId() + " ");
                }
            }
            System.out.println();
        }
    }

    private void printMoveHistory() {
        for (Agent agent : agents) {
            System.out.println("Agent " + agent.getCell().getId() + " move history: " + agent.getMoveHistory());
        }
    }
}

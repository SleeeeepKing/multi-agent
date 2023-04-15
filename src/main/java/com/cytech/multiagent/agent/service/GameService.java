package com.cytech.multiagent.agent.service;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class GameService {
    private static final int BOARD_SIZE = 5;
    private final Board board;
    private final List<Agent> agents;

    public GameService() {
        board = Board.getInstance(BOARD_SIZE);
        agents = initializeAgents();
    }

    public void run() {
        int totalMoves = 0;
        boolean isComplete = false;

        while (!isComplete) {
            isComplete = true;

            for (Agent agent : agents) {
                boolean moved = agent.move();
                if (moved) {
                    totalMoves++;
                    isComplete = false;
                }
            }
        }

        System.out.println("游戏结束，总移动步数: " + totalMoves);
    }

    private List<Agent> initializeAgents() {
        List<Agent> agents = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= BOARD_SIZE * BOARD_SIZE; i++) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);

            while (board.getCells()[row][col] != null) {
                row = random.nextInt(BOARD_SIZE);
                col = random.nextInt(BOARD_SIZE);
            }

            Position currentPosition = new Position(row, col);
            Position targetPosition = new Position((i - 1) / BOARD_SIZE, (i - 1) % BOARD_SIZE);

            Cell cell = new Cell(i, currentPosition, targetPosition);
            board.getCells()[row][col] = cell;

            Agent agent = new Agent(cell, board);
            agents.add(agent);
        }

        return agents;
    }
}


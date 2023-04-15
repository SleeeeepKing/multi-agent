package com.cytech.multiagent.agent.service;

import com.cytech.multiagent.agent.domain.Agent;
import com.cytech.multiagent.agent.domain.Board;
import com.cytech.multiagent.agent.domain.Cell;
import com.cytech.multiagent.agent.domain.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

@Service
public class GameService {

    private static final int BOARD_SIZE = 5;
    private static final int AGENT_COUNT = 4;
    private static final int MAX_STEPS = 100;

    private Board board;
    private List<Agent> agents;
    private Semaphore semaphore;

    public void run() {
        board = Board.getInstance(BOARD_SIZE);
        semaphore = new Semaphore(1);

        initializeAgents();
        printBoard();

        for (Agent agent : agents) {
            agent.start();
        }

        for (Agent agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("游戏结束!");
        printBoard();

        for (Agent agent : agents) {
            System.out.println("Agent " + agent.getCell().getId() + " 目标位置：" + agent.getCell().getTargetPosition() + " 移动历史: " + agent.getMoveHistory());
        }
    }

    private void initializeAgents() {
        agents = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < AGENT_COUNT; i++) {
            Position initialPosition;
            Position targetPosition;

            do {
                initialPosition = new Position(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE));
            } while (board.getCells()[initialPosition.getRow()][initialPosition.getCol()] != null);

            do {
                targetPosition = new Position(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE));
            } while (targetPosition.equals(initialPosition));

            Cell cell = new Cell(i + 1, initialPosition, targetPosition);
            board.updateCell(initialPosition, targetPosition);
            board.getCells()[initialPosition.getRow()][initialPosition.getCol()] = cell;

            Agent agent = new Agent(cell, board, semaphore);
            agents.add(agent);
        }
    }


    private void printBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Cell cell = board.getCells()[row][col];
                System.out.print(cell == null ? "0 " : cell.getId() + " ");
            }
            System.out.println();
        }
        System.out.println("=====================");
    }
}

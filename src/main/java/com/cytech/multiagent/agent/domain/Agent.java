package com.cytech.multiagent.agent.domain;

import com.cytech.multiagent.agent.domain.enums.Direction;
import com.cytech.multiagent.commen.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent extends Thread {
    private Cell cell;
    private Board board;
    private Semaphore semaphore;
    private Client client;
    private List<String> moveHistory;
    private int agentPort;
    private CountDownLatch agentsFinished;
    private static final int SERVER_PORT = 8090;


    public Agent(Cell cell, Board board, Semaphore semaphore, int agentPort) {
        this.cell = cell;
        this.board = board;
        this.semaphore = semaphore;
        this.agentPort = agentPort;
        this.moveHistory = new ArrayList<>();
        try {
            this.client = new Client("localhost", agentPort);
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (Socket socket = new Socket("localhost", SERVER_PORT)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 向服务器发送初始信息，例如代理 ID
            writer.println("Agent " + cell.getId() + " is connected");

            // 主通信循环
            while (!isInterrupted()) {
                // 从服务器读取消息
                String message = reader.readLine();
                if (message == null) {
                    // 如果服务器关闭连接，退出循环
                    break;
                }

                // 根据收到的消息执行操作
                if (message.startsWith("MOVE")) {
                    // 解析消息并执行相应操作，例如更新代理的位置
                    // 您可能需要根据您的项目需求自定义消息格式和解析逻辑
                    move(); // 示例移动方法
                    writer.println("Agent " + cell.getId() + " moved");
                } else if (message.startsWith("EXIT")) {
                    // 如果收到退出指令，退出循环
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        agentsFinished.countDown(); // 表示这个代理已经完成
    }

    public boolean move() {
        Position currentPosition = cell.getCurrentPosition();
        Position targetPosition = cell.getTargetPosition();

        if (currentPosition.equals(targetPosition)) {
            return false;
        }

        List<Direction> possibleDirections = getPossibleDirections(currentPosition, targetPosition);
        for (Direction direction : possibleDirections) {
            Position newPosition = getNewPosition(currentPosition, direction);

            try {
                semaphore.acquire(); // 获取许可

                if (canMoveTo(newPosition)) {
                    board.updateCell(currentPosition, newPosition);
                    cell.setCurrentPosition(newPosition);
                    moveHistory.add(String.valueOf(newPosition));
                    client.sendMessage("Agent " + cell.getId() + " moved to " + newPosition);
                    semaphore.release(); // 释放许可
                    return true;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release(); // 确保许可被释放
            }
        }

        return false;
    }


    public List<String> getMoveHistory() {
        return moveHistory;
    }


    private List<Direction> getPossibleDirections(Position currentPosition, Position targetPosition) {
        List<Direction> possibleDirections = new ArrayList<>();
        if (targetPosition.getRow() < currentPosition.getRow()) {
            possibleDirections.add(Direction.UP);
        } else if (targetPosition.getRow() > currentPosition.getRow()) {
            possibleDirections.add(Direction.DOWN);
        }

        if (targetPosition.getCol() < currentPosition.getCol()) {
            possibleDirections.add(Direction.LEFT);
        } else if (targetPosition.getCol() > currentPosition.getCol()) {
            possibleDirections.add(Direction.RIGHT);
        }

        return possibleDirections;
    }


    private Position getNewPosition(Position currentPosition, Direction direction) {
        Position newPosition = new Position(currentPosition.getRow(), currentPosition.getCol());
        switch (direction) {
            case UP -> newPosition.setRow(newPosition.getRow() - 1);
            case DOWN -> newPosition.setRow(newPosition.getRow() + 1);
            case LEFT -> newPosition.setCol(newPosition.getCol() - 1);
            case RIGHT -> newPosition.setCol(newPosition.getCol() + 1);
        }
        return newPosition;
    }

    private boolean canMoveTo(Position newPosition) {
        int row = newPosition.getRow();
        int col = newPosition.getCol();
        int boardSize = board.getCells().length;

        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return false;
        }

        return board.getCells()[row][col] == null;
    }
}



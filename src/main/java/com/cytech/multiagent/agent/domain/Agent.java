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
    private List<String> moveHistory;
    private int agentPort;
    private CountDownLatch agentsFinished;
    private static final int SERVER_PORT = 8090;
    private Client client;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Agent(Cell cell, Board board, Semaphore semaphore, Socket socket, Client client) {
        this.cell = cell;
        this.board = board;
        this.semaphore = semaphore;
        this.socket = socket;
        this.agentsFinished = new CountDownLatch(2);
        this.client = client;
    }

    @Override
    public void run() {
        try {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 向服务器发送初始信息，例如代理 ID
//            writer.println("Agent " + cell.getId() + " is connected");
            writer.println("Init Agent " + cell.getId() + " is connected"+ ", Position on " + cell.getCurrentPosition().getRow() + " " + cell.getCurrentPosition().getCol());


            // 主通信循环
            while (!isInterrupted()) {
                move();
                // 从服务器读取消息
                String message = reader.readLine();
                System.out.println("Received: " + message);
                if (message == null) {
                    // 如果服务器关闭连接，退出循环
                    break;
                }

                // 解析发送者的代理ID
                String[] parts = message.split(" ");
                int senderAgentId = Integer.parseInt(parts[1]);

                // 根据收到的消息执行操作
                if (senderAgentId != cell.getId()) { // 只对其他代理的消息进行操作
                    System.out.println("Received: " + message);
                    if (message.startsWith("MOVE")) {
                        // todo 执行移动的代码
                        System.out.println("Received: " + message);
                    } else if (message.startsWith("EXIT")) {
                        // todo 执行移动的代码
                        System.out.println("Received: " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            agentsFinished.countDown(); // 表示这个代理已经完成
        }
    }

    public void sendMessageToServer(String message) {
        client.sendMessage(message);
    }


    public boolean move() {
        System.out.println("Agent " + cell.getId() + " is moving");
        Position currentPosition = cell.getCurrentPosition();
        Position targetPosition = cell.getTargetPosition();

        if (currentPosition.equals(targetPosition)) {
            return false;
        }

        List<Direction> possibleDirections = getPossibleDirections(currentPosition, targetPosition);
        for (Direction direction : possibleDirections) {
            Position newPosition = getNewPosition(currentPosition, direction);

            try {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore.acquire(); // 获取许可

                if (canMoveTo(newPosition)) {
                    System.out.println("Agent " + cell.getId() + " can move");
                    board.updateCell(currentPosition, newPosition);
                    cell.setCurrentPosition(newPosition);
//                    moveHistory.add(String.valueOf(newPosition));
                    sendMessageToServer("Agent " + cell.getId() + " moved to " + newPosition); // 添加这一行
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

        // 发送请求到服务器，并等待响应
        System.out.println("Sending request to server"+ " agent " + cell.getId() + " from "+ cell.getCurrentPosition()+ " to" + newPosition);
        sendMessageToServer("REQUEST_MOVE " + cell.getId() + " " + newPosition.getRow() + " " + newPosition.getCol());

        String response = readMessageFromServer();
        System.out.println("Received response from server: " + response);

        return "ALLOW_MOVE".equals(response);
    }

    private String readMessageFromServer() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}



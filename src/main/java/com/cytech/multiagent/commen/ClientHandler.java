package com.cytech.multiagent.commen;

import com.cytech.multiagent.agent.domain.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private int agentId; // 添加代理ID字段
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private Position agentPosition; // 添加代理位置字段

    public ClientHandler(Socket socket, Server server, int agentId, Position agentPosition) {
        this.socket = socket;
        this.server = server;
        this.agentId = agentId; // 将代理ID设置为构造函数参数
        this.agentPosition = agentPosition; // 将代理位置设置为构造函数参数
    }

    // 添加获取代理位置的方法
    public Position getAgentPosition() {
        return agentPosition;
    }

    @Override
    public void run() {
        try {
            InputStreamReader input = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(input);

            writer = new PrintWriter(socket.getOutputStream(), true);

            String message;
            Position initialPosition = null;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                if (message.startsWith("Init")) {
                    // 解析消息并获取代理 ID 和新位置
                    String[] parts = message.split(" ");
                    initialPosition = new Position(Integer.parseInt(parts[7]), Integer.parseInt(parts[8]));
                    this.agentPosition = initialPosition;
                    System.out.println("Agent " + agentId + " is at " + agentPosition);
                }
                if (message.startsWith("REQUEST_MOVE")) {
                    // 解析消息并获取代理 ID 和新位置
                    String[] parts = message.split(" ");
                    int newRow = Integer.parseInt(parts[2]);
                    int newCol = Integer.parseInt(parts[3]);

                    // 更新 agentPosition
                    agentPosition = new Position(newRow, newCol);

                    // 通过 Server 判断新位置是否已被其他代理占用
                    boolean positionOccupied = server.isPositionOccupied(agentPosition, this);

                    if (!positionOccupied) {
                        sendMessage(agentId +" ALLOW_MOVE");
                    } else {
                        sendMessage(agentId +" DENY_MOVE, agent " + agentId + " is at " + agentPosition);
                    }
                } /*else {
                    // 处理其他消息类型（例如广播消息）
                    server.broadcast(message, this);
                }*/
            }
        } catch (SocketException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}

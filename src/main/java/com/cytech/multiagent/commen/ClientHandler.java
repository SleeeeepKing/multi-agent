package com.cytech.multiagent.commen;

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

    public ClientHandler(Socket socket, Server server, int agentId) {
        this.socket = socket;
        this.server = server;
        this.agentId = agentId; // 将代理ID设置为构造函数参数
    }

    @Override
    public void run() {
        try {
            InputStreamReader input = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(input);

            writer = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                server.broadcast(message, this);
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

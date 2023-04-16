package com.cytech.multiagent.commen;

import com.cytech.multiagent.commen.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter writer;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
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
        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}

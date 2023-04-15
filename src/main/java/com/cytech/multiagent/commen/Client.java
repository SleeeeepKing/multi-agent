package com.cytech.multiagent.commen;

import java.io.*;
import java.net.Socket;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;

    public Client(String host, int port) {
        this.host = host;
        this.port = 5000;
    }

    public void sendMessage(String message) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


package com.cytech.multiagent.commen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public Client(String hostname, int port) throws IOException {
        this.socket = new Socket(hostname, port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String readMessage() throws IOException {
        return reader.readLine();
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

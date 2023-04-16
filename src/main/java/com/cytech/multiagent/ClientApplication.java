package com.cytech.multiagent;

import java.io.*;
import java.net.Socket;

public class ClientApplication {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8090;

        try (Socket socket = new Socket(hostname, port)) {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            Console console = System.console();
            while (true) {
                String message = console.readLine("Enter message: ");
                writer.println(message);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String response = reader.readLine();
                System.out.println("Server response: " + response);

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}


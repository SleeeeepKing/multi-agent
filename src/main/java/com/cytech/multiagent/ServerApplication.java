package com.cytech.multiagent;

import com.cytech.multiagent.commen.Server;

public class ServerApplication {
    public static void main(String[] args) {
        int serverPort = 8090;
        Server server = new Server(serverPort);
        server.startServer();
    }
}


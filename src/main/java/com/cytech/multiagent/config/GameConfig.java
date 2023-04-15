package com.cytech.multiagent.config;

import com.cytech.multiagent.agent.service.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cytech.multiagent.commen.Server;

@Configuration
public class GameConfig {
    private static final int SERVER_PORT = 8090;

    @Bean
    public CommandLineRunner commandLineRunner(GameService gameService) {
        return args -> {
            // 启动服务器
            Server server = new Server(SERVER_PORT);
            new Thread(server::startServer).start();

            // 运行游戏服务
            gameService.run();
        };
    }
}

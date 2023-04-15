package com.cytech.multiagent.config;

import com.cytech.multiagent.agent.service.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {
    @Bean
    public CommandLineRunner commandLineRunner(GameService gameService) {
        return args -> {
            gameService.run();
        };
    }
}

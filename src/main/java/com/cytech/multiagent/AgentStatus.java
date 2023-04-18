package com.cytech.multiagent;

import java.util.HashMap;
import java.util.Map;

public class AgentStatus {
    private static AgentStatus instance;
    private Map<Integer, Integer> agentStatus = new HashMap<>();

    private AgentStatus() {
        // 初始化地图数据
        for (int i = 0; i < 4; i++) {
            agentStatus.put(i, 0);
        }
    }

    public static AgentStatus getInstance() {
        if (instance == null) {
            instance = new AgentStatus();
        }
        return instance;
    }

    public void setAgentStatus(int agentId, int status) {
        agentStatus.put(agentId, status);
    }

    public int getAgentStatus(int agentId) {
        return agentStatus.get(agentId);
    }
}

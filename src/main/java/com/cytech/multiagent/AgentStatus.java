package com.cytech.multiagent;

import java.util.HashMap;
import java.util.Map;

public class AgentStatus {
    private static AgentStatus instance;
    private Map<Integer, Integer> agentStatus = new HashMap<>();

    private AgentStatus() {
        // 初始化数据, 0 代表未到达目标位置，1 代表已到达目标位置
        for (int i = 1; i < 5; i++) {
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

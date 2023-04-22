package com.cytech.multiagent;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MessageList {
    private static MessageList instance;
    private ArrayList<Message> messages = new ArrayList<>();

    public static MessageList getInstance() {
        if (instance == null) {
            instance = new MessageList();
        }
        return instance;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public Message[] getUnreadMessageList(int receiveAgentId) {
        return messages.stream().filter(message -> message.getReceiverId() == receiveAgentId && !message.isRead()).toArray(Message[]::new);
    }

    public void setRead(int messageId) {
        messages.stream().filter(message -> message.getId() == messageId).forEach(message -> message.setRead(true));
    }
}

package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Message {
    private static Message instance;
    private int senderId;
    private int receiverId;
    private String content;
    private boolean isRead;

    private Message() {
        this.senderId = 0;
        this.receiverId = 0;
        this.content = "";
        this.isRead = true;
    }

    public static Message getInstance() {
        if (instance == null) {
            instance = new Message();
        }
        return instance;
    }

    public void setMessage(int senderId, int receiverId, String content, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = isRead;
    }
}

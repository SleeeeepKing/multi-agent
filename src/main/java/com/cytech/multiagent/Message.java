package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private MessageTypeEnum type;
    private boolean isRead;
}

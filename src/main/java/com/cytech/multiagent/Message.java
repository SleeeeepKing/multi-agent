package com.cytech.multiagent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private int senderId;
    private int receiverId;
    private String content;
}

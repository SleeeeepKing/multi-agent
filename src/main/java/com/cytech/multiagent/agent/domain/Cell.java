package com.cytech.multiagent.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private int id;
    private Position currentPosition;
    private Position targetPosition;
}

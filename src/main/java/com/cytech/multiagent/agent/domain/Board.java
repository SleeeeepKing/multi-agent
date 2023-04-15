package com.cytech.multiagent.agent.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Board {
    private static Board instance;
    private Cell[][] cells;

    public Board(int n) {
        cells = new Cell[n][n];
    }

    public static Board getInstance(int n) {
        if (instance == null) {
            instance = new Board(n);
        }
        return instance;
    }

    public void updateCell(Position oldPosition, Position newPosition) {
        Cell cell = cells[oldPosition.getRow()][oldPosition.getCol()];
        cells[newPosition.getRow()][newPosition.getCol()] = cell;
        cells[oldPosition.getRow()][oldPosition.getCol()] = null;
    }
}


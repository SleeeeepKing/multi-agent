package com.cytech.multiagent.agent.domain;

import com.cytech.multiagent.agent.domain.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent extends Thread {
    private Cell cell;
    private Board board;
    private Semaphore semaphore;
    private List<String> moveHistory;

    public Agent(Cell cell, Board board, Semaphore semaphore) {
        this.cell = cell;
        this.board = board;
        this.semaphore = semaphore;
        this.moveHistory = new ArrayList<>();
    }
    public boolean move() {
        Position currentPosition = cell.getCurrentPosition();
        Position targetPosition = cell.getTargetPosition();

        if (currentPosition.equals(targetPosition)) {
            return false;
        }

        List<Direction> possibleDirections = getPossibleDirections(currentPosition, targetPosition);
        for (Direction direction : possibleDirections) {
            Position newPosition = getNewPosition(currentPosition, direction);

            try {
                semaphore.acquire(); // 获取许可

                if (canMoveTo(newPosition)) {
                    board.updateCell(currentPosition, newPosition);
                    cell.setCurrentPosition(newPosition);
                    moveHistory.add(String.valueOf(newPosition));

                    semaphore.release(); // 释放许可
                    return true;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release(); // 确保许可被释放
            }
        }

        return false;
    }


    public List<String> getMoveHistory() {
        return moveHistory;
    }


    private List<Direction> getPossibleDirections(Position currentPosition, Position targetPosition) {
        List<Direction> possibleDirections = new ArrayList<>();
        if (targetPosition.getRow() < currentPosition.getRow()) {
            possibleDirections.add(Direction.UP);
        } else if (targetPosition.getRow() > currentPosition.getRow()) {
            possibleDirections.add(Direction.DOWN);
        }

        if (targetPosition.getCol() < currentPosition.getCol()) {
            possibleDirections.add(Direction.LEFT);
        } else if (targetPosition.getCol() > currentPosition.getCol()) {
            possibleDirections.add(Direction.RIGHT);
        }

        return possibleDirections;
    }

    private Position getNewPosition(Position currentPosition, Direction direction) {
        Position newPosition = new Position(currentPosition.getRow(), currentPosition.getCol());
        switch (direction) {
            case UP -> newPosition.setRow(newPosition.getRow() - 1);
            case DOWN -> newPosition.setRow(newPosition.getRow() + 1);
            case LEFT -> newPosition.setCol(newPosition.getCol() - 1);
            case RIGHT -> newPosition.setCol(newPosition.getCol() + 1);
        }
        return newPosition;
    }

    private boolean canMoveTo(Position newPosition) {
        int row = newPosition.getRow();
        int col = newPosition.getCol();
        int boardSize = board.getCells().length;

        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return false;
        }

        return board.getCells()[row][col] == null;
    }

    @Override
    public void run() {
        while (!move()) {
            try {
                Thread.sleep(100); // 每次尝试移动前，线程等待一段时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}



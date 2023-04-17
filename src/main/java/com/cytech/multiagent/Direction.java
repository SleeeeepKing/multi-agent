package com.cytech.multiagent;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public static Direction[] getAllDirections() {
        return new Direction[]{UP, DOWN, LEFT, RIGHT};
    }
}

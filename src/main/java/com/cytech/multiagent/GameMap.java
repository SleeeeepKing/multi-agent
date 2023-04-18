package com.cytech.multiagent;

public class GameMap {
    // 使用一个静态变量来保存唯一实例
    private static GameMap instance;

    // 将构造函数设为私有，以防止外部创建实例
    private GameMap() {
        // 初始化地图数据
        for (int i = 0; i < 25; i++) {
            map[i] = 0;
        }
    }

    // 提供一个公共静态方法来获取唯一实例
    public static GameMap getInstance() {
        if (instance == null) {
            instance = new GameMap();
        }
        return instance;
    }

    // 地图数据（一维数组表示5*5的地图）
    private int[] map = new int[25];

    // 其他地图类的方法，例如获取和设置地图上的值
    public int get(int index) {
        return map[index];
    }

    public void set(int index, int value) {
        map[index] = value;
    }

    public void printMap() {
        for (int i = 0; i < 25; i++) {
            System.out.print(map[i] + " ");
            if ((i + 1) % 5 == 0) {
                System.out.println();
            }
        }
    }
}



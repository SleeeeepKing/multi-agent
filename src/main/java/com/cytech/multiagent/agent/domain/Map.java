package com.cytech.multiagent.agent.domain;

public class Map {
    // 使用一个静态变量来保存唯一实例
    private static Map instance;

    // 将构造函数设为私有，以防止外部创建实例
    private Map() {
        // 初始化地图数据
        for (int i = 0; i < 25; i++) {
            map[i] = 0;
        }
    }

    // 提供一个公共静态方法来获取唯一实例
    public static Map getInstance() {
        if (instance == null) {
            instance = new Map();
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
}



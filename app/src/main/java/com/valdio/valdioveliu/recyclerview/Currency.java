package com.valdio.valdioveliu.recyclerview;

public class Currency {
    public int id;
    public String name;

    public Currency(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

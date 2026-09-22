package com.valdio.valdioveliu.recyclerview;

public class Group {
    public int id;
    public String name;

    public Group(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

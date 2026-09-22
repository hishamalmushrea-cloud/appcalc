package com.valdio.valdioveliu.recyclerview;

public class CustomerType {
    public int id;
    public String name;

    public CustomerType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

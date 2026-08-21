package com.valdio.valdioveliu.recyclerview;

public class Customer {
    public int id;
    public String name;
    public String gsm;
    public String groupName;
    public String typeName;
    public double balance;
    public int gId;
    public int typeId;

    public Customer(int id, String name, String gsm, String groupName, String typeName, double balance) {
        this(id, name, gsm, groupName, typeName, balance, 0, 0);
    }

    public Customer(int id, String name, String gsm, String groupName, String typeName,
                    double balance, int gId, int typeId) {
        this.id = id;
        this.name = name;
        this.gsm = gsm;
        this.groupName = groupName;
        this.typeName = typeName;
        this.balance = balance;
        this.gId = gId;
        this.typeId = typeId;
    }
}

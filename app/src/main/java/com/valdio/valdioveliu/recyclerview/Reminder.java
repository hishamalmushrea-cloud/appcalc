package com.valdio.valdioveliu.recyclerview;

public class Reminder {
    public int id;
    public int cusId;
    public String cusName;
    public String date;
    public String time;
    public String notes;

    public Reminder(int id, int cusId, String cusName, String date, String time, String notes) {
        this.id = id;
        this.cusId = cusId;
        this.cusName = cusName;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }
}

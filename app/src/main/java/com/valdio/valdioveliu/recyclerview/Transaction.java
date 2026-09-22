package com.valdio.valdioveliu.recyclerview;

public class Transaction {
    public int id;
    public int cusId;
    public int tCusId;
    public int currId;
    public String currName;
    public String date;
    public String remarks;
    public int dir;          // +1 = له (credit), -1 = عليه (debit)
    public double amount;    // absolute value
    public double signed;    // signed value (dir * amount, transfers negated)

    public Transaction() {
    }

    public Transaction(int id, int cusId, int tCusId, int currId, String currName,
                       String date, String remarks, int dir, double amount, double signed) {
        this.id = id;
        this.cusId = cusId;
        this.tCusId = tCusId;
        this.currId = currId;
        this.currName = currName;
        this.date = date;
        this.remarks = remarks;
        this.dir = dir;
        this.amount = amount;
        this.signed = signed;
    }
}

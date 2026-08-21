package com.valdio.valdioveliu.recyclerview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * طبقة الوصول للبيانات — نفس المنطق المحاسبي في التطبيق الأصلي.
 * القاعدة الذهبية للرصيد (مطابقة للأصل):
 *   signed = CASE WHEN t_cus_id = customer.id THEN -out ELSE (in * out) END
 * حيث in = +1 (له) أو -1 (عليه)، و out = المبلغ.
 */
public class Dao {

    private final DB helper;

    public Dao(Context context) {
        helper = new DB(context.getApplicationContext());
    }

    private SQLiteDatabase r() {
        return helper.getReadableDatabase();
    }

    private SQLiteDatabase w() {
        return helper.getWritableDatabase();
    }

    // ---------- التصنيفات / العملات / الأنواع ----------

    public List<Group> groups() {
        List<Group> list = new ArrayList<>();
        Cursor c = r().rawQuery("SELECT ID AS _id, name FROM groups ORDER BY ID", null);
        while (c.moveToNext()) list.add(new Group(c.getInt(0), c.getString(1)));
        c.close();
        return list;
    }

    public List<Currency> currencies() {
        List<Currency> list = new ArrayList<>();
        Cursor c = r().rawQuery("SELECT ID AS _id, name FROM currency ORDER BY ID", null);
        while (c.moveToNext()) list.add(new Currency(c.getInt(0), c.getString(1)));
        c.close();
        return list;
    }

    public List<CustomerType> types() {
        List<CustomerType> list = new ArrayList<>();
        Cursor c = r().rawQuery("SELECT id AS _id, name FROM cus_type ORDER BY id", null);
        while (c.moveToNext()) list.add(new CustomerType(c.getInt(0), c.getString(1)));
        c.close();
        return list;
    }

    public long addGroup(String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        return w().insert("groups", null, v);
    }

    public long addCurrency(String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        return w().insert("currency", null, v);
    }

    // ---------- العملاء ----------

    public List<Customer> customers(Integer groupId) {
        String sql = "SELECT b.ID AS _id, b.name, b.gsm, c.name AS g_name, e.name AS type_name, " +
                "IFNULL((SELECT SUM(CASE WHEN t.t_cus_id=b.ID THEN -1*CAST(t.`out` AS REAL) " +
                "  ELSE (CAST(t.`in` AS INTEGER)*CAST(t.`out` AS REAL)) END) " +
                "FROM transactions t WHERE t.cus_id=b.ID OR t.t_cus_id=b.ID),0) AS bal " +
                "FROM customers b " +
                "LEFT JOIN groups c ON b.g_id=c.ID " +
                "LEFT JOIN cus_type e ON b.cus_type_id=e.id";
        List<String> args = new ArrayList<>();
        if (groupId != null) {
            sql += " WHERE b.g_id=?";
            args.add(String.valueOf(groupId));
        }
        sql += " ORDER BY b.name COLLATE NOCASE";

        List<Customer> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, args.toArray(new String[0]));
        while (c.moveToNext()) {
            list.add(new Customer(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getDouble(5)));
        }
        c.close();
        return list;
    }

    public Customer customer(int id) {
        Cursor c = r().rawQuery(
                "SELECT b.ID AS _id, b.name, b.gsm, c.name AS g_name, e.name AS type_name, " +
                        "IFNULL((SELECT SUM(CASE WHEN t.t_cus_id=b.ID THEN -1*CAST(t.`out` AS REAL) " +
                        "  ELSE (CAST(t.`in` AS INTEGER)*CAST(t.`out` AS REAL)) END) " +
                        "FROM transactions t WHERE t.cus_id=b.ID OR t.t_cus_id=b.ID),0) AS bal, " +
                        "b.g_id, b.cus_type_id " +
                        "FROM customers b " +
                        "LEFT JOIN groups c ON b.g_id=c.ID " +
                        "LEFT JOIN cus_type e ON b.cus_type_id=e.id " +
                        "WHERE b.ID=?",
                new String[]{String.valueOf(id)});
        Customer cu = null;
        if (c.moveToFirst()) {
            cu = new Customer(c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getDouble(5), c.getInt(6), c.getInt(7));
        }
        c.close();
        return cu;
    }

    public String customerName(int id) {
        Cursor c = r().rawQuery("SELECT name FROM customers WHERE ID=?", new String[]{String.valueOf(id)});
        String name = "";
        if (c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }

    public long addCustomer(String name, String gsm, int groupId, int typeId) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("gsm", gsm);
        v.put("g_id", groupId);
        v.put("cus_type_id", typeId);
        return w().insert("customers", null, v);
    }

    public int updateCustomer(int id, String name, String gsm, int groupId, int typeId) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("gsm", gsm);
        v.put("g_id", groupId);
        v.put("cus_type_id", typeId);
        return w().update("customers", v, "ID=?", new String[]{String.valueOf(id)});
    }

    public int deleteCustomer(int id) {
        w().delete("transactions", "cus_id=? OR t_cus_id=?", new String[]{String.valueOf(id), String.valueOf(id)});
        return w().delete("customers", "ID=?", new String[]{String.valueOf(id)});
    }

    // ---------- العمليات ----------

    public double balance(int cusId) {
        Cursor c = r().rawQuery(
                "SELECT IFNULL(SUM(CASE WHEN t.t_cus_id=? THEN -1*CAST(t.`out` AS REAL) " +
                        "  ELSE (CAST(t.`in` AS INTEGER)*CAST(t.`out` AS REAL)) END),0) " +
                        "FROM transactions t WHERE t.cus_id=? OR t.t_cus_id=?",
                new String[]{String.valueOf(cusId), String.valueOf(cusId), String.valueOf(cusId)});
        double b = 0;
        if (c.moveToFirst()) b = c.getDouble(0);
        c.close();
        return b;
    }

    public List<Transaction> transactions(int cusId) {
        String sql = "SELECT a.ID AS _id, a.cus_id, a.t_cus_id, a.curr_id, d.name AS curr_name, " +
                "a.date_, a.remarks, CAST(a.`in` AS INTEGER) AS dir, CAST(a.`out` AS REAL) AS amt, " +
                "CASE WHEN a.t_cus_id=? THEN -1*CAST(a.`out` AS REAL) " +
                "  ELSE (CAST(a.`in` AS INTEGER)*CAST(a.`out` AS REAL)) END AS signed " +
                "FROM transactions a LEFT JOIN currency d ON a.curr_id=d.id " +
                "WHERE a.cus_id=? OR a.t_cus_id=? " +
                "ORDER BY a.date_ DESC, a.ID DESC";
        String[] args = new String[]{String.valueOf(cusId), String.valueOf(cusId), String.valueOf(cusId)};
        List<Transaction> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, args);
        while (c.moveToNext()) {
            list.add(new Transaction(
                    c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getString(4),
                    c.getString(5), c.getString(6), c.getInt(7), c.getDouble(8), c.getDouble(9)));
        }
        c.close();
        return list;
    }

    public Transaction transaction(int id) {
        Cursor c = r().rawQuery(
                "SELECT ID AS _id, cus_id, t_cus_id, curr_id, date_, remarks, " +
                        "CAST(`in` AS INTEGER) AS dir, CAST(`out` AS REAL) AS amt " +
                        "FROM transactions WHERE ID=?",
                new String[]{String.valueOf(id)});
        Transaction t = null;
        if (c.moveToFirst()) {
            t = new Transaction();
            t.id = c.getInt(0);
            t.cusId = c.getInt(1);
            t.tCusId = c.getInt(2);
            t.currId = c.getInt(3);
            t.date = c.getString(4);
            t.remarks = c.getString(5);
            t.dir = c.getInt(6);
            t.amount = c.getDouble(7);
        }
        c.close();
        return t;
    }

    public long addTransaction(int cusId, int tCusId, int dir, double amount,
                               int currId, String date, String remarks) {
        // ملاحظة: العمودان `in` و `out` كلمتان محجوزتان في SQLite، لذا يجب تنصيصهما
        // بـ backticks وعدم استخدام ContentValues هنا (تولّد SQL غير صالح).
        SQLiteDatabase db = w();
        db.execSQL("INSERT INTO transactions(cus_id, t_cus_id, `in`, `out`, curr_id, date_, remarks, now_) " +
                        "VALUES(?,?,?,?,?,?,?,?)",
                new Object[]{cusId, tCusId, dir, String.valueOf(amount), currId, date, remarks, Fmt.today()});
        long id = -1;
        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        if (c.moveToFirst()) id = c.getLong(0);
        c.close();
        return id;
    }

    public int updateTransaction(int id, int cusId, int tCusId, int dir, double amount,
                                 int currId, String date, String remarks) {
        SQLiteDatabase db = w();
        db.execSQL("UPDATE transactions SET cus_id=?, t_cus_id=?, `in`=?, `out`=?, curr_id=?, date_=?, remarks=? " +
                        "WHERE ID=?",
                new Object[]{cusId, tCusId, dir, String.valueOf(amount), currId, date, remarks, id});
        return 1;
    }

    public int deleteTransaction(int id) {
        return w().delete("transactions", "ID=?", new String[]{String.valueOf(id)});
    }

    /**
     * إغلاق الرصيد: حذف التفاصيل وفتح رصيد افتتاحي بقيمة الرصيد الحالي.
     */
    public long closeAccount(int cusId, String openingText) {
        double net = balance(cusId);
        w().delete("transactions", "cus_id=? OR t_cus_id=?",
                new String[]{String.valueOf(cusId), String.valueOf(cusId)});
        if (Math.abs(net) < 0.0001) {
            return -1;
        }
        int dir = net >= 0 ? 1 : -1;
        return addTransaction(cusId, 0, dir, Math.abs(net), 0, Fmt.today(), openingText);
    }

    // ---------- التقارير ----------

    private String signedExpr(String custAlias, String txnAlias) {
        return "CASE WHEN " + txnAlias + ".t_cus_id=" + custAlias + ".ID THEN -1*CAST(" + txnAlias + ".`out` AS REAL) " +
                "ELSE (CAST(" + txnAlias + ".`in` AS INTEGER)*CAST(" + txnAlias + ".`out` AS REAL)) END";
    }

    public List<ReportRow> reportBalances() {
        String s = signedExpr("b", "t");
        String sql = "SELECT b.ID AS _id, b.name AS name, " +
                "IFNULL(SUM(CASE WHEN (" + s + ")>0 THEN (" + s + ") ELSE 0 END),0) AS cr, " +
                "IFNULL(SUM(CASE WHEN (" + s + ")<0 THEN (" + s + ") ELSE 0 END),0) AS db " +
                "FROM customers b LEFT JOIN transactions t ON (t.cus_id=b.ID OR t.t_cus_id=b.ID) " +
                "GROUP BY b.ID, b.name " +
                "ORDER BY IFNULL(SUM(" + s + "),0) DESC, b.name COLLATE NOCASE";
        List<ReportRow> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, null);
        while (c.moveToNext()) {
            double cr = c.getDouble(2);
            double db = c.getDouble(3);
            list.add(new ReportRow(c.getString(1), "", Fmt.signed(cr), Fmt.signed(db), Fmt.signed(cr + db)));
        }
        c.close();
        return list;
    }

    public List<ReportRow> reportLate() {
        String sql = "SELECT b.ID AS _id, b.name AS name, b.gsm AS gsm, " +
                "IFNULL(ABS(SUM(CAST(a.`out` AS REAL)*CAST(a.`in` AS INTEGER))),0) AS amount, " +
                "MAX(a.date_) AS last_date, " +
                "CAST(julianday('now') - julianday(MAX(substr(a.date_,7,4)||'-'||substr(a.date_,4,2)||'-'||substr(a.date_,1,2))) AS INTEGER) AS days_late " +
                "FROM customers b LEFT JOIN transactions a ON a.cus_id=b.ID " +
                "GROUP BY b.ID, b.name " +
                "HAVING IFNULL(ABS(SUM(CAST(a.`out` AS REAL)*CAST(a.`in` AS INTEGER))),0) <> 0 " +
                "ORDER BY amount DESC";
        List<ReportRow> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, null);
        while (c.moveToNext()) {
            String sub = c.isNull(2) ? "" : c.getString(2);
            String last = c.isNull(4) ? "-" : c.getString(4);
            list.add(new ReportRow(c.getString(1), sub, Fmt.num(c.getDouble(3)), last,
                    c.getInt(5) + ""));
        }
        c.close();
        return list;
    }

    public List<ReportRow> reportMonthly(int cusId) {
        String s = signedExpr("b", "a");
        String sql = "SELECT substr(a.date_,4,2)||'-'||substr(a.date_,7,4) AS ym, " +
                "IFNULL(SUM(" + s + "),0) AS net " +
                "FROM customers b LEFT JOIN transactions a ON (a.cus_id=b.ID OR a.t_cus_id=b.ID) " +
                "WHERE b.ID=? " +
                "GROUP BY ym ORDER BY ym";
        List<ReportRow> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, new String[]{String.valueOf(cusId)});
        while (c.moveToNext()) {
            list.add(new ReportRow(c.getString(0), "", Fmt.signed(c.getDouble(1)), "", ""));
        }
        c.close();
        return list;
    }

    // ---------- البحث ----------

    public List<Customer> search(String q) {
        String like = "%" + q + "%";
        String sql = "SELECT b.ID AS _id, b.name, b.gsm, c.name AS g_name, e.name AS type_name, " +
                "IFNULL((SELECT SUM(CASE WHEN t.t_cus_id=b.ID THEN -1*CAST(t.`out` AS REAL) " +
                "  ELSE (CAST(t.`in` AS INTEGER)*CAST(t.`out` AS REAL)) END) " +
                "FROM transactions t WHERE t.cus_id=b.ID OR t.t_cus_id=b.ID),0) AS bal " +
                "FROM customers b " +
                "LEFT JOIN groups c ON b.g_id=c.ID " +
                "LEFT JOIN cus_type e ON b.cus_type_id=e.id " +
                "WHERE b.name LIKE ? OR b.gsm LIKE ? " +
                "   OR b.ID IN (SELECT cus_id FROM transactions WHERE remarks LIKE ? OR date_ LIKE ?) " +
                "ORDER BY b.name COLLATE NOCASE";
        List<Customer> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, new String[]{like, like, like, like});
        while (c.moveToNext()) {
            list.add(new Customer(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getDouble(5)));
        }
        c.close();
        return list;
    }

    // ---------- التنبيهات ----------

    public List<Reminder> reminders() {
        String sql = "SELECT a.id AS _id, a.cus_id, b.name, a.date_, a.time_, a.notes " +
                "FROM reminders a LEFT JOIN customers b ON a.cus_id=b.id " +
                "ORDER BY a.date_ DESC, a.time_ DESC";
        List<Reminder> list = new ArrayList<>();
        Cursor c = r().rawQuery(sql, null);
        while (c.moveToNext()) {
            list.add(new Reminder(c.getInt(0), c.getInt(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public long addReminder(int cusId, String date, String time, String notes) {
        ContentValues v = new ContentValues();
        v.put("cus_id", cusId);
        v.put("date_", date);
        v.put("time_", time);
        v.put("notes", notes);
        v.put("flag", 0);
        return w().insert("reminders", null, v);
    }

    public int deleteReminder(int id) {
        return w().delete("reminders", "id=?", new String[]{String.valueOf(id)});
    }

    // ---------- النسخ الاحتياطي ----------

    public SQLiteDatabase raw() {
        return w();
    }
}

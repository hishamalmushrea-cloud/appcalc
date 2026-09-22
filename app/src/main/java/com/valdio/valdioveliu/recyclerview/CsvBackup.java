package com.valdio.valdioveliu.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * نسخ احتياطي واسترجاع CSV (عملاء + عمليات) في مجلد التطبيق.
 */
public final class CsvBackup {

    public static final String FILE = "market_backup.csv";

    private CsvBackup() {
    }

    private static File file(Context c) {
        File dir = c.getExternalFilesDir(null);
        if (dir == null) dir = c.getFilesDir();
        return new File(dir, FILE);
    }

    private static String esc(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static boolean exportBackup(Context c) {
        try {
            File out = file(c);
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), "UTF-8");
            SQLiteDatabase db = new DB(c).getWritableDatabase();

            w.write("#customers\n");
            w.write("id,name,gsm,g_id,cus_type_id\n");
            Cursor cu = db.rawQuery("SELECT ID,name,gsm,g_id,cus_type_id FROM customers", null);
            while (cu.moveToNext()) {
                w.write(cu.getInt(0) + "," + esc(cu.getString(1)) + "," + esc(cu.getString(2)) +
                        "," + cu.getInt(3) + "," + cu.getInt(4) + "\n");
            }
            cu.close();

            w.write("#transactions\n");
            w.write("id,cus_id,t_cus_id,in,out,curr_id,date_,remarks\n");
            Cursor tr = db.rawQuery("SELECT ID,cus_id,t_cus_id,`in`,`out`,curr_id,date_,remarks FROM transactions", null);
            while (tr.moveToNext()) {
                w.write(tr.getInt(0) + "," + tr.getInt(1) + "," + tr.getInt(2) + "," + tr.getInt(3) + "," +
                        esc(tr.getString(4)) + "," + tr.getInt(5) + "," + esc(tr.getString(6)) + "," +
                        esc(tr.getString(7)) + "\n");
            }
            tr.close();
            w.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean importBackup(Context c) {
        File in = file(c);
        if (!in.exists()) return false;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(in), "UTF-8"));
            SQLiteDatabase db = new DB(c).getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM transactions");
                db.execSQL("DELETE FROM customers");

                String line;
                String section = "";
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("#")) {
                        section = line;
                        continue;
                    }
                    if (line.trim().isEmpty()) continue;
                    String[] cols = parseCsv(line);
                    if ("#customers".equals(section)) {
                        db.execSQL("INSERT OR IGNORE INTO customers(ID,name,gsm,g_id,cus_type_id) VALUES(?,?,?,?,?)",
                                new Object[]{Integer.parseInt(cols[0]), cols[1], cols[2],
                                        Integer.parseInt(cols[3]), Integer.parseInt(cols[4])});
                    } else if ("#transactions".equals(section)) {
                        db.execSQL("INSERT OR IGNORE INTO transactions(ID,cus_id,t_cus_id,`in`,`out`,curr_id,date_,remarks) VALUES(?,?,?,?,?,?,?,?)",
                                new Object[]{Integer.parseInt(cols[0]), Integer.parseInt(cols[1]),
                                        Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), cols[4],
                                        Integer.parseInt(cols[5]), cols[6], cols[7]});
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            r.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String[] parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQ = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else if (ch == '"') {
                inQ = true;
            } else if (ch == ',') {
                out.add(cur.toString());
                cur = new StringBuilder();
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}

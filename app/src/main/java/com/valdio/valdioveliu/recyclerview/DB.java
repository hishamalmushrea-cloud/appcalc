package com.valdio.valdioveliu.recyclerview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * قاعدة بيانات «دفتر الحسابات» — مطابقة لمخطط الأصل (market.db)
 * مع الاحتفاظ بنفس أسماء الجداول والأعمدة لضمان التوافق مع النسخ الاحتياطية القديمة.
 */
public class DB extends SQLiteOpenHelper {

    public static final String NAME = "market.db";
    public static final int VERSION = 1;

    public DB(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE customers (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "gsm TEXT, " +
                "g_id INTEGER DEFAULT 0, " +
                "param1 TEXT, param2 TEXT, " +
                "f1 TEXT, f2 TEXT, f3 TEXT, " +
                "cus_type_id INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE transactions (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cus_id INTEGER, " +
                "`in` INTEGER, " +
                "`out` TEXT, " +
                "date_ TEXT, " +
                "remarks TEXT, " +
                "now_ TEXT, " +
                "param1 TEXT, param2 TEXT, " +
                "t_cus_id INTEGER, " +
                "f1 TEXT, f2 TEXT, f3 TEXT, " +
                "curr_id INTEGER DEFAULT 0, " +
                "online INTEGER DEFAULT 0, " +
                "online_ref TEXT)");

        db.execSQL("CREATE TABLE groups (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "param1 TEXT, param2 TEXT)");

        db.execSQL("CREATE TABLE currency (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "param1 TEXT, param2 TEXT)");

        db.execSQL("CREATE TABLE cus_type (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT UNIQUE, " +
                "param1 TEXT, param2 TEXT)");

        db.execSQL("CREATE TABLE cus_limit (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cus_id INTEGER, " +
                "curr_id INTEGER, " +
                "cr TEXT, " +
                "db TEXT)");

        db.execSQL("CREATE TABLE reminders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cus_id INTEGER, " +
                "date_ TEXT, " +
                "time_ TEXT, " +
                "flag INTEGER DEFAULT 0, " +
                "notes TEXT)");

        db.execSQL("CREATE TABLE contacts (" +
                "id INTEGER, " +
                "name TEXT, " +
                "type_ INTEGER, " +
                "sort_id INTEGER, " +
                "UNIQUE(name, type_))");

        db.execSQL("CREATE TABLE valid (imei TEXT, imei_code TEXT)");

        db.execSQL("CREATE TABLE act_req (" +
                "imei TEXT, name TEXT, phone TEXT, country TEXT, email TEXT, " +
                "status INTEGER, date_ TEXT, reply TEXT, user_reply TEXT, reply_from TEXT)");

        db.execSQL("CREATE TABLE requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "status INTEGER, " +
                "err TEXT)");

        db.execSQL("CREATE TABLE sys_conf (id INTEGER, value_ TEXT)");

        db.execSQL("CREATE INDEX tr_ind_cus_id ON transactions(cus_id)");
        db.execSQL("CREATE INDEX tr_ind_t_cus_id ON transactions(t_cus_id)");
        db.execSQL("CREATE INDEX tr_ind_curr_id ON transactions(curr_id)");
        db.execSQL("CREATE INDEX cus_ind_g_id ON customers(g_id)");

        // بيانات أولية (مطابقة للأصل)
        db.execSQL("INSERT INTO groups (ID, name) VALUES (0, 'عام')");
        db.execSQL("INSERT INTO groups (ID, name) VALUES (1, 'عملاء')");
        db.execSQL("INSERT INTO groups (ID, name) VALUES (2, 'موردين')");

        db.execSQL("INSERT INTO currency (ID, name) VALUES (0, 'محلي')");
        db.execSQL("INSERT INTO currency (ID, name) VALUES (1, 'دولار')");
        db.execSQL("INSERT INTO currency (ID, name) VALUES (2, 'سعودي')");

        db.execSQL("INSERT INTO cus_type (id, name) VALUES (0, 'عميل')");
        db.execSQL("INSERT INTO cus_type (id, name) VALUES (1, 'مورد')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // للأمان: إعادة إنشاء المخطط في هذه النسخة التجريبية.
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS customers");
        db.execSQL("DROP TABLE IF EXISTS groups");
        db.execSQL("DROP TABLE IF EXISTS currency");
        db.execSQL("DROP TABLE IF EXISTS cus_type");
        db.execSQL("DROP TABLE IF EXISTS cus_limit");
        db.execSQL("DROP TABLE IF EXISTS reminders");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS valid");
        db.execSQL("DROP TABLE IF EXISTS act_req");
        db.execSQL("DROP TABLE IF EXISTS requests");
        db.execSQL("DROP TABLE IF EXISTS sys_conf");
        onCreate(db);
    }
}

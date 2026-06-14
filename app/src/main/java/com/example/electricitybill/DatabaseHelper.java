package com.example.electricitybill;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME    = "electricity_bill.db";
    public static final int    DATABASE_VERSION = 2; // bumped to force onUpgrade

    // Table
    public static final String TABLE_BILLS   = "bills";
    public static final String TABLE_NAME    = TABLE_BILLS;
    public static final String TABLE         = TABLE_BILLS;

    // Columns
    public static final String COL_ID            = "id";
    public static final String COL_MONTH         = "month";
    public static final String COL_UNIT          = "unit";
    public static final String COL_UNITS         = COL_UNIT;
    public static final String COL_REBATE        = "rebate";
    public static final String COL_REBATE_PCT    = COL_REBATE;
    public static final String COL_TOTAL_CHARGES = "total_charges";
    public static final String COL_TOTAL         = COL_TOTAL_CHARGES;
    public static final String COL_FINAL_COST    = "final_cost";
    public static final String COL_FINAL         = COL_FINAL_COST;

    private static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_BILLS + " (" +
        COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_MONTH         + " TEXT NOT NULL, "  +
        COL_UNIT          + " REAL NOT NULL, "  +
        COL_REBATE        + " REAL NOT NULL, "  +
        COL_TOTAL_CHARGES + " REAL NOT NULL, "  +
        COL_FINAL_COST    + " REAL NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Table created: " + TABLE_BILLS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading DB from v" + oldVersion + " to v" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }
}

package com.arghya.expensetrackerapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String PREFS_NAME = "ExpenseTrackerPrefs";
    public static final String LAST_INCOME_UPDATE = "lastIncomeUpdate";
    public static final String LAST_EXPENSE_UPDATE = "lastExpenseUpdate";
    public static final String LAST_BALANCE_UPDATE = "lastBalanceUpdate";
    private Context context;

    public DatabaseHelper(@Nullable Context context) {
        super(context, "khoroch_app", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE expense (id INTEGER PRIMARY KEY AUTOINCREMENT, amount DOUBLE, reason TEXT, time LONG)");
        db.execSQL("CREATE TABLE income (id INTEGER PRIMARY KEY AUTOINCREMENT, amount DOUBLE, reason TEXT, time LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS expense");
        db.execSQL("DROP TABLE IF EXISTS income");
        onCreate(db);
    }


//    ======================= Add Income =================================

    public void addIncome(Double amount, String reason, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", timestamp);

        db.insert("income", null, conval);
        updateLastUpdatedTime(LAST_INCOME_UPDATE, System.currentTimeMillis());
        updateLastUpdatedTime(LAST_BALANCE_UPDATE, System.currentTimeMillis());
    }


//    =================================== Add Expense ==============================================

    public void addExpense(Double amount, String reason, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues conval = new ContentValues();
        conval.put("amount", amount);
        conval.put("reason", reason);
        conval.put("time", timestamp);

        db.insert("expense", null, conval);
        updateLastUpdatedTime(LAST_EXPENSE_UPDATE, System.currentTimeMillis());
        updateLastUpdatedTime(LAST_BALANCE_UPDATE, System.currentTimeMillis());
    }


    //==============Monthly Features================
    public Cursor getMonthlyIncomeData(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDate = year + "-" + String.format("%02d", month) + "-01";
        String endDate = year + "-" + String.format("%02d", month + 1) + "-01";

        String query = "SELECT * FROM income WHERE datetime(time/1000, 'unixepoch') >= datetime(?) " +
                "AND datetime(time/1000, 'unixepoch') < datetime(?) ORDER BY time DESC";

        return db.rawQuery(query, new String[]{startDate, endDate});
    }

    public Cursor getMonthlyExpenseData(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDate = year + "-" + String.format("%02d", month) + "-01";
        String endDate = year + "-" + String.format("%02d", month + 1) + "-01";

        String query = "SELECT * FROM expense WHERE datetime(time/1000, 'unixepoch') >= datetime(?) " +
                "AND datetime(time/1000, 'unixepoch') < datetime(?) ORDER BY time DESC";

        return db.rawQuery(query, new String[]{startDate, endDate});
    }

    public double getMonthlyTotalIncome(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDate = year + "-" + String.format("%02d", month) + "-01";
        String endDate = year + "-" + String.format("%02d", month + 1) + "-01";

        String query = "SELECT SUM(amount) FROM income WHERE datetime(time/1000, 'unixepoch') >= datetime(?) " +
                "AND datetime(time/1000, 'unixepoch') < datetime(?)";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0;
    }

    public double getMonthlyTotalExpense(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDate = year + "-" + String.format("%02d", month) + "-01";
        String endDate = year + "-" + String.format("%02d", month + 1) + "-01";

        String query = "SELECT SUM(amount) FROM expense WHERE datetime(time/1000, 'unixepoch') >= datetime(?) " +
                "AND datetime(time/1000, 'unixepoch') < datetime(?)";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0;
    }

    public void updateLastUpdatedTime(String key, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, time);
        editor.apply();
    }

    public long getLastUpdatedTime(String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(key, 0);
    }

    //    ================================= Get Total Income ==========================
    public double getTotalIncome() {
        double totalIncome = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM income", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalIncome = totalIncome + amount;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return totalIncome;
    }

    //    ================================== Get Total Expense ==========================================

    public double getTotalExpense() {
        double totalExpense = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM expense", null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(1);
                totalExpense = totalExpense + amount;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return totalExpense;
    }

    //    ======================================= Get All Income Data  ======================================================
    public Cursor getAllIncomeData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM income ORDER BY time DESC", null);
    }

    //    ======================================= Get All Expense Data  ======================================================
    public Cursor getAllExpenseData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM expense ORDER BY time DESC", null);
    }

    //    ======================================= Delete Expense ==============================================================
    public void deleteExpense(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM expense WHERE id = " + id);
        updateLastUpdatedTime(LAST_EXPENSE_UPDATE, System.currentTimeMillis());
        updateLastUpdatedTime(LAST_BALANCE_UPDATE, System.currentTimeMillis());
    }

    //    ======================================= Delete Income ==============================================================
    public void deleteIncome(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM income WHERE id = " + id);
        updateLastUpdatedTime(LAST_INCOME_UPDATE, System.currentTimeMillis());
        updateLastUpdatedTime(LAST_BALANCE_UPDATE, System.currentTimeMillis());
    }

    public List<HashMap<String, String>> getRecentTransactions(int limit) {
        List<HashMap<String, String>> recentTransactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT 'income' AS type, id, amount, reason, time FROM income " +
                "UNION ALL " +
                "SELECT 'expense' AS type, id, amount, reason, time FROM expense " +
                "ORDER BY time DESC LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> transaction = new HashMap<>();
                transaction.put("type", cursor.getString(0));
                transaction.put("id", cursor.getString(1));
                transaction.put("amount", cursor.getString(2));
                transaction.put("reason", cursor.getString(3));
                transaction.put("time", cursor.getString(4));
                recentTransactions.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return recentTransactions;
    }


}

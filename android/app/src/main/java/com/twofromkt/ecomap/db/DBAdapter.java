package com.twofromkt.ecomap.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.twofromkt.ecomap.Consts;

import java.io.File;

import static com.twofromkt.ecomap.Consts.*;

public class DBAdapter {

    private static String[] schemas = new String[CATEGORIES_NUMBER];
    static final String dbPath = "db/", diffPath = "diff/", tableName = "Info";
    private static final String[][] tabNames = {
            {"id", "lat", "lng", "rate", "title", "content_text", "address", "img_link", "info",
                    "work_time", "site", "telephone", "e_mail"},
            {"address", "lat", "lng", "district", "timetable"},
            {"id"}};
    private static final String[][] tabTypes = {
            {"INT PRIMARY KEY", "DOUBLE", "DOUBLE", "DOUBLE", "TEXT", "TEXT", "TEXT", "TEXT"
                    , "TEXT", "TEXT", "TEXT", "TEXT", "TEXT"},
            {"TEXT PRIMARY KEY", "DOUBLE", "DOUBLE", "TEXT", "TEXT"},
            {"INT PRIMARY KEY"}};
    private static final int[] TAB_N = {
            tabNames[TRASH_ID].length,
            tabNames[ECOMOBILE_ID].length,
            tabNames[OTHER_ID].length};

    public static void replace(int num, Context c) {
        initSchema();
        new File(c.getFilesDir(), dbPath).mkdir();
        new File(c.getFilesDir(), diffPath).mkdir();
        try (
                SQLiteDatabase diff = SQLiteDatabase.openOrCreateDatabase(
                        new File(c.getFilesDir(), diffPath + "diff" + num + ".db"), null);
                SQLiteDatabase curr = SQLiteDatabase.openOrCreateDatabase(
                        new File(c.getFilesDir(), dbPath + FILE_NAMES[num] + ".db"), null)
        ) {
            if (isEmpty(diff)) {
                return;
            }
            curr.execSQL(schemas[num]);
            curr.beginTransactionNonExclusive();
            try (Cursor all = diff.rawQuery("SELECT * FROM " + tableName + ";", null)) {
                if (!all.moveToFirst())
                    return;
                do {
//                    cnt++;
                    // TODO please change this to normal code
                    if (num == 0) { // recycle
                        int id = all.getInt(0);
                        boolean toDelete = all.getString(all.getColumnCount() - 1).contains("DELETE");
                        if (toDelete) {
                            curr.execSQL("DELETE FROM " + tableName + " WHERE id=" + id);
                            continue;
                        }
                        String val = collectAllColumns(all, num);
                        curr.execSQL(getInsertScheme(num, val, true));
                    } else if (num == 1) { // ecomobile
                        String address = all.getString(0);
                        boolean toDelete = all.getString(all.getColumnCount() - 1).contains("DELETE");
                        if (toDelete) {
                            curr.execSQL("DELETE FROM " + tableName + " WHERE address=\'" + address + "\'");
                            continue;
                        }
                        String val = collectAllColumns(all, num);
                        Log.d("REPLACE", val);
                        curr.execSQL(getInsertScheme(num, val, true));
                    }
                } while (all.moveToNext());
//                Log.d("REPLACE", "replace cursor did " + cnt + " iterations");
            }
            curr.setTransactionSuccessful();
            curr.endTransaction();
        }
    }

    private static void initSchema() {
        if (schemas[0] != null) {
            return;
        }
        for (int tr = 0; tr < CATEGORIES_NUMBER; tr++) {
            schemas[tr] = "CREATE TABLE " + "if not exists " + tableName + "(";
            for (int i = 0; i < TAB_N[tr]; i++) {
                schemas[tr] += tabNames[tr][i] + " " + tabTypes[tr][i] + ((i == TAB_N[tr] - 1) ? "" : ", ");
            }
            schemas[tr] += ");";
        }
    }

    static boolean isEmpty(SQLiteDatabase db) {
        try (Cursor diffCurs = db.
                rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" +
                        tableName + "';", null)) {
            diffCurs.moveToFirst();
            return diffCurs.getCount() == 0;
        }
    }

    public static String getDiffPath() {
        return diffPath;
    }

    public static String getColumnName(int category, int i) {
        return tabNames[category][i];
    }

    static String getPathToDb(int num) {
        return dbPath + FILE_NAMES[num] + ".db";
    }

    private static String getInsertScheme(int number, String s, boolean replace) {
        String sch = (replace ? "REPLACE" : "INSERT") + " INTO " + tableName + " (";
        for (int i = 0; i < tabNames[number].length; i++) {
            sch += tabNames[number][i] + ((i == tabNames[number].length - 1) ? ")" : ", ");
        }
        sch += " VALUES (\'" + s + ");";
        return sch;
    }

    private static String collectAllColumns(Cursor c, int num) {
        String ans = "";
        for (int i = 0; i < c.getColumnCount(); i++) {
            ans += c.getString(i) + ((i == TAB_N[num] - 1) ? "\'" : "\', \'");
        }
        return ans;
    }
}

package com.weathercock.profilepicker_plus.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.PasswordEntryDao;
import com.weathercock.profilepicker_plus.dao.PasswordFieldDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;

/**
 * Profileデータベースのヘルパークラス
 * 
 * @author Kensuke
 * 
 */
public class ProfileDbOpenHelper extends SQLiteOpenHelper {

    /**
     * データベースバージョン
     */
    final public static int DB_VERSION = 2;
    /**
     * データベースファイル名
     */
    final public static String DB_NAME = "ProfilePickeR.db";

    /**
     * ヘルパーのコンストラクタ
     * 
     * @param context
     */
    public ProfileDbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //名前テーブルを作成
        createNameTable(db);
        //プロフィールテーブルを作成
        createProfileTable(db);
        //パスワードテーブルを作成
        createPasswordTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            if (oldVersion <= 1) {
                // パスワード機能実装時のテーブル追加
                createPasswordTable(db);
            }
        }
    }

    /**
     * 名前テーブルを作成
     * 
     * @param db
     */
    private void createNameTable(SQLiteDatabase db) {
        //名前テーブルを作成
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NameDao.TABLE_NAME + " (" + NameDao.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NameDao.FIRST_NAME + " TEXT, " + NameDao.LAST_NAME + " TEXT, " + NameDao.READING_FIRST_NAME + " TEXT, " + NameDao.READING_LAST_NAME + " TEXT);");
    }

    /**
     * プロフィールテーブルを作成
     * 
     * @param db
     */
    private void createProfileTable(SQLiteDatabase db) {
        //プロフィールテーブルを作成
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ProfileDao.TABLE_NAME + " (" + ProfileDao.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ProfileDao.COLUMN_CATEGORY + " INTEGER, " + ProfileDao.COLUMN_CONTENT_TAG + " TEXT, " + ProfileDao.COLUMN_CONTENT_TEXT + " TEXT, " + ProfileDao.COLUMN_ALLOW_SHARE + " BOOLEAN, " + ProfileDao.COLUMN_ORDER_INDEX + " INTEGER);");
    }

    /**
     * パスワードテーブルを作成
     * 
     * @param db
     */
    private void createPasswordTable(SQLiteDatabase db) {
        //パスワードエントリーテーブルを作成
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PasswordEntryDao.TABLE_NAME + " (" + PasswordEntryDao.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PasswordEntryDao.COLUMN_TITLE + " TEXT, " + PasswordEntryDao.COLUMN_ORDER_INDEX + " INTEGER);");
        //パスワードフィールドテーブルを作成
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PasswordFieldDao.TABLE_NAME + " (" + PasswordFieldDao.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PasswordFieldDao.COLUMN_PARENT_ID + " INTEGER, " + PasswordFieldDao.COLUMN_CONTENT_TAG + " TEXT, " + PasswordFieldDao.COLUMN_CONTENT_TEXT + " TEXT, " + PasswordFieldDao.COLUMN_ORDER_INDEX + " INTEGER);");
    }
}

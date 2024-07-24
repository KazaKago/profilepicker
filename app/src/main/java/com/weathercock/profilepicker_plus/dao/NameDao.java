package com.weathercock.profilepicker_plus.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.weathercock.profilepicker_plus.entity.NameEntity;

/**
 * 名前のデータベースDAO
 * 
 * @author Kensuke
 * 
 */
public class NameDao {

    /**
     * DBテーブル名
     */
    final public static String TABLE_NAME = "name";

    /**
     * DBカラム ID
     */
    final public static String ID = "id";
    /**
     * DBカラム 名前
     */
    final public static String FIRST_NAME = "first_name";
    /**
     * DBカラム 苗字
     */
    final public static String LAST_NAME = "last_name";
    /**
     * DBカラム 名前読み仮名
     */
    final public static String READING_FIRST_NAME = "reading_first_name";
    /**
     * DBカラム 苗字読み仮名
     */
    final public static String READING_LAST_NAME = "reading_last_name";

    /**
     * データベースインスタンス
     */
    private final SQLiteDatabase mDb;

    /**
     * コンストラクタ
     * 
     * @param db
     */
    public NameDao(SQLiteDatabase db) {
        this.mDb = db;
    }

    /**
     * 名前を取得する。取得件数は常に1件のみ
     * 
     * @return
     */
    public NameEntity select() {
        NameEntity entity = null;

        //抽出条件の指定
        String table = TABLE_NAME;
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;

        //抽出
        Cursor cursor = null;
        try {
            cursor = mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            entity = readCursor(cursor);
        } finally {
            if (cursor != null) cursor.close();
        }

        return entity;
    }

    /**
     * Cursorからレコードを生成し返す
     * 
     * @param cursor
     * @return
     */
    private NameEntity readCursor(Cursor cursor) {
        NameEntity entity = new NameEntity();
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(ID));
            String firstName = cursor.getString(cursor.getColumnIndex(FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(LAST_NAME));
            String readingFirstName = cursor.getString(cursor.getColumnIndex(READING_FIRST_NAME));
            String readingLastName = cursor.getString(cursor.getColumnIndex(READING_LAST_NAME));
            entity = new NameEntity(id, firstName, lastName, readingFirstName, readingLastName);
        }
        return entity;
    }

    /**
     * 名前を保存する。保存件数は常に1件のみ。(古い情報は破棄される)
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public long insert(NameEntity entity) throws SQLiteException {
        long result = mDb.delete(TABLE_NAME, null, null);
        if (result < 0) return result;

        ContentValues values = new ContentValues();
        values.put(FIRST_NAME, entity.getFirstName());
        values.put(LAST_NAME, entity.getLastName());
        values.put(READING_FIRST_NAME, entity.getReadingFirstName());
        values.put(READING_LAST_NAME, entity.getReadingLastName());
        return mDb.insert(TABLE_NAME, null, values);
    }

}

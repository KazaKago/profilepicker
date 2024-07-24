package com.weathercock.profilepicker_plus.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.weathercock.profilepicker_plus.entity.PasswordEntryEntity;

/**
 * パスワードエントリーのデータベースDAO
 * 
 * @author Kensuke
 * 
 */
public class PasswordEntryDao {

    /**
     * DBテーブル名
     */
    final public static String TABLE_NAME = "password_entry";

    /**
     * DBカラム名 ID
     */
    final public static String COLUMN_ID = "id";
    /**
     * DBカラム名 タイトル
     */
    final public static String COLUMN_TITLE = "title";
    /**
     * DBカラム名 並び順
     */
    final public static String COLUMN_ORDER_INDEX = "order_index";

    /**
     * データベースインスタンス
     */
    private final SQLiteDatabase mDb;

    /**
     * コンストラクタ
     * 
     * @param db
     */
    public PasswordEntryDao(SQLiteDatabase db) {
        this.mDb = db;
    }

    /**
     * パスワードエントリーリストを取得する。
     * 
     * @return
     */
    public ArrayList<PasswordEntryEntity> selectAll() {
        ArrayList<PasswordEntryEntity> entityList = new ArrayList<PasswordEntryEntity>();

        //抽出条件の指定
        String table = TABLE_NAME;
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = COLUMN_ORDER_INDEX + " ASC";

        //抽出
        Cursor cursor = null;
        try {
            cursor = mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            entityList = readCursor(cursor);
        } finally {
            if (cursor != null) cursor.close();
        }

        return entityList;
    }

    /**
     * Cursorからレコードリストを生成し返す
     * 
     * @param cursor
     * @return
     */
    private ArrayList<PasswordEntryEntity> readCursor(Cursor cursor) {
        ArrayList<PasswordEntryEntity> entityList = new ArrayList<PasswordEntryEntity>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            int order = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_INDEX));

            PasswordEntryEntity entity = new PasswordEntryEntity(id, title, order);
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * 新しいパスワードエントリーを追加する。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public long insert(PasswordEntryEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, entity.getTitle());
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.insert(TABLE_NAME, null, values);
    }

    /**
     * IDを指定してパスワードエントリーを上書きする。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public int update(PasswordEntryEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, entity.getId());
        values.put(COLUMN_TITLE, entity.getTitle());
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(entity.getId()) });
    }

    /**
     * 指定されたパスワードエントリーと紐づくフィールドを削除する
     * 
     * @param id
     * @return
     * @throws SQLiteException
     */
    public int deleteById(int id) throws SQLiteException {
        int result = mDb.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        if (result < 0) return result;

        PasswordFieldDao passwordFieldDao = new PasswordFieldDao(mDb);
        return passwordFieldDao.deleteAll(id);
    }

    /**
     * パスワードエントリーとフィールドを全件削除する
     * 
     * @return
     * @throws SQLiteException
     */
    public int deleteAll() throws SQLiteException {
        int result = mDb.delete(TABLE_NAME, null, null);
        if (result < 0) return result;

        PasswordFieldDao passwordFieldDao = new PasswordFieldDao(mDb);
        return passwordFieldDao.deleteAll();
    }

}

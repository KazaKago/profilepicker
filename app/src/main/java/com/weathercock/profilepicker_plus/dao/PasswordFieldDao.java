package com.weathercock.profilepicker_plus.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.weathercock.profilepicker_plus.entity.PasswordFieldEntity;

/**
 * パスワードフィールドのデータベースDAO
 * 
 * @author Kensuke
 * 
 */
public class PasswordFieldDao {

    /**
     * DBテーブル名
     */
    final public static String TABLE_NAME = "password_field";

    /**
     * DBカラム名 ID
     */
    final public static String COLUMN_ID = "id";
    /**
     * DBカラム名 親ID
     */
    final public static String COLUMN_PARENT_ID = "parent_id";
    /**
     * DBカラム名 タグテキスト
     */
    final public static String COLUMN_CONTENT_TAG = "content_tag";
    /**
     * DBカラム名 内容テキスト
     */
    final public static String COLUMN_CONTENT_TEXT = "content_text";
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
    public PasswordFieldDao(SQLiteDatabase db) {
        this.mDb = db;
    }

    /**
     * 指定されたパスワードフィールドリストを取得する。
     * 
     * @param _parentId
     * @return
     */
    public ArrayList<PasswordFieldEntity> selectByParentId(int _parentId) {
        ArrayList<PasswordFieldEntity> entityList = new ArrayList<PasswordFieldEntity>();

        //抽出条件の指定
        String table = TABLE_NAME;
        String[] columns = null;
        String selection = COLUMN_PARENT_ID;
        String[] selectionArgs = new String[] { String.valueOf(_parentId) };
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
    private ArrayList<PasswordFieldEntity> readCursor(Cursor cursor) {
        ArrayList<PasswordFieldEntity> entityList = new ArrayList<PasswordFieldEntity>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            int parentId = cursor.getInt(cursor.getColumnIndex(COLUMN_PARENT_ID));
            String contentTag = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_TAG));
            String contentText = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_TEXT));
            int order = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_INDEX));

            PasswordFieldEntity entity = new PasswordFieldEntity(id, parentId, contentTag, contentText, order);
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * 新しいパスワードフィールドを追加する。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public long insert(PasswordFieldEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARENT_ID, entity.getParentId());
        values.put(COLUMN_CONTENT_TAG, entity.getContentTag());
        values.put(COLUMN_CONTENT_TEXT, entity.getContentText());
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.insert(TABLE_NAME, null, values);
    }

    /**
     * IDを指定してパスワードフィールドを上書きする。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public long update(PasswordFieldEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, entity.getId());
        values.put(COLUMN_PARENT_ID, entity.getParentId());
        values.put(COLUMN_CONTENT_TAG, entity.getContentTag());
        values.put(COLUMN_CONTENT_TEXT, entity.getContentText());
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(entity.getId()) });
    }

    /**
     * 指定されたIDのパスワードフィールドを削除する
     * 
     * @param id
     * @return
     * @throws SQLiteException
     */
    public int deleteById(int id) throws SQLiteException {
        return mDb.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
    }

    /**
     * 指定された親IDをもつパスワードフィールドを全件削除する
     * 
     * @param parentId
     * @return
     * @throws SQLiteException
     */
    public int deleteAll(int parentId) throws SQLiteException {
        return mDb.delete(TABLE_NAME, COLUMN_PARENT_ID + " = ?", new String[] { String.valueOf(parentId) });
    }

    /**
     * パスワードフィールドを全件削除する
     * 
     * @return
     * @throws SQLiteException
     */
    public int deleteAll() throws SQLiteException {
        return mDb.delete(TABLE_NAME, null, null);
    }

}

package com.weathercock.profilepicker_plus.dao;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.util.CommonUtil;

/**
 * プロフィールのデータベースDAO
 * 
 * @author Kensuke
 * 
 */
public class ProfileDao {

    /**
     * DBテーブル名
     */
    final public static String TABLE_NAME = "profile";

    /**
     * DBカラム名 ID
     */
    final public static String COLUMN_ID = "id";
    /**
     * DBカラム名 種類
     */
    final public static String COLUMN_CATEGORY = "category";
    /**
     * DBカラム名 タグテキスト
     */
    final public static String COLUMN_CONTENT_TAG = "content_tag";
    /**
     * DBカラム名 内容テキスト
     */
    final public static String COLUMN_CONTENT_TEXT = "content_text";
    /**
     * DBカラム名 共有可否判定
     */
    final public static String COLUMN_ALLOW_SHARE = "allow_share";
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
    public ProfileDao(SQLiteDatabase db) {
        this.mDb = db;
    }

    /**
     * 指定されたカテゴリーのプロフィールリストを取得する。
     * 
     * @param selectCategory
     * @return
     */
    public ArrayList<ProfileEntity> selectByCategory(int selectCategory) {
        ArrayList<ProfileEntity> entityList = new ArrayList<ProfileEntity>();

        //抽出条件の指定
        String table = TABLE_NAME;
        String[] columns = null;
        String selection = COLUMN_CATEGORY + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(selectCategory) };
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
     * 指定されたカテゴリーの共有を許可しているプロフィールリストだけを取得する。
     * 
     * @param selectCategory
     * @return
     */
    public ArrayList<ProfileEntity> selectByCategoryOnlyAllowShare(int selectCategory) {
        ArrayList<ProfileEntity> entityList = new ArrayList<ProfileEntity>();

        //抽出条件の指定
        String table = TABLE_NAME;
        String[] columns = null;
        String selection = COLUMN_CATEGORY + " = ? AND " + COLUMN_ALLOW_SHARE + " = ?";
        String[] selectionArgs = new String[] { String.valueOf(selectCategory), String.valueOf(CommonUtil.booleanToInt(true)) };
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
    private ArrayList<ProfileEntity> readCursor(Cursor cursor) {
        ArrayList<ProfileEntity> entityList = new ArrayList<ProfileEntity>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            int category = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY));
            String contentTag = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_TAG));
            String contentText = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_TEXT));
            Boolean allowShare = CommonUtil.intToBoolean(cursor.getInt(cursor.getColumnIndex(COLUMN_ALLOW_SHARE)));
            int order = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_INDEX));

            ProfileEntity entity = new ProfileEntity(id, category, contentTag, contentText, allowShare, order);
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * 新しいプロフィールを追加する。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public long insert(ProfileEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, entity.getCategory());
        values.put(COLUMN_CONTENT_TAG, entity.getContentTag());
        values.put(COLUMN_CONTENT_TEXT, entity.getContentText());
        values.put(COLUMN_ALLOW_SHARE, CommonUtil.booleanToInt(entity.getAllowShare()));
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.insert(TABLE_NAME, null, values);
    }

    /**
     * IDを指定してプロフィールを上書きする。
     * 
     * @param entity
     * @return
     * @throws SQLiteException
     */
    public int update(ProfileEntity entity) throws SQLiteException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, entity.getId());
        values.put(COLUMN_CATEGORY, entity.getCategory());
        values.put(COLUMN_CONTENT_TAG, entity.getContentTag());
        values.put(COLUMN_CONTENT_TEXT, entity.getContentText());
        values.put(COLUMN_ALLOW_SHARE, CommonUtil.booleanToInt(entity.getAllowShare()));
        values.put(COLUMN_ORDER_INDEX, entity.getOrderIndex());
        return mDb.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(entity.getId()) });
    }

    /**
     * 該当するIDのプロフィールを削除する
     * 
     * @param id
     * @return
     * @throws SQLiteException
     */
    public int deleteById(int id) throws SQLiteException {
        return mDb.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
    }

    /**
     * プロフィールを全件削除する
     * 
     * @return
     * @throws SQLiteException
     */
    public int deleteAll() throws SQLiteException {
        return mDb.delete(TABLE_NAME, null, null);
    }

}

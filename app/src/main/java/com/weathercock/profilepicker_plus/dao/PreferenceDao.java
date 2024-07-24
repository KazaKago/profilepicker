package com.weathercock.profilepicker_plus.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 設定関係のDAO
 * 
 * @author Kensuke
 * 
 */
public class PreferenceDao {

    /**
     * SharedPreference名
     */
    final private static String PREF_KEY = "ProfilePickeR";
    /**
     * カテゴリ別のインデックスを保存しておくためのキー(カテゴリIDと組み合わせてキーとする)
     */
    final public static String PREF_CATEGORY_INDEX = "pref_category_index_";

    /**
     * プレファレンス読み取りインスタンス
     */
    private final SharedPreferences mPref;
    /**
     * プレファレンス書き込みインスタンス
     */
    private final SharedPreferences.Editor mEdit;

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
    }

    /**
     * 読み取り専用モード
     * 
     * @param pref
     */
    public PreferenceDao(SharedPreferences pref) {
        this.mPref = pref;
        this.mEdit = null;
    }

    /**
     * 書き込み専用モード
     * 
     * @param edit
     */
    public PreferenceDao(SharedPreferences.Editor edit) {
        this.mPref = null;
        this.mEdit = edit;
    }

    /**
     * 読み込み、書き込み両対応モード
     * 
     * @param pref
     * @param edit
     */
    public PreferenceDao(SharedPreferences pref, SharedPreferences.Editor edit) {
        this.mPref = pref;
        this.mEdit = edit;
    }

    /**
     * カテゴリ別に並び順のインデックスを取得する
     * 
     * @param category
     * @return
     */
    public int getCategoryIndex(int category) {
        return mPref.getInt(PREF_CATEGORY_INDEX + category, 0);
    }

    /**
     * カテゴリ別に並び順のインデックスを保存しておく
     * 
     * @param category
     * @param orderIndex
     */
    public void setCategoryIndex(int category, int orderIndex) {
        mEdit.putInt(PREF_CATEGORY_INDEX + category, orderIndex);
    }

}

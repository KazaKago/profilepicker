package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * パスワードエントリーのDBのデータを保持しておくためのクラス
 * 
 * @author PTAMURA
 * 
 */
public class PasswordEntryEntity implements Serializable {

    private static final long serialVersionUID = -279782517500258979L;

    /**
     * フィールド変数 ID
     */
    private final int id;
    /**
     * フィールド変数 タイトル
     */
    private String title;
    /**
     * フィールド変数 並び順
     */
    public int orderIndex;

    /**
     * コンストラクタ
     */
    public PasswordEntryEntity() {
        this.id = -1;
        this.title = "";
        this.orderIndex = -1;
    }

    /**
     * コンストラクタ
     * 
     * @param title
     * @param orderIndex
     */
    public PasswordEntryEntity(String title, int orderIndex) {
        this.id = -1;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    /**
     * コンストラクタ
     * 
     * @param id
     * @param title
     * @param orderIndex
     */
    public PasswordEntryEntity(int id, String title, int orderIndex) {
        this.id = id;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    /**
     * IDを取得
     * 
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * タイトルを取得
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * タイトルを設定
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 並び順を取得
     * 
     * @return
     */
    public int getOrderIndex() {
        return orderIndex;
    }

    /**
     * 並び順を設定
     * 
     * @param orderIndex
     */
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

}

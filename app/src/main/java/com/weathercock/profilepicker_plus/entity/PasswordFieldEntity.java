package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * パスワードフィールドのDBのデータを保持しておくためのクラス
 * 
 * @author PTAMURA
 * 
 */
public class PasswordFieldEntity implements Serializable {

    private static final long serialVersionUID = 1761533235680371589L;

    /**
     * フィールド変数 ID
     */
    private final int id;
    /**
     * フィールド変数 所属カテゴリID
     */
    private int parentId;
    /**
     * フィールド変数 タグテキスト
     */
    private String contentTag;
    /**
     * フィールド変数 内容テキスト
     */
    private String contentText;
    /**
     * フィールド変数 並び順
     */
    public int orderIndex;

    /**
     * コンストラクタ
     */
    public PasswordFieldEntity() {
        this.id = -1;
        this.parentId = -1;
        this.contentTag = "";
        this.contentText = "";
        this.orderIndex = -1;
    }

    /**
     * コンストラクタ
     * 
     * @param parentId
     * @param contentTag
     * @param contentText
     * @param orderIndex
     */
    public PasswordFieldEntity(int parentId, String contentTag, String contentText, int orderIndex) {
        this.id = -1;
        this.parentId = parentId;
        this.contentTag = contentTag;
        this.contentText = contentText;
        this.orderIndex = orderIndex;
    }

    /**
     * コンストラクタ
     * 
     * @param id
     * @param parentId
     * @param contentTag
     * @param contentText
     * @param orderIndex
     */
    public PasswordFieldEntity(int id, int parentId, String contentTag, String contentText, int orderIndex) {
        this.id = id;
        this.parentId = parentId;
        this.contentTag = contentTag;
        this.contentText = contentText;
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
     * 親のIDを取得
     * 
     * @return
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * 親のIDを設定
     * 
     * @param parentId
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * タグを取得
     * 
     * @return
     */
    public String getContentTag() {
        return contentTag;
    }

    /**
     * タグを設定
     * 
     * @param contentTag
     */
    public void setContentTag(String contentTag) {
        this.contentTag = contentTag;
    }

    /**
     * 内容テキストを取得
     * 
     * @return
     */
    public String getContentText() {
        return contentText;
    }

    /**
     * 内容テキストを設定
     * 
     * @param contentText
     */
    public void setContentText(String contentText) {
        this.contentText = contentText;
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

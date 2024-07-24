package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * ProfileのDBのデータを保持しておくためのクラス
 * 
 * @author PTAMURA
 * 
 */
public class ProfileEntity implements Serializable {

    private static final long serialVersionUID = 5546258575440539093L;

    /**
     * フィールド変数 ID
     */
    private final int id;
    /**
     * フィールド変数 カテゴリー
     */
    private int category;
    /**
     * フィールド変数 タグテキスト
     */
    private String contentTag;
    /**
     * フィールド変数 内容テキスト
     */
    private String contentText;
    /**
     * フィールド変数 共有可否判定
     */
    private Boolean allowShare;
    /**
     * フィールド変数 並び順
     */
    public int orderIndex;

    /**
     * カテゴリID 電話番号
     */
    final public static int CATEGORY_PHONE_NUMBER = 0;
    /**
     * カテゴリID メールアドレス
     */
    final public static int CATEGORY_MAIL = 1;
    /**
     * カテゴリID 住所
     */
    final public static int CATEGORY_ADDRESS = 2;
    /**
     * カテゴリID 所属
     */
    final public static int CATEGORY_MEMBER = 3;
    /**
     * カテゴリID 誕生日
     */
    final public static int CATEGORY_BIRTHDAY = 4;
    /**
     * カテゴリID メモ
     */
    final public static int CATEGORY_MEMO = 5;

    /**
     * コンストラクタ
     */
    public ProfileEntity() {
        this.id = -1;
        this.category = -1;
        this.contentTag = "";
        this.contentText = "";
        this.allowShare = false;
        this.orderIndex = -1;
    }

    /**
     * コンストラクタ
     * 
     * @param category
     * @param contentTag
     * @param contentText
     * @param allowShare
     * @param orderIndex
     */
    public ProfileEntity(int category, String contentTag, String contentText, Boolean allowShare, int orderIndex) {
        this.id = -1;
        this.category = category;
        this.contentTag = contentTag;
        this.contentText = contentText;
        this.allowShare = allowShare;
        this.orderIndex = orderIndex;
    }

    /**
     * コンストラクタ
     * 
     * @param id
     * @param category
     * @param contentTag
     * @param contentText
     * @param allowShare
     * @param orderIndex
     */
    public ProfileEntity(int id, int category, String contentTag, String contentText, Boolean allowShare, int orderIndex) {
        this.id = id;
        this.category = category;
        this.contentTag = contentTag;
        this.contentText = contentText;
        this.allowShare = allowShare;
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
     * カテゴリを取得
     * 
     * @return
     */
    public int getCategory() {
        return category;
    }

    /**
     * カテゴリを設定
     * 
     * @param category
     */
    public void setCategory(int category) {
        this.category = category;
    }

    /**
     * タグテキストを取得
     * 
     * @return
     */
    public String getContentTag() {
        return contentTag;
    }

    /**
     * タグテキストを設定
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
     * この項目の共有可否を取得
     * 
     * @return
     */
    public Boolean getAllowShare() {
        return allowShare;
    }

    /**
     * この項目の共有可否を設定
     * 
     * @param allowShare
     */
    public void setAllowShare(Boolean allowShare) {
        this.allowShare = allowShare;
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

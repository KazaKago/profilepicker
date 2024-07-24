package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * プロフィールのセルデータを保持するクラス
 * 
 * @author Kensuke
 * 
 */
public class ProfileListViewCell implements Serializable {

    private static final long serialVersionUID = -6703761210014534922L;

    /**
     * 行モード一覧
     * 
     * @author Kensuke
     * 
     */
    public enum ProfileListRowMode {
        /**
         * タイトル用
         */
        HEADER,
        /**
         * 本文用
         */
        CONTENT,
        /**
         * 新規追加行用
         */
        NEW_ADDITION,
    }

    /**
     * タイトル行用ヘッダー文字列
     */
    private final String headerText;
    /**
     * 本文行用タイプ文字列
     */
    private final String contentTag;
    /**
     * 本文行用コンテンツ文字列
     */
    private final String contentText;
    /**
     * モード判定ID(タイトル行、本文行、新規作成行)
     */
    private final ProfileListRowMode rowMode;
    /**
     * 保持データ
     */
    private final ProfileEntity profileData;

    /**
     * ヘッダーセルを作る時の作成用コンストラクタ
     * 
     * @param headerText
     */
    public ProfileListViewCell(String headerText) {
        this.headerText = headerText;
        this.contentTag = "";
        this.contentText = "";
        this.rowMode = ProfileListRowMode.HEADER;
        this.profileData = new ProfileEntity();
    }

    /**
     * コンテンツセルを作る時の作成用コンストラクタ
     * 
     * @param profileData
     */
    public ProfileListViewCell(ProfileEntity profileData) {
        this.headerText = "";
        this.contentTag = profileData.getContentTag();
        this.contentText = profileData.getContentText();
        this.rowMode = ProfileListRowMode.CONTENT;
        this.profileData = profileData;
    }

    /**
     * 新規追加セルを作る時の作成用コンストラクタ
     * 
     * @param category
     */
    public ProfileListViewCell(int category) {
        this.headerText = "";
        this.contentTag = "";
        this.contentText = "";
        this.rowMode = ProfileListRowMode.NEW_ADDITION;
        ProfileEntity profileData = new ProfileEntity();
        profileData.setCategory(category);
        this.profileData = profileData;
    }

    /**
     * ヘッダーテキストを取得
     * 
     * @return
     */
    public String getHeaderText() {
        return headerText;
    }

    /**
     * タグテキストを取得
     * 
     * @return
     */
    public String getTagText() {
        return contentTag;
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
     * 行モードを取得
     * 
     * @return
     */
    public ProfileListRowMode getRowMode() {
        return rowMode;
    }

    /**
     * 内包するプロフィール情報を取得
     * 
     * @return
     */
    public ProfileEntity getProfileData() {
        return profileData;
    }
}

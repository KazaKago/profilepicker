package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * メモのセルデータを保持するクラス
 * 
 * @author Kensuke
 * 
 */
public class MemoListViewCell implements Serializable {

    private static final long serialVersionUID = -6703761210014534922L;

    /**
     * タイトル行用ヘッダー文字列
     */
    private final String titleText;
    /**
     * 本文行用コンテンツ文字列
     */
    private final String contentText;
    /**
     * 保持データ
     */
    private final ProfileEntity profileData;

    /**
     * コンストラクタ
     * 
     * @param profileData
     */
    public MemoListViewCell(ProfileEntity profileData) {
        this.titleText = profileData.getContentTag();
        this.contentText = profileData.getContentText();
        this.profileData = profileData;
    }

    /**
     * タイトルを取得
     * 
     * @return
     */
    public String getTitleText() {
        return titleText;
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
     * 内包するProfileを取得
     * 
     * @return profileDB
     */
    public ProfileEntity getProfileData() {
        return profileData;
    }

}

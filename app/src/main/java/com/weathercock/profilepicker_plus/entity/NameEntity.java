package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * 名前データを保持しておくためのクラス
 * 
 * @author PTAMURA
 * 
 */
public class NameEntity implements Serializable {

    private static final long serialVersionUID = 6488564234410269508L;

    /**
     * フィールド変数 ID
     */
    private final int id;
    /**
     * フィールド変数 名前
     */
    private String firstName;
    /**
     * フィールド変数 苗字
     */
    private String lastName;
    /**
     * フィールド変数 名前カナ
     */
    private String readingFirstName;
    /**
     * フィールド変数 苗字カナ
     */
    private String readingLastName;

    /**
     * コンストラクタ
     */
    public NameEntity() {
        this.id = -1;
        this.firstName = "";
        this.lastName = "";
        this.readingFirstName = "";
        this.readingLastName = "";
    }

    /**
     * コンストラクタ
     * 
     * @param firstName
     * @param lastName
     * @param readingFirstName
     * @param readingLastName
     */
    public NameEntity(String firstName, String lastName, String readingFirstName, String readingLastName) {
        this.id = -1;
        this.firstName = firstName;
        this.lastName = lastName;
        this.readingFirstName = readingFirstName;
        this.readingLastName = readingLastName;
    }

    /**
     * コンストラクタ
     * 
     * @param id
     * @param firstName
     * @param lastName
     * @param readingFirstName
     * @param readingLastName
     */
    public NameEntity(int id, String firstName, String lastName, String readingFirstName, String readingLastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.readingFirstName = readingFirstName;
        this.readingLastName = readingLastName;
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
     * 名前を取得
     * 
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * 名前を設定
     * 
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 苗字を取得
     * 
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * 苗字を設定
     * 
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 名前読み仮名を取得
     * 
     * @return
     */
    public String getReadingFirstName() {
        return readingFirstName;
    }

    /**
     * 名前読み仮名を設定
     * 
     * @param readingFirstName
     */
    public void setReadingFirstName(String readingFirstName) {
        this.readingFirstName = readingFirstName;
    }

    /**
     * 苗字読み仮名を取得
     * 
     * @return
     */
    public String getReadingLastName() {
        return readingLastName;
    }

    /**
     * 苗字読み仮名を設定
     * 
     * @param readingLastName
     */
    public void setReadingLastName(String readingLastName) {
        this.readingLastName = readingLastName;
    }
}

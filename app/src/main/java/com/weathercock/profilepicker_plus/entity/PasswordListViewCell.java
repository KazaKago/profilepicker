package com.weathercock.profilepicker_plus.entity;

import java.io.Serializable;

/**
 * パスワードリストのセルクラス
 * 
 * @author Kensuke
 * 
 */
public class PasswordListViewCell implements Serializable {

    private static final long serialVersionUID = 7928109114480639573L;

    /**
     * 行モード一覧
     * 
     * @author Kensuke
     * 
     */
    public enum PasswordListRowMode {
        /**
         * タイトル用
         */
        HEADER,
        /**
         * 本文用
         */
        CONTENT,
        /**
         * フィールドの新規追加行用
         */
        NEW_ADDITION_FIELD,
        /**
         * エントリーの新規追加行用
         */
        NEW_ADDITION_ENTRY,
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
    private final PasswordListRowMode rowMode;
    /**
     * パスワード保持データ
     */
    private final PasswordEntryEntity passwordEntry;
    /**
     * パスワードフィールド保持データ
     */
    private final PasswordFieldEntity passwordField;

    /**
     * パスワードセルを作る時の作成用コンストラクタ
     * 
     * @param passwordEntry
     */
    public PasswordListViewCell(PasswordEntryEntity passwordEntry) {
        this.headerText = passwordEntry.getTitle();
        this.contentTag = "";
        this.contentText = "";
        this.rowMode = PasswordListRowMode.HEADER;
        this.passwordEntry = passwordEntry;
        this.passwordField = new PasswordFieldEntity();
    }

    /**
     * フィールドセルを作る時の作成用コンストラクタ
     * 
     * @param passwordField
     */
    public PasswordListViewCell(PasswordFieldEntity passwordField) {
        this.headerText = "";
        this.contentTag = passwordField.getContentTag();
        this.contentText = passwordField.getContentText();
        this.rowMode = PasswordListRowMode.CONTENT;
        this.passwordEntry = new PasswordEntryEntity();
        this.passwordField = passwordField;
    }

    /**
     * フィールドセルの新規追加セルを作る時の作成用コンストラクタ
     * 
     * @param parentId
     */
    public PasswordListViewCell(int parentId) {
        this.headerText = "";
        this.contentTag = "";
        this.contentText = "";
        this.rowMode = PasswordListRowMode.NEW_ADDITION_FIELD;
        this.passwordEntry = new PasswordEntryEntity();
        PasswordFieldEntity passwordField = new PasswordFieldEntity();
        passwordField.setParentId(parentId);
        this.passwordField = passwordField;
    }

    /**
     * エントリーセルの新規追加セルを作る時の作成用コンストラクタ
     */
    public PasswordListViewCell() {
        this.headerText = "";
        this.contentTag = "";
        this.contentText = "";
        this.rowMode = PasswordListRowMode.NEW_ADDITION_ENTRY;
        this.passwordEntry = new PasswordEntryEntity();
        this.passwordField = new PasswordFieldEntity();
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
    public PasswordListRowMode getRowMode() {
        return rowMode;
    }

    /**
     * 内包するパスワードデータを取得
     * 
     * @return
     */

    public PasswordEntryEntity getPasswordEntry() {
        return passwordEntry;
    }

    /**
     * 内包するパスワードフィールドデータを取得
     * 
     * @return
     */
    public PasswordFieldEntity getPasswordField() {
        return passwordField;
    }
}

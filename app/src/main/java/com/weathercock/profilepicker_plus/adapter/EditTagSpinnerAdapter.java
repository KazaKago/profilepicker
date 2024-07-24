package com.weathercock.profilepicker_plus.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * タグ編集のスピナーのアダプタークラス
 * 
 * @author Kensuke
 * 
 */
public class EditTagSpinnerAdapter extends ArrayAdapter<String> {

    /**
     * スピナーのタイトル欄に表示する文字列
     */
    private String mTitleText;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param textViewResourceId
     * @param list
     */
    public EditTagSpinnerAdapter(Context context, int textViewResourceId, List<String> list) {
        super(context, textViewResourceId, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(this.mTitleText);
        return view;
    }

    /**
     * タイトル欄にセット
     * 
     * @param titleText
     */
    public void setTitleText(String titleText) {
        this.mTitleText = titleText;
    }

    /**
     * タイトル欄の文字を取得
     * 
     * @return
     */
    public String getTitleText() {
        return this.mTitleText;
    }

}

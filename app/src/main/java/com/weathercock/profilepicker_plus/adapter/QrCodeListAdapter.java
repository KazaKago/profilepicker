package com.weathercock.profilepicker_plus.adapter;

import java.util.ArrayList;

import android.content.Context;

import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;

/**
 * タップ挙動を変更したプロフィールリストアダプター継承クラス
 * 
 * @author Kensuke
 * 
 */
public class QrCodeListAdapter extends ProfileListAdapter {

    /**
     * コンストラクタ
     * 
     * @param context
     * @param objects
     */
    public QrCodeListAdapter(Context context, ArrayList<ProfileListViewCell> objects) {
        super(context, objects);
    }

    /**
     * 全行タップ無効
     */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}

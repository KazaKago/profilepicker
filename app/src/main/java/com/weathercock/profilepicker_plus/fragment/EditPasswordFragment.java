package com.weathercock.profilepicker_plus.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.weathercock.profilepicker_plus.R;

/**
 * パスワード編集画面のフラグメントクラス
 * 
 * @author Kensuke
 * 
 */
public class EditPasswordFragment extends Fragment {

    /**
     * コンストラクタ
     */
    public EditPasswordFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //自動でソフトウェアキーボードが出てくるのを防ぐ
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return inflater.inflate(R.layout.fragment_edit_password, container, false);
    }

}

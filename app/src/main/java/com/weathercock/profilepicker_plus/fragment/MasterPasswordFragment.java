package com.weathercock.profilepicker_plus.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;

/**
 * マスターパスワードのフラグメントクラス
 * 
 * @author Kensuke
 * 
 */
public class MasterPasswordFragment extends Fragment {

    /**
     * マスターパスワードのイベントを通知するインターフェースクラス
     * 
     * @author Kensuke
     * 
     */
    public interface MasterPasswordCallbackListener {

        /**
         * パスワードが解除された際のイベントを通知する
         */
        public void onUnlockPass();

        /**
         * パスワードを間違えた際のイベントを通知する
         */
        public void onMistakePass();

    }

    /**
     * コールバックリスナー
     */
    private MasterPasswordCallbackListener mListener;
    /**
     * 解除パスワード
     */
    private String mPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_master_password, container, false);
        view.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //タップイベントを透過しないように指定
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText inputArea = (EditText) getActivity().findViewById(R.id.master_password_input);
        inputArea.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkPassword();
                    return true;
                }
                return false;
            }
        });
        inputArea.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (!hasFocus) hideSoftwareKeyboard();
            }
        });

        Button unlockBtn = (Button) getActivity().findViewById(R.id.unlock_btn);
        unlockBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checkPassword();
            }
        });
    }

    /**
     * ソフトキーボードを非表示にする
     */
    private void hideSoftwareKeyboard() {
        EditText inputArea = (EditText) getActivity().findViewById(R.id.master_password_input);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputArea.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * パスワードの正当性をチェック
     */
    private void checkPassword() {
        EditText inputArea = (EditText) getActivity().findViewById(R.id.master_password_input);
        String inputWord = inputArea.getText().toString();
        if (isMatchPassword(inputWord)) {
            //一致した時
            unlockMasterPassword();
        } else {
            //間違った時
            mistakeMasterPassword();
        }
    }

    /**
     * パスワードが一致しているかチェック
     * 
     * @param inputWord
     * @return
     */
    private boolean isMatchPassword(String inputWord) {
        if (inputWord != null && inputWord.equals(mPassword)) return true;
        else return false;
    }

    /**
     * パスワードロックを行う<br>
     * Fragmentから呼ばれる場合はgetFragmentManagerがnullになるので親側から渡してもらう<br>
     * APILevel 17からはgetChildFragmentManagerが使えるらしい
     * 
     * @param fragmentManager
     * @param parentViewId
     * @param password
     */
    public void lockMasterPassword(FragmentManager fragmentManager, int parentViewId, String password) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(parentViewId, this);
        fragmentTransaction.commit();

        this.mPassword = password;
    }

    /**
     * パスワードロックを解除する
     */
    public void unlockMasterPassword() {
        //フラグメントを除外
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();

        // ソフトキーボードを非表示にする
        hideSoftwareKeyboard();

        if (mListener != null) mListener.onUnlockPass();
    }

    /**
     * パスワードロックを間違えたとき
     */
    private void mistakeMasterPassword() {
        //赤文字で警告
        TextView titleView = (TextView) getActivity().findViewById(R.id.master_password_input_txt);
        titleView.setText(R.string.layer_mistake_password);
        titleView.setTextColor(getResources().getColor(R.color.red));

        //解除失敗アニメーションを実行
        EditText inputArea = (EditText) getActivity().findViewById(R.id.master_password_input);
        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        inputArea.startAnimation(shake);

        if (mListener != null) mListener.onMistakePass();
    }

    /**
     * コールバックリスナーを設定する
     * 
     * @param listener
     */
    public void setCallbackListener(MasterPasswordCallbackListener listener) {
        this.mListener = listener;
    }

    /**
     * コールバックリスナーを解除する
     */
    public void removeCallbackListener() {
        this.mListener = null;
    }
}

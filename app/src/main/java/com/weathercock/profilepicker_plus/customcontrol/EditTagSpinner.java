package com.weathercock.profilepicker_plus.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * 以下の2つの問題を解消したカスタムスピナークラス<br>
 * ・標準のスピナーはOnItemSelectイベントがOnCreateで生成した時に一度走ってしまう。<br>
 * ・同一のアイテムを選択してもOnItemSelectイベントが起こらない。 <br>
 * <br>
 * setOnItemSelectedEvenIfUnchangedListenerをsetOnItemSelectedListenerの代わりとして使用すること
 * 
 * @author Kensuke
 * 
 */
public class EditTagSpinner extends Spinner {

    private OnItemSelectedListener mListener;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param attrs
     */
    public EditTagSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (mListener != null) mListener.onItemSelected(this, this, position, 0);
    }

    /**
     * 同一アイテムをタップしても反応するOnItemSelectedListener
     * 
     * @param listener
     */
    public void setOnItemSelectedEvenIfUnchangedListener(OnItemSelectedListener listener) {
        this.mListener = listener;
    }
}

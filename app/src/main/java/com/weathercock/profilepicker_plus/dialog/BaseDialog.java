package com.weathercock.profilepicker_plus.dialog;

import android.app.DialogFragment;

/**
 * ダイアログの基底クラス
 * 
 * @author Kensuke
 * 
 */
public class BaseDialog extends DialogFragment {

    /**
     * Listenerタイプ一覧
     * 
     * @author PTAMURA
     * 
     */
    protected enum ListenerType {
        /**
         * ActivityからDialogを表示させたとき
         */
        ACTIVITY,
        /**
         * FragmentからDialogを表示させたとき
         */
        FRAGMENT,
    }

    /**
     * Listenerタイプ
     */
    protected final static String ARG_LISTENER_TYPE = "listenerType";

}

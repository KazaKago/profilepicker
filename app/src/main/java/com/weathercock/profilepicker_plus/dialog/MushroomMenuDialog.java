package com.weathercock.profilepicker_plus.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;

import com.weathercock.profilepicker_plus.R;

/**
 * マッシュルームメニューダイアログを制御するクラス
 * 
 * @author Kensuke
 * 
 */
public class MushroomMenuDialog extends BaseDialog {

    /**
     * ダイアログのボタンが押された際のイベントを通知するインターフェースクラス
     * 
     * @author Kensuke
     * 
     */
    public interface MushroomMenuDialogListener {

        /**
         * 文字を挿入が押された際のイベントを通知する
         * 
         * @param mushroomText
         */
        public void onInsertTextClick(String mushroomText);

        /**
         * 文字をコピーが押された際のイベントを通知する
         * 
         * @param mushroomText
         */
        public void onCopyTextClick(String mushroomText);
    }

    private final static String DIALOG_MUSHROOM_TEXT_KEY = "dialog_mushroom_text";

    private MushroomMenuDialogListener mListener;
    private String mMushroomText;

    /**
     * ダイアログのインスタンスを取得する。コンストラクタの引数で値を受け取ってしまうとフラグメント再起動時に落ちてしまうためインスタンス取得関数を使用
     * 
     * @param mushroomText
     * @return
     */
    public static MushroomMenuDialog newInstance(String mushroomText) {
        //インスタンスを生成
        MushroomMenuDialog dialog = new MushroomMenuDialog();

        //画面回転時などフラグメント再起動時に初期化されないようにBundleに持たせておく
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_MUSHROOM_TEXT_KEY, mushroomText);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Bundleからリスナーのタイプを取り出す
        Bundle bundle = getArguments();
        ListenerType listenerType = (ListenerType) bundle.getSerializable(ARG_LISTENER_TYPE);

        //リスナーをセットしなおす
        if (listenerType == ListenerType.ACTIVITY) mListener = (MushroomMenuDialogListener) activity;
        else if (listenerType == ListenerType.FRAGMENT) mListener = (MushroomMenuDialogListener) getTargetFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(DIALOG_MUSHROOM_TEXT_KEY, mMushroomText);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Bundleから復元
        mMushroomText = getArguments().getString(DIALOG_MUSHROOM_TEXT_KEY);

        //ダイアログの生成
        final CharSequence[] menuItems = getResources().getStringArray(R.array.menu_mushroom);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.menu_mushroom_title));
        builder.setItems(menuItems, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //文字列を挿入
                        mListener.onInsertTextClick(mMushroomText);
                        break;
                    case 1:
                        //文字列をコピー
                        mListener.onCopyTextClick(mMushroomText);
                        break;
                    default:
                        break;
                }
            }
        });

        return builder.create();
    }

    /**
     * ダイアログのイベントリスナーを登録する
     * 
     * @param listener
     */
    public void setDialogListener(MushroomMenuDialogListener listener) {
        //Activityからダイアログが呼び出された時とFragmentからダイアログが呼び出されたときはリスナーの保持の仕方を変える。
        //画面回転時などの再起動時に破棄されないようにする。
        //・FragmentのときはsetTargetFragmentに持たせておくことでgetTargetFragmentから適切なインスタンスを取り出すことができる。
        //・ActivityのときはonAttachでActivityと接続したときにlistenerを更新することが必要。

        //リスナーのタイプをチェック
        ListenerType listenerType;
        if (listener instanceof Activity) {
            listenerType = ListenerType.ACTIVITY;
        } else if (listener instanceof Fragment) {
            listenerType = ListenerType.FRAGMENT;
            setTargetFragment((Fragment) listener, 0);
        } else {
            throw new IllegalArgumentException(listener.getClass() + " must be either an Activity or a Fragment");
        }

        //取得したリスナーのタイプをBundleに持たせておく
        Bundle bundle = getArguments();
        bundle.putSerializable(ARG_LISTENER_TYPE, listenerType);
        setArguments(bundle);
    }

    /**
     * ダイアログのイベントリスナーを解除する
     */
    public void removeDialogListener() {
        mListener = null;
        setTargetFragment(null, 0);

        Bundle bundle = getArguments();
        bundle.putSerializable(ARG_LISTENER_TYPE, null);
        setArguments(bundle);
    }

}

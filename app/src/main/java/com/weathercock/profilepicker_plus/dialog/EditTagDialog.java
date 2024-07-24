package com.weathercock.profilepicker_plus.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.weathercock.profilepicker_plus.R;

/**
 * 編集ダイアログを制御するクラス
 * 
 * @author Kensuke
 * 
 */
public class EditTagDialog extends BaseDialog {

    /**
     * ダイアログのボタンが押された際のイベントを通知するインターフェースクラス
     * 
     * @author Kensuke
     * 
     */
    public interface EditTagDialogListener {

        /**
         * OKボタンが押された際のイベントを通知する
         * 
         * @param tag
         *            編集済みのタグ
         */
        public void onPositiveClick(String tag);

        /**
         * キャンセルボタンが押された際のイベントを通知する
         */
        public void onNeutralClick();

    }

    private EditTagDialogListener mListener;
    private EditText mEditTag;

    /**
     * ダイアログのインスタンスを取得する。コンストラクタの引数で値を受け取ってしまうとフラグメント再起動時に落ちてしまうためインスタンス取得関数を使用
     * 
     * @return
     */
    public static EditTagDialog newInstance() {
        //インスタンスを生成
        EditTagDialog dialog = new EditTagDialog();

        //画面回転時などフラグメント再起動時に初期化されないようにBundleに持たせておく
        Bundle bundle = new Bundle();
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
        if (listenerType == ListenerType.ACTIVITY) mListener = (EditTagDialogListener) activity;
        else if (listenerType == ListenerType.FRAGMENT) mListener = (EditTagDialogListener) getTargetFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Viewの読み込みと現在の設定の復元
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editDialogLayout = inflater.inflate(R.layout.dialog_edit_tag, null, false);
        mEditTag = (EditText) editDialogLayout.findViewById(R.id.field_edit_tag);

        //ダイアログの生成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.edit_tag));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //文字を取得する
                String newTag = mEditTag.getText().toString();

                //結果を返すフラグメントを取得する
                if (mListener != null) {
                    //イベントを通知する
                    mListener.onPositiveClick(newTag);
                }
            }

        });
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //結果を返すフラグメントを取得する
                if (mListener != null) {
                    //イベントを通知する
                    mListener.onNeutralClick();
                }
            }
        });
        builder.setView(editDialogLayout);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    /**
     * ダイアログのイベントリスナーを登録する
     * 
     * @param listener
     */
    public void setDialogListener(EditTagDialogListener listener) {
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

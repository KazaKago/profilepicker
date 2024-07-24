package com.weathercock.profilepicker_plus.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Switch;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;

/**
 * 誕生日編集ダイアログを制御するクラス
 * 
 * @author Kensuke
 * 
 */
public class EditBirthdayDialog extends BaseDialog {

    /**
     * ダイアログのボタンが押された際のイベントを通知するインターフェースクラス
     * 
     * @author Kensuke
     * 
     */
    public interface EditBirthdayDialogListener {

        /**
         * OKボタンが押された際のイベントを通知する
         * 
         * @param newCell
         *            編集済みのセル
         * @param position
         *            編集中のセル座標
         * @param isNew
         *            編集中の項目が既存のものか新しく生成されるものか
         */
        public void onPositiveClick(ProfileListViewCell newCell, int position, boolean isNew);

        /**
         * キャンセルボタンが押された際のイベントを通知する
         * 
         * @param newCell
         * @param position
         * @param isNew
         */
        public void onNeutralClick(ProfileListViewCell newCell, int position, boolean isNew);

        /**
         * 削除ボタンが押された際のイベントを通知する
         * 
         * @param newCell
         * @param position
         * @param isNew
         */
        public void onNegativeClick(ProfileListViewCell newCell, int position, boolean isNew);

    }

    private final static String DIALOG_EDIT_CELL_KEY = "dialog_edit_cell";
    private final static String DIALOG_CELL_POSITION_KEY = "dialog_cell_position";
    private final static String DIALOG_IS_NEW_KEY = "dialog_is_new";
    private final static String DIALOG_IS_RETRY_KEY = "dialog_is_retry";

    private EditBirthdayDialogListener mListener;
    private ProfileListViewCell mCell;
    private int mPosition;
    private boolean mIsNew;
    private boolean mIsRetry;
    private DatePicker mBirthdayPicker;
    private Switch mShareSwitch;

    /**
     * ダイアログのインスタンスを取得する。コンストラクタの引数で値を受け取ってしまうとフラグメント再起動時に落ちてしまうためインスタンス取得関数を使用
     * 
     * @param cell
     * @param position
     * @param isNew
     * @param isRetry
     * @return
     */
    public static EditBirthdayDialog newInstance(ProfileListViewCell cell, int position, boolean isNew, boolean isRetry) {
        //インスタンスを生成
        EditBirthdayDialog dialog = new EditBirthdayDialog();

        //画面回転時などフラグメント再起動時に初期化されないようにBundleに持たせておく
        Bundle bundle = new Bundle();
        bundle.putSerializable(DIALOG_EDIT_CELL_KEY, cell);
        bundle.putInt(DIALOG_CELL_POSITION_KEY, position);
        bundle.putBoolean(DIALOG_IS_NEW_KEY, isNew);
        bundle.putBoolean(DIALOG_IS_RETRY_KEY, isRetry);
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
        if (listenerType == ListenerType.ACTIVITY) mListener = (EditBirthdayDialogListener) activity;
        else if (listenerType == ListenerType.FRAGMENT) mListener = (EditBirthdayDialogListener) getTargetFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //引数で取得した値を復元
        mCell = (ProfileListViewCell) getArguments().getSerializable(DIALOG_EDIT_CELL_KEY);
        mPosition = getArguments().getInt(DIALOG_CELL_POSITION_KEY);
        mIsNew = getArguments().getBoolean(DIALOG_IS_NEW_KEY);
        mIsRetry = getArguments().getBoolean(DIALOG_IS_RETRY_KEY);

        //Viewの読み込みと現在の設定の復元
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editDialogLayout = inflater.inflate(R.layout.dialog_edit_birthday, null, false);
        mBirthdayPicker = (DatePicker) editDialogLayout.findViewById(R.id.piker_birthday);
        mShareSwitch = (Switch) editDialogLayout.findViewById(R.id.switch_share_birthday);
        mShareSwitch.setChecked(mCell.getProfileData().getAllowShare());
        if (!(mIsNew && !mIsRetry)) {
            //新規作成以外はcellの持っている誕生日を復元
            try {
                String[] dateStrList = mCell.getProfileData().getContentText().split(getString(R.string.date_splitter));
                int year = Integer.valueOf(dateStrList[0]);
                int month = Integer.valueOf(dateStrList[1]) - 1;
                int day = Integer.valueOf(dateStrList[2]);
                mBirthdayPicker.updateDate(year, month, day);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //ダイアログの生成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.section_birthday));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //新しいDB項目の作成
                String newTag = "";
                String newContent = getString(R.string.date_format, mBirthdayPicker.getYear(), mBirthdayPicker.getMonth() + 1, mBirthdayPicker.getDayOfMonth());
                boolean newSharePref = mShareSwitch.isChecked();
                ProfileEntity newDB = new ProfileEntity(mCell.getProfileData().getId(), mCell.getProfileData().getCategory(), newTag, newContent, newSharePref, mCell.getProfileData().getOrderIndex());
                //新しいDBを内包したセルの作成
                final ProfileListViewCell newCell = new ProfileListViewCell(newDB);

                //結果を返すフラグメントを取得する
                if (mListener != null) {
                    //イベントを通知する
                    mListener.onPositiveClick(newCell, mPosition, mIsNew);
                }
            }

        });
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //新しいDB項目の作成
                String newTag = "";
                String newContent = getString(R.string.date_format, mBirthdayPicker.getYear(), mBirthdayPicker.getMonth() + 1, mBirthdayPicker.getDayOfMonth());
                boolean newSharePref = mShareSwitch.isChecked();
                ProfileEntity newDB = new ProfileEntity(mCell.getProfileData().getId(), mCell.getProfileData().getCategory(), newTag, newContent, newSharePref, mCell.getProfileData().getOrderIndex());
                //新しいDBを内包したセルの作成
                final ProfileListViewCell newCell = new ProfileListViewCell(newDB);

                //結果を返すフラグメントを取得する
                if (mListener != null) {
                    //イベントを通知する
                    mListener.onNeutralClick(newCell, mPosition, mIsNew);
                }
            }
        });
        if (!mIsNew) {
            //新規作成ではないときは削除ボタンをつける
            builder.setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //新しいDB項目の作成
                    String newTag = "";
                    String newContent = getString(R.string.date_format, mBirthdayPicker.getYear(), mBirthdayPicker.getMonth() + 1, mBirthdayPicker.getDayOfMonth());
                    boolean newSharePref = mShareSwitch.isChecked();
                    ProfileEntity newDB = new ProfileEntity(mCell.getProfileData().getId(), mCell.getProfileData().getCategory(), newTag, newContent, newSharePref, mCell.getProfileData().getOrderIndex());
                    //新しいDBを内包したセルの作成
                    final ProfileListViewCell newCell = new ProfileListViewCell(newDB);

                    //結果を返すフラグメントを取得する
                    if (mListener != null) {
                        //イベントを通知する
                        mListener.onNegativeClick(newCell, mPosition, mIsNew);
                    }
                }

            });
        }
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
    public void setDialogListener(EditBirthdayDialogListener listener) {
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

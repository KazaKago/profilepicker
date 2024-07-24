package com.weathercock.profilepicker_plus.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Switch;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.adapter.EditTagSpinnerAdapter;
import com.weathercock.profilepicker_plus.customcontrol.EditTagSpinner;
import com.weathercock.profilepicker_plus.dialog.EditTagDialog.EditTagDialogListener;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;

/**
 * 編集ダイアログを制御するクラス
 * 
 * @author Kensuke
 * 
 */
public class EditProfileDialog extends BaseDialog implements EditTagDialogListener {

    /**
     * ダイアログのボタンが押された際のイベントを通知するインターフェースクラス
     * 
     * @author Kensuke
     * 
     */
    public interface EditProfileDialogListener {

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
    private final static String DIALOG_SPINNER_TITLE_KEY = "dialog_spinner_adapter_key";

    private EditProfileDialogListener mListener;
    private ProfileListViewCell mCell;
    private int mPosition;
    private boolean mIsNew;
    private boolean mIsRetry;
    private EditText mEditContent;
    private EditTagSpinner mEditSpinner;
    private Switch mShareSwitch;
    private EditTagSpinnerAdapter mSpinnerAdapter;

    /**
     * ダイアログのインスタンスを取得する。コンストラクタの引数で値を受け取ってしまうとフラグメント再起動時に落ちてしまうためインスタンス取得関数を使用
     * 
     * @param cell
     * @param position
     * @param isNew
     * @param isRetry
     * @return
     */
    public static EditProfileDialog newInstance(ProfileListViewCell cell, int position, boolean isNew, boolean isRetry) {
        //インスタンスを生成
        EditProfileDialog dialog = new EditProfileDialog();

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
        if (listenerType == ListenerType.ACTIVITY) mListener = (EditProfileDialogListener) activity;
        else if (listenerType == ListenerType.FRAGMENT) mListener = (EditProfileDialogListener) getTargetFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(DIALOG_SPINNER_TITLE_KEY, mSpinnerAdapter.getTitleText());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //引数で取得した値を復元
        mCell = (ProfileListViewCell) getArguments().getSerializable(DIALOG_EDIT_CELL_KEY);
        mPosition = getArguments().getInt(DIALOG_CELL_POSITION_KEY);
        mIsNew = getArguments().getBoolean(DIALOG_IS_NEW_KEY);
        mIsRetry = getArguments().getBoolean(DIALOG_IS_RETRY_KEY);

        //セル内のデータのカテゴリからタイトルとInputTypeを取得
        String title = "";
        int inputType = 0;
        String[] spinnerStrList = null;
        switch (mCell.getProfileData().getCategory()) {
            case ProfileEntity.CATEGORY_PHONE_NUMBER:
                title = getString(R.string.section_phone_number);
                inputType = InputType.TYPE_CLASS_PHONE;
                spinnerStrList = getResources().getStringArray(R.array.spinner_phone_number);
                break;
            case ProfileEntity.CATEGORY_MAIL:
                title = getString(R.string.section_mail);
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                spinnerStrList = getResources().getStringArray(R.array.spinner_mail);
                break;
            case ProfileEntity.CATEGORY_ADDRESS:
                title = getString(R.string.section_address);
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS;
                spinnerStrList = getResources().getStringArray(R.array.spinner_address);
                break;
            case ProfileEntity.CATEGORY_MEMBER:
                title = getString(R.string.section_member);
                inputType = InputType.TYPE_CLASS_TEXT;
                spinnerStrList = getResources().getStringArray(R.array.spinner_member);
                break;
            case ProfileEntity.CATEGORY_MEMO:
                title = getString(R.string.section_memo);
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
                spinnerStrList = getResources().getStringArray(R.array.spinner_memo);
                break;
            default:
                break;
        }

        //スピナーのアダプターに文字列を追加(まとめて追加すると要素の追加や削除ができないためひとつずつ追加している)
        ArrayList<String> spinnerItemList = new ArrayList<String>();
        for (int i = 0; i < spinnerStrList.length; i++) {
            String spinnerStr = spinnerStrList[i];
            spinnerItemList.add(spinnerStr);
        }
        //最後にカスタムを追加
        spinnerItemList.add(getString(R.string.custom));
        mSpinnerAdapter = new EditTagSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerItemList);
        //spinnerのタイトルを指定
        if (mIsNew && !mIsRetry) mSpinnerAdapter.setTitleText(mSpinnerAdapter.getItem(0));
        else mSpinnerAdapter.setTitleText(mCell.getProfileData().getContentTag());

        //Viewの読み込みと現在の設定の復元
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editDialogLayout = inflater.inflate(R.layout.dialog_edit_profile, null, false);
        mEditContent = (EditText) editDialogLayout.findViewById(R.id.field_edit_content);
        mEditContent.setInputType(inputType);
        mEditContent.setText(mCell.getProfileData().getContentText());
        mShareSwitch = (Switch) editDialogLayout.findViewById(R.id.switch_share);
        mShareSwitch.setChecked(mCell.getProfileData().getAllowShare());
        mEditSpinner = (EditTagSpinner) editDialogLayout.findViewById(R.id.spinner_edit_tag);
        mEditSpinner.setAdapter(mSpinnerAdapter);
        mEditSpinner.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EditTagSpinner spinner = (EditTagSpinner) parent;
                if (spinner.getSelectedItemPosition() == mSpinnerAdapter.getCount() - 1) {
                    //スピナーの最後の要素が選択されたとき
                    createTagDialog();
                } else {
                    //それ以外の時
                    mSpinnerAdapter.setTitleText(spinner.getSelectedItem().toString());
                    mSpinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //ダイアログの生成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //新しいDB項目の作成
                String newTag = mSpinnerAdapter.getTitleText();
                String newContent = mEditContent.getText().toString();
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
                String newType = mEditSpinner.getSelectedItem().toString();
                String newContent = mEditContent.getText().toString();
                boolean newSharePref = mShareSwitch.isChecked();
                ProfileEntity newDB = new ProfileEntity(mCell.getProfileData().getId(), mCell.getProfileData().getCategory(), newType, newContent, newSharePref, mCell.getProfileData().getOrderIndex());
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
                    String newType = mEditSpinner.getSelectedItem().toString();
                    String newContent = mEditContent.getText().toString();
                    boolean newSharePref = mShareSwitch.isChecked();
                    ProfileEntity newDB = new ProfileEntity(mCell.getProfileData().getId(), mCell.getProfileData().getCategory(), newType, newContent, newSharePref, mCell.getProfileData().getOrderIndex());
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

        //画面回転時など一時的に保存された項目があれば上書く
        if (savedInstanceState != null) {
            mSpinnerAdapter.setTitleText(savedInstanceState.getString(DIALOG_SPINNER_TITLE_KEY));
            mSpinnerAdapter.notifyDataSetChanged();
        }

        return dialog;
    }

    /**
     * タグの編集ダイアログ作成メソッド
     * 
     */
    private void createTagDialog() {
        //ダイアログを作成
        FragmentManager manager = getActivity().getFragmentManager();
        EditTagDialog editTagDialog = EditTagDialog.newInstance();
        editTagDialog.setDialogListener(this);
        editTagDialog.show(manager, "");
    }

    /**
     * ダイアログのイベントリスナーを登録する
     * 
     * @param listener
     */
    public void setDialogListener(EditProfileDialogListener listener) {
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

    /* EditTagDialogListener */

    @Override
    public void onPositiveClick(String tag) {
        mSpinnerAdapter.setTitleText(tag);
        mSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNeutralClick() {

    }

}

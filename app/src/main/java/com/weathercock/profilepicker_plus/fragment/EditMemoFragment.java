package com.weathercock.profilepicker_plus.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.adapter.EditMemoListAdapter;
import com.weathercock.profilepicker_plus.dao.PreferenceDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.dialog.EditProfileDialog;
import com.weathercock.profilepicker_plus.dialog.EditProfileDialog.EditProfileDialogListener;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell.ProfileListRowMode;
import com.weathercock.profilepicker_plus.listener.EditFlagListener;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * メモ編集画面のフラグメントクラス
 * 
 * @author Kensuke
 * 
 */
public class EditMemoFragment extends Fragment implements EditProfileDialogListener {

    //画面回転時などActivity再起動時の一時保存用キー
    final private static String TEMPORARY_SAVE_CELL_LIST_KEY = "temp_save_cell_list";
    final private static String TEMPORARY_SAVE_DELETE_LIST_KEY = "temp_save_delete_list";

    /**
     * カテゴリ別のインデックスを一時的に保存しておくためのキー
     */
    final private static String CATEGORY_INDEX = "category_index_";

    /**
     * セルの内容を保持するリスト
     */
    private ArrayList<ProfileListViewCell> mCellList;
    /**
     * セルの削除リスト(内部DBのIDを保持)
     */
    private ArrayList<ProfileListViewCell> mDeleteCellList;
    /**
     * 各カテゴリ別のインデックス格納用変数
     */
    private HashMap<String, Integer> mCategoryIndex;

    /**
     * アクティビティへ編集済みフラグを通知するためのリスナー
     */
    private EditFlagListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_memo, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //画面回転時などActivityが再起動する際に一時的に値を退避させておく
        outState.putSerializable(TEMPORARY_SAVE_CELL_LIST_KEY, mCellList);
        outState.putSerializable(TEMPORARY_SAVE_DELETE_LIST_KEY, mDeleteCellList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //自動でソフトウェアキーボードが出てくるのを防ぐ
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //すべての項目を読み込む
        refresh(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof EditFlagListener == true) {
            //リスナーをセット
            mListener = ((EditFlagListener) activity);
        }
    }

    /**
     * すべてのフィールドをリフレッシュ
     * 
     * @param savedInstanceState
     */
    @SuppressWarnings("unchecked")
    private void refresh(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceDao.getSharedPreferences(getActivity());
        PreferenceDao preferenceDao = new PreferenceDao(pref);

        //タップリスナーをセット
        RelativeLayout memoArea = (RelativeLayout) getActivity().findViewById(R.id.layout_root_fragment_edit_memo);
        memoArea.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ListView memoList = (ListView) getActivity().findViewById(R.id.list_edit_memo);

                //リストが新規ボタンのみの時はタップで新規追加アクションを行う
                if (memoList.getAdapter().getCount() < 2) {
                    ProfileListViewCell cell = (ProfileListViewCell) memoList.getItemAtPosition(0);
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_UP:
                            createDialog(cell, 0, true, false);
                            break;
                        default:
                            break;
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        //各カテゴリの新規インデックスを取得
        mCategoryIndex = new HashMap<String, Integer>();
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMO, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MEMO));

        //リスト内の項目を取得
        mCellList = new ArrayList<ProfileListViewCell>();
        mCellList.addAll(getMemoCellList(ProfileEntity.CATEGORY_MEMO)); //メモの内容を取得
        mCellList.add(getNewAdditionCell(ProfileEntity.CATEGORY_MEMO)); //新規作成セルを取得

        //削除リストを初期化
        mDeleteCellList = new ArrayList<ProfileListViewCell>();

        //リストを読み込み
        EditMemoListAdapter adapter = new EditMemoListAdapter(getActivity(), mCellList);
        ListView memoList = (ListView) getActivity().findViewById(R.id.list_edit_memo);
        memoList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //セルを取得
                ListView listView = (ListView) parent;
                ProfileListViewCell cell = (ProfileListViewCell) listView.getItemAtPosition(position);
                //新規作成か判定
                boolean isNewCell = (cell.getRowMode() == ProfileListRowMode.NEW_ADDITION);

                //ダイアログ作成メソッドへ
                createDialog(cell, position, isNewCell, false);
            }
        });
        memoList.setAdapter(adapter);

        //リストなしメッセージを更新
        setNoItemMessage();

        //画面回転時など一時的に保存された項目があれば上書く
        if (savedInstanceState != null) {
            mCellList = (ArrayList<ProfileListViewCell>) savedInstanceState.getSerializable(TEMPORARY_SAVE_CELL_LIST_KEY);
            mDeleteCellList = (ArrayList<ProfileListViewCell>) savedInstanceState.getSerializable(TEMPORARY_SAVE_DELETE_LIST_KEY);
            adapter = new EditMemoListAdapter(getActivity(), mCellList);
            memoList.setAdapter(adapter);
            setNoItemMessage();
        }
    }

    /**
     * リストなしメッセージを切り替える
     */
    private void setNoItemMessage() {
        TextView noMessage = (TextView) getActivity().findViewById(R.id.edit_no_memo);
        ListView memoList = (ListView) getActivity().findViewById(R.id.list_edit_memo);
        if (mCellList.size() < 2) {
            noMessage.setVisibility(View.VISIBLE);
            memoList.setVisibility(View.INVISIBLE);
        } else {
            noMessage.setVisibility(View.INVISIBLE);
            memoList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 編集ダイアログ作成メソッド
     * 
     * @param position
     *            タップしたリストの位置
     * @param cell
     *            タップしたセル
     * @param isNew
     * @param isRetry
     */
    private void createDialog(ProfileListViewCell cell, int position, boolean isNew, boolean isRetry) {

        //ダイアログを作成
        FragmentManager manager = getActivity().getFragmentManager();
        EditProfileDialog editDialog = EditProfileDialog.newInstance(cell, position, isNew, isRetry);
        editDialog.setDialogListener(this);
        editDialog.show(manager, "");
    }

    /**
     * 指定されたカテゴリーのメモセルのリストを返す
     * 
     * @param category
     * @return
     */
    private ArrayList<ProfileListViewCell> getMemoCellList(int category) {
        ArrayList<ProfileListViewCell> items = new ArrayList<ProfileListViewCell>();

        // メモの読み込み
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            ArrayList<ProfileEntity> profileList = profileDao.selectByCategory(category);
            for (ProfileEntity profile : profileList) {
                ProfileListViewCell content = new ProfileListViewCell(profile);
                items.add(content);
            }
        } finally {
            if (db != null) db.close();
        }

        return items;
    }

    /**
     * 指定されたカテゴリーの新規追加セルを返す
     * 
     * @param category
     * @return
     */
    private ProfileListViewCell getNewAdditionCell(int category) {
        return (new ProfileListViewCell(category));
    }

    /**
     * 項目の保存を行う。保存ボタンをタップされたときにActivityから通知される
     * 
     * @param edit
     * @param db
     * @throws SQLException
     */
    public void onSave(SharedPreferences.Editor edit, SQLiteDatabase db) throws SQLException {
        ProfileDao profileDao = new ProfileDao(db);
        PreferenceDao preferenceDao = new PreferenceDao(edit);

        //カテゴリ別のインデックスを保存する
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MEMO, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMO));

        //セルの要素をデータベースへすべて保存する
        for (ProfileListViewCell cell : mCellList) {
            //コンテンツ行のみ抽出
            if (cell.getRowMode() == ProfileListRowMode.CONTENT) {
                ProfileEntity profile = cell.getProfileData();
                if (profile.getId() == -1) {
                    //新規作成の時
                    profileDao.insert(profile);
                } else {
                    //更新の時
                    profileDao.update(profile);
                }
            }
        }

        //削除リストの項目を削除
        for (ProfileListViewCell delCell : mDeleteCellList) {
            ProfileEntity profile = delCell.getProfileData();
            profileDao.deleteById(profile.getId());
        }

    }

    /**
     * 編集済みを通知するイベントリスナーを登録する
     * 
     * @param listener
     */
    public void setEditFlagListener(EditFlagListener listener) {
        this.mListener = listener;
    }

    /**
     * 編集済みを通知するイベントリスナーを解除する
     */
    public void removeEditFlagListener() {
        mListener = null;
    }

    /* EditDialogListener */

    @Override
    public void onPositiveClick(final ProfileListViewCell newCell, final int position, final boolean isNew) {

        //内容が埋められているかチェック
        if (newCell.getContentText().length() > 0) {
            //内容があるとき→リストへ反映
            if (isNew) {
                //新規作成の時→カテゴリ別のインデックスを追加して行を追加
                int index = mCategoryIndex.get(CATEGORY_INDEX + newCell.getProfileData().getCategory());
                newCell.getProfileData().setOrderIndex(index);

                mCellList.add(position, newCell);

                //インデックスをインクリメントして一時保存
                mCategoryIndex.put(CATEGORY_INDEX + newCell.getProfileData().getCategory(), index + 1);
            } else {
                //既存のセルの時→行を置き換え
                mCellList.set(position, newCell);
            }

            //リストの要素を読み込み直し
            ListView memoList = (ListView) getActivity().findViewById(R.id.list_edit_memo);
            EditMemoListAdapter adapter = (EditMemoListAdapter) memoList.getAdapter();
            adapter.notifyDataSetChanged();

            //リストなしメッセージを更新
            setNoItemMessage();

            //編集済みフラグを立てる。
            if (mListener != null) mListener.setEditFlag();
        } else {
            //内容が空のとき→エラーダイアログ表示
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.empty_message));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //再度編集ダイアログを表示する
                    createDialog(newCell, position, isNew, true);
                }

            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    //再度ダイアログを表示する
                    createDialog(newCell, position, isNew, true);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onNeutralClick(ProfileListViewCell newCell, int position, boolean isNew) {

    }

    @Override
    public void onNegativeClick(final ProfileListViewCell newCell, final int position, final boolean isNew) {

        //削除してもよいか確認
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.comfirmation));
        builder.setMessage(getString(R.string.comfirm_delete));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //セルリストから削除し、新規作成セルでなければ削除リストに追加
                mCellList.remove(position);
                if (newCell.getProfileData().getId() != -1) mDeleteCellList.add(newCell);

                //リストの要素を読み込み直し
                ListView memoList = (ListView) getActivity().findViewById(R.id.list_edit_memo);
                EditMemoListAdapter adapter = (EditMemoListAdapter) memoList.getAdapter();
                adapter.notifyDataSetChanged();

                //リストなしメッセージを更新
                setNoItemMessage();

                //編集済みフラグを立てる。
                if (mListener != null) mListener.setEditFlag();
            }

        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //再度ダイアログを表示する
                createDialog(newCell, position, isNew, true);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //再度ダイアログを表示する
                createDialog(newCell, position, isNew, true);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}

package com.weathercock.profilepicker_plus.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.adapter.PasswordListAdapter;
import com.weathercock.profilepicker_plus.dao.PasswordEntryDao;
import com.weathercock.profilepicker_plus.dao.PasswordFieldDao;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog.MushroomMenuDialogListener;
import com.weathercock.profilepicker_plus.entity.MemoListViewCell;
import com.weathercock.profilepicker_plus.entity.PasswordEntryEntity;
import com.weathercock.profilepicker_plus.entity.PasswordFieldEntity;
import com.weathercock.profilepicker_plus.entity.PasswordListViewCell;
import com.weathercock.profilepicker_plus.fragment.MasterPasswordFragment.MasterPasswordCallbackListener;
import com.weathercock.profilepicker_plus.util.CommonUtil;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * パスワードページのフラグメントクラス
 * 
 * @author Kensuke
 * 
 */
public class PasswordFragment extends Fragment implements MasterPasswordCallbackListener, MushroomMenuDialogListener {

    /**
     * マスターパスワード解除フラグ
     */
    private boolean mIsUnlockMasterPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //マスターパスワード入力のフラグメントを被せる
        if (!mIsUnlockMasterPassword) {
            MasterPasswordFragment masterPasswordFragment = new MasterPasswordFragment();
            masterPasswordFragment.lockMasterPassword(getFragmentManager(), R.id.layout_root_fragment_password, "1234");
            masterPasswordFragment.setCallbackListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    /**
     * すべてのフィールドをリフレッシュ
     */
    public void refresh() {
        ArrayList<PasswordListViewCell> items = new ArrayList<PasswordListViewCell>();

        //パスワードの読み込み
        for (PasswordListViewCell tempListViewCell : getPasswordEntryCellList()) {
            items.add(tempListViewCell);
            items.addAll(getPasswordFieldCellList(tempListViewCell.getPasswordEntry().getId()));
        }

        //リストを読み込み
        PasswordListAdapter adapter = new PasswordListAdapter(getActivity(), items);
        ListView passwordListView = (ListView) getActivity().findViewById(R.id.list_password);
        passwordListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                PasswordListViewCell item = (PasswordListViewCell) listView.getItemAtPosition(position);
                if (CommonUtil.isMushroom(getActivity())) {
                    //マッシュルームから呼び出された→文字をマッシュルームへ返す
                    CommonUtil.copyToMushroom(getActivity(), item.getContentText());
                } else {
                    //通常起動された→文字をコピーする
                    CommonUtil.copyToClipboard(getActivity(), item.getContentText());
                    Toast.makeText(getActivity(), getString(R.string.copy_toast_text, item.getContentText()), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (CommonUtil.isMushroom(getActivity())) {
            //マッシュルーム経由のときのみ長押しメニューを実装
            passwordListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    MemoListViewCell item = (MemoListViewCell) listView.getItemAtPosition(position);
                    createMushroomMenuDialog(item.getContentText());
                    return false;
                }

            });
        }
        passwordListView.setAdapter(adapter);

        //リストなしメッセージを切り替える
        TextView noMessage = (TextView) getActivity().findViewById(R.id.no_password);
        if (items.size() < 1) noMessage.setVisibility(View.VISIBLE);
        else noMessage.setVisibility(View.INVISIBLE);
    }

    /**
     * パスワードエントリーセルのリストを返す
     * 
     * @return
     */
    private ArrayList<PasswordListViewCell> getPasswordEntryCellList() {
        ArrayList<PasswordListViewCell> items = new ArrayList<PasswordListViewCell>();
        PasswordListViewCell item;

        // パスワードの読み込み
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            PasswordEntryDao passwordEntryDao = new PasswordEntryDao(db);
            ArrayList<PasswordEntryEntity> passwordEntryList = passwordEntryDao.selectAll();
            for (PasswordEntryEntity passwordEntry : passwordEntryList) {
                item = new PasswordListViewCell(passwordEntry);
                items.add(item);
            }
        } finally {
            if (db != null) db.close();
        }

        return items;
    }

    /**
     * 指定されたパスワードフィールドセルのリストを返す
     * 
     * @param parentId
     * @return
     */
    private ArrayList<PasswordListViewCell> getPasswordFieldCellList(int parentId) {
        ArrayList<PasswordListViewCell> items = new ArrayList<PasswordListViewCell>();
        PasswordListViewCell item;

        // パスワードの読み込み
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            PasswordFieldDao passwordFieldDao = new PasswordFieldDao(db);
            ArrayList<PasswordFieldEntity> passwordFieldList = passwordFieldDao.selectByParentId(parentId);
            for (PasswordFieldEntity passwordField : passwordFieldList) {
                item = new PasswordListViewCell(passwordField);
                items.add(item);
            }
        } finally {
            if (db != null) db.close();
        }

        return items;
    }

    /**
     * マッシュルーム向けの長押し用メニューを表示する
     * 
     * @param mushroomText
     */
    private void createMushroomMenuDialog(String mushroomText) {
        FragmentManager manager = getActivity().getFragmentManager();
        MushroomMenuDialog mushroomMenuDialog = MushroomMenuDialog.newInstance(mushroomText);
        mushroomMenuDialog.setDialogListener(this);
        mushroomMenuDialog.show(manager, "");
    }

    /* MasterPasswordCallbackListener */

    @Override
    public void onUnlockPass() {
        mIsUnlockMasterPassword = true;
    }

    @Override
    public void onMistakePass() {

    }

    /* MushroomMenuDialogListener */

    @Override
    public void onInsertTextClick(String mushroomText) {
        CommonUtil.copyToMushroom(getActivity(), mushroomText);
    }

    @Override
    public void onCopyTextClick(String mushroomText) {
        CommonUtil.copyToClipboard(getActivity(), mushroomText);
        getActivity().finish();
    }
}

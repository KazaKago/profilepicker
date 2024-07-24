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
import com.weathercock.profilepicker_plus.adapter.MemoListAdapter;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog.MushroomMenuDialogListener;
import com.weathercock.profilepicker_plus.entity.MemoListViewCell;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.util.CommonUtil;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * メモページのフラグメント
 * 
 * @author Kensuke
 * 
 */
public class MemoFragment extends Fragment implements MushroomMenuDialogListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memo, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    /**
     * すべてのフィールドをリフレッシュ
     */
    private void refresh() {
        ArrayList<MemoListViewCell> items = new ArrayList<MemoListViewCell>();

        //メモの読み込み
        items.addAll(getMemoCellList(ProfileEntity.CATEGORY_MEMO));

        //リストを読み込み
        MemoListAdapter adapter = new MemoListAdapter(getActivity(), items);
        ListView memoListView = (ListView) getActivity().findViewById(R.id.list_memo);
        memoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                MemoListViewCell item = (MemoListViewCell) listView.getItemAtPosition(position);
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
            memoListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    MemoListViewCell item = (MemoListViewCell) listView.getItemAtPosition(position);
                    createMushroomMenuDialog(item.getContentText());
                    return false;
                }

            });
        }
        memoListView.setAdapter(adapter);

        //リストなしメッセージを切り替える
        TextView noMessage = (TextView) getActivity().findViewById(R.id.no_memo);
        if (items.size() < 1) noMessage.setVisibility(View.VISIBLE);
        else noMessage.setVisibility(View.INVISIBLE);
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

    /**
     * メモセルのリストを返す
     * 
     * @param category
     * @return
     */
    private ArrayList<MemoListViewCell> getMemoCellList(int category) {
        ArrayList<MemoListViewCell> items = new ArrayList<MemoListViewCell>();

        // メモの読み込み
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            ArrayList<ProfileEntity> profileList = profileDao.selectByCategory(category);
            for (ProfileEntity profile : profileList) {
                MemoListViewCell content = new MemoListViewCell(profile);
                items.add(content);
            }
        } finally {
            if (db != null) db.close();
        }

        return items;
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

package com.weathercock.profilepicker_plus.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.activity.OverlayActivity;
import com.weathercock.profilepicker_plus.adapter.ProfileListAdapter;
import com.weathercock.profilepicker_plus.dao.FileDao;
import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog;
import com.weathercock.profilepicker_plus.dialog.MushroomMenuDialog.MushroomMenuDialogListener;
import com.weathercock.profilepicker_plus.entity.NameEntity;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;
import com.weathercock.profilepicker_plus.util.CommonUtil;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * メインとなるプロフィール表示クラス
 * 
 * @author Kensuke
 * 
 */
public class ProfileFragment extends Fragment implements MushroomMenuDialogListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
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
        FileDao fileDao = new FileDao(getActivity());

        //オリジナルの画像があるなら読み込み
        ImageButton faceImage = (ImageButton) getActivity().findViewById(R.id.image_face);
        faceImage.setImageResource(R.drawable.no_image);
        faceImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), OverlayActivity.class);
                startActivity(intent);
            }
        });
        File faceImageFile = fileDao.getFaceImage();
        if (faceImageFile.exists()) {
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(Uri.fromFile(faceImageFile));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                faceImage.setImageBitmap(null);
                faceImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.error_load_image), Toast.LENGTH_LONG).show();
            }
        }

        //名前を読み込み
        loadNameInfo();

        //リスト内の項目を取得
        ArrayList<ProfileListViewCell> items = new ArrayList<ProfileListViewCell>();
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_PHONE_NUMBER, getString(R.string.section_phone_number))); //電話番号
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_MAIL, getString(R.string.section_mail))); //メールアドレス
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_ADDRESS, getString(R.string.section_address))); //住所
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_MEMBER, getString(R.string.section_member))); //所属
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_BIRTHDAY, getString(R.string.section_birthday))); //誕生日

        //リストを読み込み
        ProfileListAdapter adapter = new ProfileListAdapter(getActivity(), items);
        ListView profileList = (ListView) getActivity().findViewById(R.id.list_profile);
        profileList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ProfileListViewCell item = (ProfileListViewCell) listView.getItemAtPosition(position);
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
            profileList.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    ProfileListViewCell item = (ProfileListViewCell) listView.getItemAtPosition(position);
                    createMushroomMenuDialog(item.getContentText());
                    return false;
                }

            });
        }
        profileList.setAdapter(adapter);

        //リストなしメッセージを切り替える
        TextView noMessage = (TextView) getActivity().findViewById(R.id.no_profile);
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
     * 名前情報を読み込む
     */
    private void loadNameInfo() {
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            NameDao nameDao = new NameDao(db);

            /* 名前の読み込み */
            NameEntity name = nameDao.select();
            TextView nameLabel = (TextView) getActivity().findViewById(R.id.text_name);
            nameLabel.setText(name.getLastName() + " " + name.getFirstName());
            TextView readingLabel = (TextView) getActivity().findViewById(R.id.text_reading);
            readingLabel.setText(name.getReadingLastName() + " " + name.getReadingFirstName());
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 指定されたカテゴリとヘッダーテキストにしたがってカテゴリ内リストを生成する。コンテンツが存在しない場合にはヘッダーは含まない。
     * 
     * @param category
     * @param headerText
     * @return
     */
    private ArrayList<ProfileListViewCell> getInCategoryList(int category, String headerText) {
        ArrayList<ProfileListViewCell> inCategoryList = getProfileCellList(category); //カテゴリ内セルリストを取得
        if (inCategoryList.size() > 0) {
            //コンテンツが存在するならヘッダーセルを先頭に差し込む
            inCategoryList.add(0, getHeaderCell(headerText));
        }
        return inCategoryList;
    }

    /**
     * 指定された文字列のヘッダーを返す
     * 
     * @param headerText
     * @return
     */
    private ProfileListViewCell getHeaderCell(String headerText) {
        return (new ProfileListViewCell(headerText));
    }

    /**
     * 指定されたカテゴリーのプロフィールセルのリストを返す
     * 
     * @param category
     * @return
     */
    private ArrayList<ProfileListViewCell> getProfileCellList(int category) {
        ArrayList<ProfileListViewCell> items = new ArrayList<ProfileListViewCell>();
        ProfileListViewCell item;

        // プロフィールの読み込み
        ProfileDbOpenHelper profileDbOpenHelper = new ProfileDbOpenHelper(getActivity());
        SQLiteDatabase db = profileDbOpenHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            ArrayList<ProfileEntity> profileList = profileDao.selectByCategory(category);
            for (ProfileEntity profile : profileList) {
                item = new ProfileListViewCell(profile);
                items.add(item);
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

package com.weathercock.profilepicker_plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.adapter.EditProfileListAdapter;
import com.weathercock.profilepicker_plus.dao.FileDao;
import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.PreferenceDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.dialog.EditBirthdayDialog;
import com.weathercock.profilepicker_plus.dialog.EditBirthdayDialog.EditBirthdayDialogListener;
import com.weathercock.profilepicker_plus.dialog.EditProfileDialog;
import com.weathercock.profilepicker_plus.dialog.EditProfileDialog.EditProfileDialogListener;
import com.weathercock.profilepicker_plus.entity.NameEntity;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell.ProfileListRowMode;
import com.weathercock.profilepicker_plus.listener.EditFlagListener;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * プロフィール編集画面のフラグメントクラス
 * 
 * @author Kensuke
 * 
 */
public class EditProfileFragment extends Fragment implements EditProfileDialogListener, EditBirthdayDialogListener {

    //画面回転時などActivity再起動時の一時保存用キー
    final private static String TEMPORARY_SAVE_NAME_KEY = "temp_save_name";
    final private static String TEMPORARY_SAVE_READING_KEY = "temp_save_reading";
    final private static String TEMPORARY_SAVE_CELL_LIST_KEY = "temp_save_cell_list";
    final private static String TEMPORARY_SAVE_DELETE_LIST_KEY = "temp_save_delete_list";
    final private static String TEMPORARY_SAVE_BITMAP_KEY = "temp_save_bitmap";
    final private static String TEMPORARY_SAVE_IMAGE_URI_KEY = "temp_save_image_uri";

    //画像選択インテントのリクエストキー
    final private static int REQUEST_TAKE_PICTURE = 100;
    final private static int REQUEST_PICK_IMAGE = 101;
    final private static int REQUEST_TRIMMING_IMAGE = 102;

    /**
     * カテゴリ別のインデックスを一時的に保存しておくためのキー
     */
    final private static String CATEGORY_INDEX = "category_index_";

    /**
     * トリミング画像サイズ(px)
     */
    final private static int TRIMMING_SIZE = 512;

    /**
     * セルの内容を保持するリスト
     */
    private ArrayList<ProfileListViewCell> mCellList;
    /**
     * セルの削除リスト(内部DBのIDを保持)
     */
    private ArrayList<ProfileListViewCell> mDeleteCellList;
    /**
     * フィールドのリフレッシュ中か判定(更新中にTextWatcherのイベントが走らないようにするため)
     */
    private boolean mIsRefreshing;
    /**
     * 各カテゴリ別のインデックス格納用変数
     */
    private HashMap<String, Integer> mCategoryIndex;
    /**
     * オリジナル画像Bitmap
     */
    private Bitmap mFaceImageBitmap;

    /**
     * 一時保存用の画像Uri
     */
    private Uri mTempFaceImageUri;

    /**
     * アクティビティへ編集済みフラグを通知するためのリスナー
     */
    private EditFlagListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //画面回転時などActivityが再起動する際に一時的に値を退避させておく
        EditText nameField = (EditText) getActivity().findViewById(R.id.field_edit_name1);
        outState.putString(TEMPORARY_SAVE_NAME_KEY, nameField.getText().toString());
        EditText readingField = (EditText) getActivity().findViewById(R.id.field_edit_reading2);
        outState.putString(TEMPORARY_SAVE_READING_KEY, readingField.getText().toString());
        outState.putSerializable(TEMPORARY_SAVE_CELL_LIST_KEY, mCellList);
        outState.putSerializable(TEMPORARY_SAVE_DELETE_LIST_KEY, mDeleteCellList);
        outState.putParcelable(TEMPORARY_SAVE_BITMAP_KEY, mFaceImageBitmap);
        outState.putParcelable(TEMPORARY_SAVE_IMAGE_URI_KEY, mTempFaceImageUri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //リフレッシュフラグを初期化
        mIsRefreshing = false;

        //自動でソフトウェアキーボードが出てくるのを防ぐ
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //すべての項目を読み込む
        refresh(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof EditFlagListener) {
            //リスナーをセット
            mListener = ((EditFlagListener) activity);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            //カメラで写真が撮られたとき
            try {
                //トリミングアプリへ
                mTempFaceImageUri = requestTrimmingImage(mTempFaceImageUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.error_camera), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            //ギャラリーから画像が選択されたとき
            try {
                //トリミングアプリへ
                mTempFaceImageUri = data.getData();
                mTempFaceImageUri = requestTrimmingImage(mTempFaceImageUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.error_gallary), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_TRIMMING_IMAGE && resultCode == Activity.RESULT_OK) {
            //トリミングが完了したとき
            try {
                //画像を読み込み
                mFaceImageBitmap = loadTrimmingImage(mTempFaceImageUri);
                //編集済みフラグを立てる。
                if (mListener != null) mListener.setEditFlag();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.error_trimming), Toast.LENGTH_LONG).show();
            }
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
        FileDao fileDao = new FileDao(getActivity());

        //リフレッシュ中フラグを立てる
        mIsRefreshing = true;

        //各カテゴリの新規インデックスを取得
        mCategoryIndex = new HashMap<String, Integer>();
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_PHONE_NUMBER, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_PHONE_NUMBER));
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_MAIL, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MAIL));
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_ADDRESS, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_ADDRESS));
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMBER, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MEMBER));
        mCategoryIndex.put(CATEGORY_INDEX + ProfileEntity.CATEGORY_BIRTHDAY, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_BIRTHDAY));

        //画像を読み込み(あれば)
        ImageButton faceImage = (ImageButton) getActivity().findViewById(R.id.image_edit_face);
        faceImage.setImageResource(R.drawable.no_image);
        faceImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                createEditImageDialog();
            }
        });
        File photoImage = fileDao.getFaceImage();
        if (photoImage.exists()) {
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(Uri.fromFile(photoImage));
                mFaceImageBitmap = BitmapFactory.decodeStream(is);
                faceImage.setImageBitmap(null);
                faceImage.setImageBitmap(mFaceImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.error_load_image), Toast.LENGTH_LONG).show();
            }
        }

        //名前を読み込み
        loadNameInfo();

        //リスト内の項目を取得
        mCellList = new ArrayList<ProfileListViewCell>();
        mCellList.addAll(getInCategoryList(ProfileEntity.CATEGORY_PHONE_NUMBER, getString(R.string.section_phone_number))); //電話番号
        mCellList.addAll(getInCategoryList(ProfileEntity.CATEGORY_MAIL, getString(R.string.section_mail))); //メール
        mCellList.addAll(getInCategoryList(ProfileEntity.CATEGORY_ADDRESS, getString(R.string.section_address))); //住所
        mCellList.addAll(getInCategoryList(ProfileEntity.CATEGORY_MEMBER, getString(R.string.section_member))); //所属
        mCellList.addAll(getInCategoryList(ProfileEntity.CATEGORY_BIRTHDAY, getString(R.string.section_birthday))); //誕生日

        //削除リストを初期化
        mDeleteCellList = new ArrayList<ProfileListViewCell>();

        //リストを読み込み
        EditProfileListAdapter adapter = new EditProfileListAdapter(getActivity(), mCellList);
        ListView profileList = (ListView) getActivity().findViewById(R.id.list_edit_profile);
        profileList.setOnItemClickListener(new OnItemClickListener() {
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
        profileList.setAdapter(adapter);

        //誕生日の新規作成行の調整
        tuningBirthdayRow();

        //画面回転時など一時的に保存された項目があれば上書く
        if (savedInstanceState != null) {
            EditText nameField = (EditText) getActivity().findViewById(R.id.field_edit_name1);
            nameField.setText(savedInstanceState.getString(TEMPORARY_SAVE_NAME_KEY));
            EditText readingField = (EditText) getActivity().findViewById(R.id.field_edit_reading2);
            readingField.setText(savedInstanceState.getString(TEMPORARY_SAVE_READING_KEY));
            mCellList = (ArrayList<ProfileListViewCell>) savedInstanceState.getSerializable(TEMPORARY_SAVE_CELL_LIST_KEY);
            mDeleteCellList = (ArrayList<ProfileListViewCell>) savedInstanceState.getSerializable(TEMPORARY_SAVE_DELETE_LIST_KEY);
            adapter = new EditProfileListAdapter(getActivity(), mCellList);
            profileList.setAdapter(adapter);
            mFaceImageBitmap = savedInstanceState.getParcelable(TEMPORARY_SAVE_BITMAP_KEY);
            faceImage.setImageBitmap(null);
            if (mFaceImageBitmap != null) faceImage.setImageBitmap(mFaceImageBitmap);
            else faceImage.setImageResource(R.drawable.no_image);
            mTempFaceImageUri = savedInstanceState.getParcelable(TEMPORARY_SAVE_IMAGE_URI_KEY);
        }

        //リフレッシュ中フラグを解除
        mIsRefreshing = false;

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
        if (cell.getProfileData().getCategory() != ProfileEntity.CATEGORY_BIRTHDAY) {
            EditProfileDialog editDialog = EditProfileDialog.newInstance(cell, position, isNew, isRetry);
            editDialog.setDialogListener(this);
            editDialog.show(manager, "");
        } else {
            EditBirthdayDialog editDialog = EditBirthdayDialog.newInstance(cell, position, isNew, isRetry);
            editDialog.setDialogListener(this);
            editDialog.show(manager, "");
        }
    }

    /**
     * 写真変更ダイアログ作成メソッド
     */
    private void createEditImageDialog() {
        FragmentManager manager = getActivity().getFragmentManager();
        EditImageDialog editDialog = new EditImageDialog();
        editDialog.setTargetFragment(this, 0);
        editDialog.show(manager, "");
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

            EditText nameField1 = (EditText) getActivity().findViewById(R.id.field_edit_name1);
            nameField1.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //更新中でないなら編集済みフラグを立てる。
                    if (!mIsRefreshing && mListener != null) mListener.setEditFlag();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            nameField1.setText(name.getLastName());

            EditText nameField2 = (EditText) getActivity().findViewById(R.id.field_edit_name2);
            nameField2.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //更新中でないなら編集済みフラグを立てる。
                    if (!mIsRefreshing && mListener != null) mListener.setEditFlag();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            nameField2.setText(name.getFirstName());

            EditText readingField1 = (EditText) getActivity().findViewById(R.id.field_edit_reading1);
            readingField1.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //更新中でないなら編集済みフラグを立てる。
                    if (!mIsRefreshing && mListener != null) mListener.setEditFlag();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            readingField1.setText(name.getReadingLastName());

            EditText readingField2 = (EditText) getActivity().findViewById(R.id.field_edit_reading2);
            readingField2.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //更新中でないなら編集済みフラグを立てる。
                    if (!mIsRefreshing && mListener != null) mListener.setEditFlag();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });
            readingField2.setText(name.getReadingFirstName());
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * 指定されたカテゴリとヘッダーテキストにしたがってカテゴリ内リストを生成する。
     * 
     * @param category
     * @param headerText
     * @return
     */
    private ArrayList<ProfileListViewCell> getInCategoryList(int category, String headerText) {

        //カテゴリ内セルリストを取得
        ArrayList<ProfileListViewCell> inCategoryList = new ArrayList<ProfileListViewCell>();
        inCategoryList.add(getHeaderCell(headerText)); //ヘッダーセル
        inCategoryList.addAll(getProfileCellList(category)); //コンテンツセル
        inCategoryList.add(getNewAdditionCell(category)); //新規追加セル

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
     * 誕生日の新規作成セルの有効無効を切り替える
     */
    private void tuningBirthdayRow() {

        ListView profileList = (ListView) getActivity().findViewById(R.id.list_edit_profile);
        EditProfileListAdapter adapter = (EditProfileListAdapter) profileList.getAdapter();

        int birthdayContentIndex = -1;
        int birthdayNewAdditionIndex = -1;
        for (int i = 0; i < adapter.getCount(); i++) {
            ProfileListViewCell cell = adapter.getItem(i);
            if (cell.getProfileData().getCategory() == ProfileEntity.CATEGORY_BIRTHDAY && cell.getRowMode() == ProfileListRowMode.CONTENT) birthdayContentIndex = i;
            if (cell.getProfileData().getCategory() == ProfileEntity.CATEGORY_BIRTHDAY && cell.getRowMode() == ProfileListRowMode.NEW_ADDITION) birthdayNewAdditionIndex = i;
        }

        if (birthdayContentIndex != -1) {
            adapter.setButtonEnabled(birthdayNewAdditionIndex, false);
            adapter.notifyDataSetChanged();
        } else {
            adapter.setButtonEnabled(birthdayNewAdditionIndex, true);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * カメラアプリを起動
     */
    public void requestTakePicture() {
        //カメラ撮影画像を撮影後にそのままBitmapで取得すると画質が悪いため、
        //撮影した画像を一度外部ストレージに保存しトリミングの際は保存先Uriから画像を読み込むようにする
        FileDao fileDao = new FileDao(getActivity());

        //写真のパラメータを設定
        File tempPhotoPath = fileDao.getTempFaceImage();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, tempPhotoPath.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mTempFaceImageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //カメラアプリ起動
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempFaceImageUri);
        startActivityForResult(intent, REQUEST_TAKE_PICTURE);
    }

    /**
     * ギャラリーアプリを起動
     */
    public void requestPickupImage() {
        //ギャラリーを起動して画像を選択
        Intent intent = new Intent();
        //        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    /**
     * 画像を元に戻す
     */
    public void resetImage() {

        //一時保存Bitmapを消去
        mFaceImageBitmap = null;

        //表示をNo Imageに差し替え
        ImageButton faceImage = (ImageButton) getActivity().findViewById(R.id.image_edit_face);
        faceImage.setImageResource(R.drawable.no_image);

        //編集済みフラグを立てる。
        if (mListener != null) mListener.setEditFlag();
    }

    /**
     * トリミングアプリを起動
     * 
     * @param mImageUri
     * @return
     */
    private Uri requestTrimmingImage(Uri mImageUri) {
        FileDao fileDao = new FileDao(getActivity());
        Uri faceImageUri = Uri.fromFile(fileDao.getTempFaceImage());

        //トリミングアプリを起動
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setData(mImageUri);
        intent.putExtra("outputX", TRIMMING_SIZE); // トリミング後の画像の幅(px)
        intent.putExtra("outputY", TRIMMING_SIZE); // トリミング後の画像の高さ(px)
        intent.putExtra("aspectX", 1); // トリミング後の画像のアスペクト比(X)
        intent.putExtra("aspectY", 1); // トリミング後の画像のアスペクト比(Y)
        intent.putExtra("scale", true); // トリミング中の枠を拡大縮小できるかどうか
        //        intent.putExtra("return-data", true); // トリミングしたデータを返却するかどうか
        intent.putExtra(MediaStore.EXTRA_OUTPUT, faceImageUri);
        startActivityForResult(intent, REQUEST_TRIMMING_IMAGE);

        return faceImageUri;
    }

    /**
     * トリミング後の画像を表示
     * 
     * @param mImageUri
     * @return
     * @throws FileNotFoundException
     */
    private Bitmap loadTrimmingImage(Uri mImageUri) throws FileNotFoundException {

        InputStream is = getActivity().getContentResolver().openInputStream(mImageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(is);

        ImageButton faceImage = (ImageButton) getActivity().findViewById(R.id.image_edit_face);
        faceImage.setImageBitmap(null);
        faceImage.setImageBitmap(bitmap);

        return bitmap;
    }

    /**
     * 項目の保存を行う。保存ボタンをタップされたときにActivityから通知される
     * 
     * @param edit
     * @param db
     * @throws SQLException
     * @throws IOException
     */
    public void onSave(SharedPreferences.Editor edit, SQLiteDatabase db) throws SQLException, IOException {
        ProfileDao profileDao = new ProfileDao(db);
        NameDao nameDao = new NameDao(db);
        PreferenceDao preferenceDao = new PreferenceDao(edit);
        FileDao fileDao = new FileDao(getActivity());

        //カテゴリ別のインデックスを保存する
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_PHONE_NUMBER, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_PHONE_NUMBER));
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MAIL, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_MAIL));
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_ADDRESS, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_ADDRESS));
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MEMBER, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMBER));
        preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_BIRTHDAY, mCategoryIndex.get(CATEGORY_INDEX + ProfileEntity.CATEGORY_BIRTHDAY));

        //画像を保存
        File photoImage = fileDao.getFaceImage();
        if (mFaceImageBitmap != null) {
            OutputStream os = new FileOutputStream(photoImage);
            mFaceImageBitmap.compress(CompressFormat.JPEG, 100, os);
            os.close();
        } else {
            //保存画像を消去
            photoImage.delete();
        }

        //名前をデータベースへ保存する
        EditText nameField1 = (EditText) getActivity().findViewById(R.id.field_edit_name1);
        String lastName = nameField1.getText().toString();
        EditText nameField2 = (EditText) getActivity().findViewById(R.id.field_edit_name2);
        String firstName = nameField2.getText().toString();
        EditText readingField1 = (EditText) getActivity().findViewById(R.id.field_edit_reading1);
        String readingLastName = readingField1.getText().toString();
        EditText readingField2 = (EditText) getActivity().findViewById(R.id.field_edit_reading2);
        String readingFirstName = readingField2.getText().toString();
        NameEntity nameEntity = new NameEntity(firstName, lastName, readingFirstName, readingLastName);
        nameDao.insert(nameEntity);

        //セルの要素をデータベースへすべて保存する
        for (ProfileListViewCell cell : mCellList) {
            //コンテンツ行のみ抽出
            if (cell.getRowMode() == ProfileListRowMode.CONTENT) {
                ProfileEntity profile = cell.getProfileData();
                if (profile.getId() < 0) {
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

    /**
     * 画像選択メニューダイアログを制御するクラス
     * 
     * @author Kensuke
     * 
     */
    public static class EditImageDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //ダイアログの生成
            final CharSequence[] menuItems = getResources().getStringArray(R.array.menu_image);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.menu_image_title));
            builder.setItems(menuItems, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            //写真を撮る
                            ((EditProfileFragment) getTargetFragment()).requestTakePicture();
                            break;
                        case 1:
                            //ギャラリーから選ぶ
                            ((EditProfileFragment) getTargetFragment()).requestPickupImage();
                            break;
                        case 2:
                            //画像を設定しない
                            ((EditProfileFragment) getTargetFragment()).resetImage();
                            break;
                        default:
                            break;
                    }
                }
            });

            return builder.create();
        }

    }

    /* EditDialogListener EditBirthdayListener */

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

            //誕生日の新規作成行の調整
            tuningBirthdayRow();

            //リストの要素を読み込み直し
            ListView profileList = (ListView) getActivity().findViewById(R.id.list_edit_profile);
            EditProfileListAdapter adapter = (EditProfileListAdapter) profileList.getAdapter();
            adapter.notifyDataSetChanged();

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

                //誕生日の新規作成行の調整
                tuningBirthdayRow();

                //リストの要素を読み込み直し
                ListView profileList = (ListView) getActivity().findViewById(R.id.list_edit_profile);
                EditProfileListAdapter adapter = (EditProfileListAdapter) profileList.getAdapter();
                adapter.notifyDataSetChanged();

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

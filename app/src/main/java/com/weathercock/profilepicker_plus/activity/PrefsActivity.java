package com.weathercock.profilepicker_plus.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.constants.AppConstants;
import com.weathercock.profilepicker_plus.dao.FileDao;
import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.PreferenceDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.entity.NameEntity;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.util.CommonUtil;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * 設定画面のアクティビティ
 * 
 * @author Kensuke
 * 
 */
public class PrefsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Display the fragment as the main content.
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            PrefsFragment prefsFragment = new PrefsFragment();
            transaction.replace(android.R.id.content, prefsFragment);

            transaction.commit();
        }

        //アプリケーションアイコンをホームボタンとして利用
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backHomeActivity();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ホームへ戻る
     */
    private void backHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * 設定画面のフラグメント
     * 
     * @author Kensuke
     * 
     */
    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            //プログレスダイアログ表示中の画面回転対策
            setRetainInstance(true);

//            Preference backupPref = findPreference(getString(R.string.pref_backup));
//            backupPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    comfirmBackup();
//                    return false;
//                }
//            });
//            Preference restorePref = findPreference(getString(R.string.pref_restore));
//            restorePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    comfirmRestore();
//                    return false;
//                }
//            });
            Preference qrCodeHelp = findPreference(getString(R.string.pref_help_qr));
            qrCodeHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showQrCodeHelp();
                    return false;
                }
            });
            Preference mushroomHelp = findPreference(getString(R.string.pref_help_mushroom));
            mushroomHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showMushroomHelp();
                    return false;
                }
            });
            Preference aboutApp = findPreference(getString(R.string.pref_about));
            aboutApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showAboutApp();
                    return false;
                }
            });

        }

        /* バックアップ処理 */

        /**
         * バックアップ実行確認ダイアログ
         */
        private void comfirmBackup() {
            //確認ダイアログを表示
            FragmentManager manager = getFragmentManager();
            BackupDialog backupDialog = new BackupDialog();
            backupDialog.setTargetFragment(this, 0);
            backupDialog.show(manager, "");
        }

        /**
         * バックアップ確認ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class BackupDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_backup));
                builder.setMessage(getString(R.string.dialog_backup));
                builder.setPositiveButton(getString(R.string.execute), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((PrefsFragment) getTargetFragment()).startBackup();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), null);
                return builder.create();
            }
        }

        /**
         * バックアップ開始メソッド
         */
        private void startBackup() {
            //バックアップをバックグラウンドで実行
            BackupTask backupTask = new BackupTask();
            backupTask.execute();
        }

        /**
         * バックアップをバックグラウンドで行う非同期処理
         * 
         * @author Kensuke
         * 
         */
        private class BackupTask extends AsyncTask<Void, Void, Boolean> {

            /**
             * ダイアログのインスタンス
             */
            private BackupProgressDialog backupProgress;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                backupProgress = new BackupProgressDialog();
                backupProgress.show(getFragmentManager(), "");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean result = true;

                try {
                    String backupStr = createBackupJson();
                    createBackupFile(backupStr);
                    copyBakcupPicture();
                    CommonUtil.createNomedia(new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH));
                } catch (JSONException e) {
                    e.printStackTrace();
                    result = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    result = false;
                }

                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                backupProgress.getDialog().dismiss();
                finishBackup(result);
            }

            /**
             * ユーザー情報をJSON文字列として生成
             * 
             * @return
             * @throws JSONException
             */
            private String createBackupJson() throws JSONException {
                JSONObject backupJson = new JSONObject();

                ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(getActivity());
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                try {
                    ProfileDao profileDao = new ProfileDao(db);
                    NameDao nameDao = new NameDao(db);

                    SharedPreferences pref = PreferenceDao.getSharedPreferences(getActivity());
                    PreferenceDao preferenceDao = new PreferenceDao(pref);

                    //バックアップ日時を保存
                    backupJson.put(AppConstants.BACKUP_DATE_KEY, System.currentTimeMillis());

                    //PreferenceをJsonに加工
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_PHONE_NUMBER, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_PHONE_NUMBER));
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MAIL, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MAIL));
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_ADDRESS, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_ADDRESS));
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMBER, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MEMBER));
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_BIRTHDAY, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_BIRTHDAY));
                    backupJson.put(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMO, preferenceDao.getCategoryIndex(ProfileEntity.CATEGORY_MEMO));

                    //名前情報をJsonに加工
                    NameEntity name = nameDao.select();
                    backupJson.put(NameDao.FIRST_NAME, name.getFirstName());
                    backupJson.put(NameDao.LAST_NAME, name.getLastName());
                    backupJson.put(NameDao.READING_FIRST_NAME, name.getReadingFirstName());
                    backupJson.put(NameDao.READING_LAST_NAME, name.getReadingLastName());

                    //プロフィール情報をJsonに加工
                    JSONArray profileJsonArray = new JSONArray();
                    ArrayList<ProfileEntity> profileList = new ArrayList<ProfileEntity>();
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_PHONE_NUMBER));
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_MAIL));
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_ADDRESS));
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_MEMBER));
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_BIRTHDAY));
                    profileList.addAll(profileDao.selectByCategory(ProfileEntity.CATEGORY_MEMO));
                    for (ProfileEntity profile : profileList) {
                        JSONObject profileJson = new JSONObject();
                        profileJson.put(ProfileDao.COLUMN_ID, profile.getId());
                        profileJson.put(ProfileDao.COLUMN_CATEGORY, profile.getCategory());
                        profileJson.put(ProfileDao.COLUMN_CONTENT_TAG, profile.getContentTag());
                        profileJson.put(ProfileDao.COLUMN_CONTENT_TEXT, profile.getContentText());
                        profileJson.put(ProfileDao.COLUMN_ALLOW_SHARE, profile.getAllowShare());
                        profileJson.put(ProfileDao.COLUMN_ORDER_INDEX, profile.getOrderIndex());
                        profileJsonArray.put(profileJson);
                    }
                    backupJson.put(AppConstants.PROFILE_LIST_KEY, profileJsonArray);
                } finally {
                    if (db != null) db.close();
                }

                return backupJson.toString(4);
            }

            /**
             * 文字列をバックアップファイルとしてSDカードへ出力
             * 
             * @param backupStr
             * @throws IOException
             */
            private void createBackupFile(String backupStr) throws IOException {
                //文字列をSDカード内に書き込む
                File backupFile = new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH + AppConstants.BACKUP_JSON_NAME);
                backupFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(backupFile, false);
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                writer.write(backupStr);
                writer.flush();
                writer.close();
                fos.close();
            }

            /**
             * 独自画像があればバックアップフォルダへコピーする
             * 
             * @throws IOException
             */
            private void copyBakcupPicture() throws IOException {
                FileDao fileDao = new FileDao(getActivity());
                File currentPicture = fileDao.getFaceImage();
                File backupPicture = new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH + AppConstants.BACKUP_PICTURE_NAME);

                if (currentPicture.exists()) CommonUtil.copyTransfer(currentPicture.getAbsolutePath(), backupPicture.getAbsolutePath());
                else if (backupPicture.exists()) backupPicture.delete();
            }

        }

        /**
         * バックアッププログレスダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class BackupProgressDialog extends DialogFragment {

            /**
             * ダイアログインスタンス
             */
            private static ProgressDialog progressDialog;

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                if (savedInstanceState == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle(getString(R.string.dialog_during_backup_title));
                    progressDialog.setMessage(getString(R.string.dialog_during_backup));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                }

                return progressDialog;
            }

            @Override
            public Dialog getDialog() {
                return progressDialog;
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                progressDialog = null;
            }

        }

        /**
         * バックアップ終了後のメソッド
         * 
         * @param result
         */
        private void finishBackup(boolean result) {
            //終了ダイアログを表示
            FragmentManager manager = getFragmentManager();
            if (result) {
                BackupCompleteDialog backupCompleteDialog = new BackupCompleteDialog();
                backupCompleteDialog.show(manager, "");
            } else {
                BackupErrorDialog backupErrorDialog = new BackupErrorDialog();
                backupErrorDialog.show(manager, "");
            }
        }

        /**
         * バックアップ完了ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class BackupCompleteDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_backup));
                builder.setMessage(getString(R.string.dialog_complete_backup));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /**
         * バックアップ失敗ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class BackupErrorDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.error));
                builder.setMessage(getString(R.string.dialog_error_backup));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /* リストア処理 */

        /**
         * リストア確認ダイアログ
         */
        private void comfirmRestore() {
            //確認ダイアログを表示
            FragmentManager manager = getFragmentManager();
            RestoreDialog restoreDialog = new RestoreDialog();
            restoreDialog.setTargetFragment(this, 0);
            restoreDialog.show(manager, "");
        }

        /**
         * リストア確認ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class RestoreDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_restore));
                builder.setMessage(getString(R.string.dialog_restore));
                builder.setPositiveButton(getString(R.string.execute), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((PrefsFragment) getTargetFragment()).startRestore();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), null);
                return builder.create();
            }
        }

        /**
         * リストア実行
         */
        private void startRestore() {
            //リストアをバックグラウンドで実行
            RestoreTask restoreTask = new RestoreTask();
            restoreTask.execute();
        }

        /**
         * リストアをバックグラウンドで行う非同期処理
         * 
         * @author Kensuke
         * 
         */
        private class RestoreTask extends AsyncTask<Void, Void, Boolean> {

            /**
             * ダイアログのインスタンス
             */
            private RestoreProgressDialog restoreProgress;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                //存在チェック
                File backupFile = new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH + AppConstants.BACKUP_JSON_NAME);
                if (backupFile.exists()) {
                    restoreProgress = new RestoreProgressDialog();
                    restoreProgress.show(getFragmentManager(), "");
                } else {
                    BackupFileNotFoundDialog backupFileNotFoundDialog = new BackupFileNotFoundDialog();
                    backupFileNotFoundDialog.show(getFragmentManager(), "");
                    cancel(true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean result = true;
                FileDao fileDao = new FileDao(getActivity());

                try {
                    //リストア処理
                    restorePicture();
                    String backupStr = readBackupFile();
                    result = restoreBackupJson(backupStr);
                } catch (IOException e) {
                    e.printStackTrace();
                    result = false;
                    fileDao.rollbackPicture();
                } catch (JSONException e) {
                    e.printStackTrace();
                    result = false;
                    fileDao.rollbackPicture();
                }

                //ロールバック用の画像を削除
                fileDao.deleteRollbackPicture();

                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                restoreProgress.getDialog().dismiss();
                finishRestore(result);
            }

            /**
             * バックアップファイルを読み込む
             * 
             * @return
             * @throws IOException
             */
            private String readBackupFile() throws IOException {
                //バックアップファイルから文字列を読み込む
                File backupFile = new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH + AppConstants.BACKUP_JSON_NAME);
                FileInputStream fis = new FileInputStream(backupFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String jsonStr = "";
                String jsonStrParts = "";
                while ((jsonStrParts = reader.readLine()) != null) {
                    jsonStr = jsonStr + jsonStrParts;
                }
                reader.close();
                fis.close();

                return jsonStr;
            }

            /**
             * json文字列をもとに復元する
             * 
             * @param backupJsonStr
             * @return
             * @throws JSONException
             */
            private boolean restoreBackupJson(String backupJsonStr) throws JSONException {
                boolean result = true;
                JSONObject backupJson = new JSONObject(backupJsonStr);

                //Preference
                SharedPreferences pref = PreferenceDao.getSharedPreferences(getActivity());
                SharedPreferences.Editor edit = pref.edit();
                PreferenceDao preferenceDao = new PreferenceDao(edit);

                //Database
                ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(getActivity());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ProfileDao profileDao = new ProfileDao(db);
                NameDao nameDao = new NameDao(db);

                //トランザクション開始
                db.beginTransaction();
                try {

                    //設定を復元
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_PHONE_NUMBER, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_PHONE_NUMBER));
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MAIL, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MAIL));
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_ADDRESS, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_ADDRESS));
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MEMBER, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMBER));
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_BIRTHDAY, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_BIRTHDAY));
                    preferenceDao.setCategoryIndex(ProfileEntity.CATEGORY_MEMO, backupJson.getInt(PreferenceDao.PREF_CATEGORY_INDEX + ProfileEntity.CATEGORY_MEMO));

                    //まずプロフィールをすべて削除
                    profileDao.deleteAll();

                    //名前を復元
                    String firstName = backupJson.getString(NameDao.FIRST_NAME);
                    String lastName = backupJson.getString(NameDao.LAST_NAME);
                    String readingFirstName = backupJson.getString(NameDao.READING_FIRST_NAME);
                    String readingLastName = backupJson.getString(NameDao.READING_LAST_NAME);
                    NameEntity nameEntity = new NameEntity(firstName, lastName, readingFirstName, readingLastName);
                    nameDao.insert(nameEntity);

                    //プロフィールを復元
                    JSONArray profileJsonArray = backupJson.getJSONArray(AppConstants.PROFILE_LIST_KEY);
                    for (int i = 0; i < profileJsonArray.length(); i++) {
                        JSONObject profileJson = profileJsonArray.getJSONObject(i);
                        int category = profileJson.getInt(ProfileDao.COLUMN_CATEGORY);

                        String contentTag = profileJson.getString(ProfileDao.COLUMN_CONTENT_TAG);
                        String contentText = profileJson.getString(ProfileDao.COLUMN_CONTENT_TEXT);
                        boolean allowShare = profileJson.getBoolean(ProfileDao.COLUMN_ALLOW_SHARE);
                        int orderIndex = profileJson.getInt(ProfileDao.COLUMN_ORDER_INDEX);
                        ProfileEntity profileEntity = new ProfileEntity(category, contentTag, contentText, allowShare, orderIndex);
                        profileDao.insert(profileEntity);
                    }

                    //DBトランザクション完了
                    db.setTransactionSuccessful();
                    //Preferenceコミット
                    edit.commit();

                } catch (SQLException e) {
                    e.printStackTrace();
                    result = false;
                } finally {
                    db.endTransaction();
                    if (db != null) db.close();
                }

                return result;
            }

            /**
             * 画像を復元する(ロールバック用に元画像はリネームして持っておく)
             * 
             * @return
             * 
             * @throws IOException
             */
            private boolean restorePicture() throws IOException {
                boolean result = true;
                FileDao fileDao = new FileDao(getActivity());
                File currentPicture = fileDao.getFaceImage();
                File backupPicture = new File(CommonUtil.getPreferredStorageDir(), AppConstants.BACKUP_FOLDER_PATH + AppConstants.BACKUP_PICTURE_NAME);

                if (backupPicture.exists()) {
                    fileDao.setupRollbackImage();
                    CommonUtil.copyTransfer(backupPicture.getAbsolutePath(), currentPicture.getAbsolutePath());
                } else {
                    result = currentPicture.delete();
                }

                return result;
            }
        }

        /**
         * リストアプログレスダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class RestoreProgressDialog extends DialogFragment {

            /**
             * ダイアログインスタンス
             */
            private static ProgressDialog progressDialog;

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle(getString(R.string.dialog_during_restore_title));
                    progressDialog.setMessage(getString(R.string.dialog_during_restore));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                }

                return progressDialog;
            }

            @Override
            public Dialog getDialog() {
                return progressDialog;
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                progressDialog = null;
            }

        }

        /**
         * リストア終了後のメソッド
         * 
         * @param result
         */
        private void finishRestore(boolean result) {
            //終了ダイアログを表示
            FragmentManager manager = getFragmentManager();
            if (result) {
                RestoreCompleteDialog restoreCompleteDialog = new RestoreCompleteDialog();
                restoreCompleteDialog.show(manager, "");
            } else {
                RestoreErrorDialog restoreErrorDialog = new RestoreErrorDialog();
                restoreErrorDialog.show(manager, "");
            }

        }

        /**
         * リストア完了ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class RestoreCompleteDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_restore));
                builder.setMessage(getString(R.string.dialog_complete_restore));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /**
         * リストア失敗ダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class RestoreErrorDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.error));
                builder.setMessage(getString(R.string.dialog_error_restore));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /**
         * バックアップファイルが存在しないダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class BackupFileNotFoundDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.error));
                builder.setMessage(getString(R.string.dialog_backup_not_found));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /* ヘルプ */

        /**
         * QRコードについてがタップされたとき
         */
        private void showQrCodeHelp() {
            FragmentManager manager = getFragmentManager();
            QrCodeDialog qrCodeDialog = new QrCodeDialog();
            qrCodeDialog.show(manager, "");
        }

        /**
         * QRコードについてダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class QrCodeDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_help_qr));
                builder.setMessage(getString(R.string.dialog_qr_help));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /**
         * マッシュルームについてがタップされたとき
         */
        private void showMushroomHelp() {
            FragmentManager manager = getFragmentManager();
            MushroomDialog mushroomDialog = new MushroomDialog();
            mushroomDialog.show(manager, "");
        }

        /**
         * マッシュルームについてダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class MushroomDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_help_mushroom));
                builder.setMessage(getString(R.string.dialog_mushroom_help));
                builder.setPositiveButton(getString(R.string.ok), null);
                return builder.create();
            }

        }

        /**
         * このアプリについてがタップされたとき
         */
        private void showAboutApp() {
            FragmentManager manager = getFragmentManager();
            AboutAppDialog aboutAppDialog = new AboutAppDialog();
            aboutAppDialog.show(manager, "");
        }

        /**
         * このアプリについてダイアログを制御する内部クラス
         * 
         * @author Kensuke
         * 
         */
        public static class AboutAppDialog extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                //バージョン名を取得
                String versionName = "";
                try {
                    versionName = CommonUtil.getVersionName(getActivity());
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }

                //Viewの読み込みと現在の設定の復元
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View aboutAppLayout = inflater.inflate(R.layout.dialog_about_app, null, false);
                TextView appVersion = (TextView) aboutAppLayout.findViewById(R.id.text_app_version);
                appVersion.setText(getString(R.string.dialog_about_app_version, versionName));
                TextView copyRight = (TextView) aboutAppLayout.findViewById(R.id.text_copyright);
                copyRight.setText(getString(R.string.dialog_about_copyright, getString(R.string.dialog_about_creator_content)));

                //ダイアログの生成
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.pref_about));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.setView(aboutAppLayout);
                return builder.create();
            }
        }

    }

}

package com.weathercock.profilepicker_plus.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.dao.FileDao;
import com.weathercock.profilepicker_plus.dao.PreferenceDao;
import com.weathercock.profilepicker_plus.fragment.EditMemoFragment;
import com.weathercock.profilepicker_plus.fragment.EditPasswordFragment;
import com.weathercock.profilepicker_plus.fragment.EditProfileFragment;
import com.weathercock.profilepicker_plus.listener.EditFlagListener;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

import java.io.IOException;

/**
 * 編集画面アクティビティ
 *
 * @author PTAMURA
 */
public class EditActivity extends Activity implements EditFlagListener {

    //ページ数
    final private static int PAGE_COUNT = 2;

    //ページ番号
    final private static int PAGE_INDEX_PROFILE = 0;
    final private static int PAGE_INDEX_MEMO = 1;
    final private static int PAGE_INDEX_PASSWORD = 2;

    //アクティビティ再起動時の一時退避用のキー
    final private static String TEMPORARY_SAVE_IS_EDIT_KEY = "temp_save_is_edit";

    //編集済みフラグ
    private boolean mIsEdit;

    //ViewPager関連
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //キャッシュするページ範囲をすべてのページに拡大(デフォルトは1)
        mViewPager.setOffscreenPageLimit(PAGE_COUNT - 1);

        //初期表示するページ
        mViewPager.setCurrentItem(PAGE_INDEX_PROFILE);

        //アプリケーションアイコンをホームボタンとして利用
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //変更フラグは最初は変更なしにしておく
        mIsEdit = false;

        if (savedInstanceState != null) {
            //一時保存された値があれば復元
            mIsEdit = savedInstanceState.getBoolean(TEMPORARY_SAVE_IS_EDIT_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //画面回転時などActivityが再起動する際に一時的に値を退避させておく
        outState.putBoolean(TEMPORARY_SAVE_IS_EDIT_KEY, mIsEdit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            confirmSave();
        } else if (item.getItemId() == R.id.menu_cancel) {
            confirmToPrevious();
        } else if (item.getItemId() == android.R.id.home) {
            confirmToHome();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //戻るボタンをフック
            confirmToPrevious();
            return false;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * ホームへ戻る確認ダイアログ(編集済時のみ)
     */
    private void confirmToHome() {
        if (mIsEdit) {
            FragmentManager manager = getFragmentManager();
            ToHomeDialog toHomeDialog = new ToHomeDialog();
            toHomeDialog.show(manager, "");
        } else {
            backHomeActivity();
        }
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
     * 前の画面に戻る確認ダイアログ(編集済み時のみ)
     */
    private void confirmToPrevious() {
        if (mIsEdit) {
            FragmentManager manager = getFragmentManager();
            ToPreviousDialog toPreviousDialog = new ToPreviousDialog();
            toPreviousDialog.show(manager, "");
        } else {
            backPreviousActivity();
        }
    }

    /**
     * 前の画面に戻る
     */
    private void backPreviousActivity() {
        finish();
    }

    /**
     * 保存確認ダイアログ表示
     */
    private void confirmSave() {
        FragmentManager manager = getFragmentManager();
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.show(manager, "");
    }

    /**
     * 項目をDBに保存し、前の画面へ戻る
     */
    private void saveEditContents() {
        //Preferenceを取得
        SharedPreferences pref = PreferenceDao.getSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();

        //DBを取得
        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //ファイル制御クラスを取得
        FileDao fileDao = new FileDao(this);

        //トランザクション開始
        db.beginTransaction();
        try {

            //ロールバック用に画像を退避させておく
            fileDao.setupRollbackImage();

            //保存後アクティビティを抜ける
            EditProfileFragment editProfileFragment = (EditProfileFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, PAGE_INDEX_PROFILE);
            editProfileFragment.onSave(editor, db);
            EditMemoFragment editMemoFragment = (EditMemoFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, PAGE_INDEX_MEMO);
            editMemoFragment.onSave(editor, db);

            //DBトランザクション完了
            db.setTransactionSuccessful();
            //Preferenceコミット
            editor.commit();

            finish();
        } catch (SQLException e) {
            e.printStackTrace();
            fileDao.rollbackPicture();

            showSaveErrorDialog();
        } catch (IOException e) {
            e.printStackTrace();
            fileDao.rollbackPicture();

            showSaveErrorDialog();
        } finally {
            db.endTransaction();
            if (db != null) db.close();

            //ロールバック画像を削除
            fileDao.deleteRollbackPicture();
        }

    }

    /**
     * 保存失敗ダイアログを表示する
     */
    private void showSaveErrorDialog() {
        FragmentManager manager = getFragmentManager();
        SaveErrorDialog saveErrorDialog = new SaveErrorDialog();
        saveErrorDialog.show(manager, "");
    }

    /**
     * ホームに戻るダイアログを制御する内部クラス
     *
     * @author Kensuke
     */
    public static class ToHomeDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.comfirmation));
            builder.setMessage(getString(R.string.comfirm_dest));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((EditActivity) getActivity()).backHomeActivity();
                }

            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            return builder.create();
        }

    }

    /**
     * 前のActivityに戻るダイアログを制御する内部クラス
     *
     * @author Kensuke
     */
    public static class ToPreviousDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.comfirmation));
            builder.setMessage(getString(R.string.comfirm_dest));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((EditActivity) getActivity()).backPreviousActivity();
                }

            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            return builder.create();
        }

    }

    /**
     * 保存ダイアログを制御する内部クラス
     *
     * @author Kensuke
     */
    public static class SaveDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.comfirmation));
            builder.setMessage(getString(R.string.comfirm_save));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((EditActivity) getActivity()).saveEditContents();
                }

            });
            builder.setNegativeButton(getString(R.string.cancel), null);
            return builder.create();
        }

    }

    /**
     * 保存失敗ダイアログを制御する内部クラス
     *
     * @author Kensuke
     */
    public static class SaveErrorDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.dialog_save_failure));
            builder.setPositiveButton(getString(R.string.ok), null);
            builder.setNegativeButton(getString(R.string.cancel), null);
            return builder.create();
        }

    }

    /**
     * ViewPager管理用の内部クラス
     *
     * @author Kensuke
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        /**
         * コンストラクタ
         *
         * @param fm
         */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case PAGE_INDEX_MEMO:
                    fragment = new EditMemoFragment();
                    break;
                case PAGE_INDEX_PROFILE:
                    fragment = new EditProfileFragment();
                    break;
                case PAGE_INDEX_PASSWORD:
                    fragment = new EditPasswordFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String pageTitle = "";
            switch (position) {
                case PAGE_INDEX_MEMO:
                    pageTitle = getString(R.string.title_edit_section1);
                    break;
                case PAGE_INDEX_PROFILE:
                    pageTitle = getString(R.string.title_edit_section2);
                    break;
                case PAGE_INDEX_PASSWORD:
                    pageTitle = getString(R.string.title_edit_section3);
                    break;
                default:
                    break;
            }
            return pageTitle;
        }
    }

    /* EditFlagListener */

    @Override
    public void setEditFlag() {
        mIsEdit = true;
    }
}

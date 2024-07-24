package com.weathercock.profilepicker_plus.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.dialog.ShareTextDialog;
import com.weathercock.profilepicker_plus.dialog.ShareTextDialog.EditShareTextDialogListener;
import com.weathercock.profilepicker_plus.entity.NameEntity;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.fragment.MemoFragment;
import com.weathercock.profilepicker_plus.fragment.PasswordFragment;
import com.weathercock.profilepicker_plus.fragment.ProfileFragment;
import com.weathercock.profilepicker_plus.util.CommonUtil;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

import java.util.ArrayList;

/**
 * ホーム画面アクティビティ
 *
 * @author PTAMURA
 */
public class ProfileActivity extends Activity implements EditShareTextDialogListener {

    //ページ数
    final private static int PAGE_COUNT = 2;

    //ページ番号
    final private static int PAGE_INDEX_PROFILE = 0;
    final private static int PAGE_INDEX_MEMO = 1;
    final private static int PAGE_INDEX_PASSWORD = 2;

    //ViewPager関連
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //初期表示するページ
        mViewPager.setCurrentItem(PAGE_INDEX_PROFILE);

        //マッシュルーム経由のときだけアプリケーションアイコンをホームボタンとして利用
        if (CommonUtil.isMushroom(this)) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!CommonUtil.isMushroom(this)) getMenuInflater().inflate(R.menu.activity_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backPreviousActivity();
        } else if (item.getItemId() == R.id.menu_edit) {
            toEditActivity();
        } else if (item.getItemId() == R.id.menu_qrcode) {
            showQRCode();
        } else if (item.getItemId() == R.id.menu_share) {
            shareProfile();
        } else if (item.getItemId() == R.id.menu_settings) {
            toPrefsActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 前の画面に戻る
     */
    private void backPreviousActivity() {
        finish();
    }

    /**
     * 編集画面へ遷移
     */
    private void toEditActivity() {
        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        startActivity(intent);
    }

    /**
     * 設定画面へ遷移
     */
    private void toPrefsActivity() {
        Intent intent = new Intent(getApplicationContext(), PrefsActivity.class);
        startActivity(intent);
    }

    /**
     * プロフィールを共有メニューへ送る
     */
    private void shareProfile() {

        //共有用テキストを取得
        String shareText = getShareText();

        //共有内容確認ダイアログを作成
        FragmentManager manager = getFragmentManager();
        ShareTextDialog editShareTextDialog = ShareTextDialog.newInstance(shareText);
        editShareTextDialog.setDialogListener(this);
        editShareTextDialog.show(manager, "");

    }

    /**
     * QRコードを生成し表示する
     */
    private void showQRCode() {
        Intent intent = new Intent(getApplicationContext(), QrCodeActivity.class);
        startActivity(intent);
    }

    /**
     * 共有用のテキストを生成する
     *
     * @return
     */
    public String getShareText() {
        String shareText = "";
        String categoryText = "";
        ArrayList<ProfileEntity> items = new ArrayList<ProfileEntity>();

        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            NameDao nameDao = new NameDao(db);

            NameEntity name = nameDao.select();
            categoryText = getString(R.string.section_name);
            shareText = getString(R.string.share_name, categoryText, name.getLastName(), name.getFirstName(), name.getReadingLastName(), name.getReadingFirstName());

            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_PHONE_NUMBER);
            categoryText = getString(R.string.section_phone_number);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_profile, categoryText, item.getContentTag(), item.getContentText());
            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_MAIL);
            categoryText = getString(R.string.section_mail);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_profile, categoryText, item.getContentTag(), item.getContentText());
            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_ADDRESS);
            categoryText = getString(R.string.section_address);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_profile, categoryText, item.getContentTag(), item.getContentText());
            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_MEMBER);
            categoryText = getString(R.string.section_member);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_profile, categoryText, item.getContentTag(), item.getContentText());
            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_BIRTHDAY);
            categoryText = getString(R.string.section_birthday);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_birthday_or_memo, categoryText, item.getContentText());
            items = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_MEMO);
            categoryText = getString(R.string.section_memo);
            for (ProfileEntity item : items)
                shareText = shareText + getString(R.string.share_birthday_or_memo, item.getContentTag(), item.getContentText());
        } finally {
            if (db != null) db.close();
        }

        return shareText;
    }

    /**
     * ViewPager管理用の内部クラス
     *
     * @author Kensuke
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case PAGE_INDEX_MEMO:
                    fragment = new MemoFragment();
                    break;
                case PAGE_INDEX_PROFILE:
                    fragment = new ProfileFragment();
                    break;
                case PAGE_INDEX_PASSWORD:
                    fragment = new PasswordFragment();
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
                    pageTitle = getString(R.string.title_main_section1);
                    break;
                case PAGE_INDEX_PROFILE:
                    pageTitle = getString(R.string.title_main_section2);
                    break;
                case PAGE_INDEX_PASSWORD:
                    pageTitle = getString(R.string.title_main_section3);
                    break;
                default:
                    break;
            }
            return pageTitle;
        }
    }

    /* EditShareTextDialogListener */

    @Override
    public void onPositiveClick(String shareText) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNeutralClick() {

    }

}

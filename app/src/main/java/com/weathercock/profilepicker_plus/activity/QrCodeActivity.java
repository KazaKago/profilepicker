package com.weathercock.profilepicker_plus.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.adapter.QrCodeListAdapter;
import com.weathercock.profilepicker_plus.dao.NameDao;
import com.weathercock.profilepicker_plus.dao.ProfileDao;
import com.weathercock.profilepicker_plus.entity.NameEntity;
import com.weathercock.profilepicker_plus.entity.ProfileEntity;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;
import com.weathercock.profilepicker_plus.util.ProfileDbOpenHelper;

/**
 * QRコードを表示するActivityクラス
 * 
 * @author Kensuke
 * 
 */
public class QrCodeActivity extends Activity {

    /**
     * QRコード画像サイズ(px)
     */
    final private static int QRCODE_SIZE = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        //QRコードを取得
        String qrCodeText = getQrCodeText();

        //QRコードを表示
        ImageView qrImage = (ImageView) findViewById(R.id.image_qrcode);
        try {
            qrImage.setImageBitmap(getQrCodeByString(qrCodeText, QRCODE_SIZE));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        //名前を表示
        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            NameDao nameDao = new NameDao(db);
            NameEntity name = nameDao.select();
            TextView nameView = (TextView) findViewById(R.id.text_qr_name);
            nameView.setText(name.getLastName() + " " + name.getFirstName() + "(" + name.getReadingLastName() + " " + name.getReadingFirstName() + ")");
        } finally {
            if (db != null) db.close();
        }

        //リスト内の項目を取得(QRコードで送信できる項目のみ)
        ArrayList<ProfileListViewCell> items = new ArrayList<ProfileListViewCell>();
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_PHONE_NUMBER, getString(R.string.section_phone_number))); //電話番号
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_MAIL, getString(R.string.section_mail))); //メールアドレス
        items.addAll(getInCategoryList(ProfileEntity.CATEGORY_ADDRESS, getString(R.string.section_address))); //住所

        //QRコード内容をリストに表示
        QrCodeListAdapter adapter = new QrCodeListAdapter(this, items);
        ListView profileList = (ListView) findViewById(R.id.list_qr_content);
        profileList.setAdapter(adapter);

        //項目がないときはQRコード自体を出さずに警告メッセージだけを表示する
        if (adapter.isEmpty()) {
            LinearLayout qrLayout = (LinearLayout) findViewById(R.id.qr_layout);
            qrLayout.setVisibility(View.GONE);
        } else {
            TextView warningText = (TextView) findViewById(R.id.warning_text);
            warningText.setVisibility(View.GONE);
        }

        ImageButton closeBtn = (ImageButton) findViewById(R.id.btn_qr_close);
        closeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            ArrayList<ProfileEntity> profileList = profileDao.selectByCategoryOnlyAllowShare(category);
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
     * QRコードのBitmapを返すメソッド。Shift-JISで格納される
     * 
     * @param contents
     *            格納したい文字列
     * @param size
     *            表示させるサイズ(size×sizeで出力される)
     * @return 返されるBitmap型変数
     * @throws WriterException
     *             エラーのとき
     */
    private Bitmap getQrCodeByString(String contents, int size) throws WriterException {
        //QRコードをエンコードするクラス
        QRCodeWriter writer = new QRCodeWriter();

        //異なる型の値を入れるためgenericは使えない
        Hashtable<EncodeHintType, Object> encodeHint = new Hashtable<EncodeHintType, Object>();

        //日本語を扱うためにシフトJISを指定
        encodeHint.put(EncodeHintType.CHARACTER_SET, "shiftjis");

        //エラー修復レベルを指定
        //L 7%が復元可能
        //M 15%が復元可能
        //Q 25%が復元可能
        //H 30%が復元可能
        encodeHint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix qrCodeData = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, encodeHint);

        //QRコードのbitmap画像を作成
        Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        bitmap.eraseColor(Color.argb(255, 255, 255, 255)); //いらないかも

        for (int x = 0; x < qrCodeData.getWidth(); x++) {
            for (int y = 0; y < qrCodeData.getHeight(); y++) {
                if (qrCodeData.get(x, y) == true) {
                    //0はBlack
                    bitmap.setPixel(x, y, Color.argb(255, 0, 0, 0));
                } else {
                    //-1はWhite
                    bitmap.setPixel(x, y, Color.argb(255, 255, 255, 255));
                }
            }
        }

        return bitmap;
    }

    /**
     * 主要3キャリア対応のQRコードを返す
     * 
     * @return
     */
    private String getQrCodeText() {
        return getQrCodeTextForDocomo() + "\n\n" + getQrCodeTextForAuSoftbank();
    }

    /**
     * docomo用のQRコードを返す
     * 
     * @return
     */
    private String getQrCodeTextForDocomo() {
        String qrText = "MECARD:";

        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            NameDao nameDao = new NameDao(db);

            //名前(カンマで区切ると姓と名を分けることができるがスマホのアプリの日本語環境への対応が悪いため分けない)
            NameEntity name = nameDao.select();
            qrText = qrText + "N:" + name.getLastName() + " " + name.getFirstName() + ";";
            qrText = qrText + "SOUND:" + name.getReadingLastName() + " " + name.getReadingFirstName() + ";";

            //電話番号
            ArrayList<ProfileEntity> phoneNumList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_PHONE_NUMBER);
            for (ProfileEntity profile : phoneNumList)
                qrText = qrText + "TEL:" + profile.getContentText() + ";";

            //メールアドレス
            ArrayList<ProfileEntity> mailList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_MAIL);
            for (ProfileEntity profile : mailList)
                qrText = qrText + "EMAIL:" + profile.getContentText() + ";";

            //住所を読み込み(共有を認めている中で一番上の一つのみ読み取れるらしい)
            ArrayList<ProfileEntity> addressList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_ADDRESS);
            for (ProfileEntity profile : addressList)
                qrText = qrText + "ADR:" + profile.getContentText() + ";";

            //終了文字を挿入
            qrText = qrText + ";";
        } finally {
            if (db != null) db.close();
        }

        return qrText;
    }

    /**
     * au,Softbank用のQRコードを返す
     * 
     * @return
     */
    private String getQrCodeTextForAuSoftbank() {
        String qrText = "MEMORY:\n";

        ProfileDbOpenHelper dbHelper = new ProfileDbOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            ProfileDao profileDao = new ProfileDao(db);
            NameDao nameDao = new NameDao(db);

            //名前(カンマで区切ると姓と名を分けることができるがスマホのアプリの日本語環境への対応が悪いため分けない)
            NameEntity name = nameDao.select();
            qrText = qrText + "NAME1:" + name.getLastName() + " " + name.getFirstName() + "\n";
            qrText = qrText + "NAME2:" + name.getReadingLastName() + " " + name.getReadingFirstName() + "\n";

            //電話番号
            ArrayList<ProfileEntity> phoneNumList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_PHONE_NUMBER);
            for (int i = 0; i < phoneNumList.size(); i++) {
                ProfileEntity profile = phoneNumList.get(i);
                qrText = qrText + "TEL" + (i + 1) + ":" + profile.getContentText() + "\n";
            }

            //メールアドレス
            ArrayList<ProfileEntity> MailList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_MAIL);
            for (int i = 0; i < MailList.size(); i++) {
                ProfileEntity profile = MailList.get(i);
                qrText = qrText + "MAIL" + (i + 1) + ":" + profile.getContentText() + "\n";
            }

            //住所を読み込み(共有を認めている中で一番上の一つのみ読み取れるらしい)
            ArrayList<ProfileEntity> AddressList = profileDao.selectByCategoryOnlyAllowShare(ProfileEntity.CATEGORY_ADDRESS);
            for (int i = 0; i < AddressList.size(); i++) {
                ProfileEntity profile = AddressList.get(i);
                qrText = qrText + "ADD" + (i + 1) + ":" + profile.getContentText() + "\n";
            }

            //終了文字を挿入
            qrText = qrText + "\n";
        } finally {
            if (db != null) db.close();
        }

        return qrText;
    }

}

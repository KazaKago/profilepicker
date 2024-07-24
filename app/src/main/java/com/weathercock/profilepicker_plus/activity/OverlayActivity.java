package com.weathercock.profilepicker_plus.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.dao.FileDao;

/**
 * 画像をオーバーレイ表示するActivityクラス
 * 
 * @author Kensuke
 * 
 */
public class OverlayActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        //画像を読み込み(あれば)
        FileDao fileDao = new FileDao(this);
        File photoImage = fileDao.getFaceImage();

        ImageView faceImage = (ImageView) findViewById(R.id.image_overlay);
        if (photoImage.exists()) {
            try {
                InputStream is = getContentResolver().openInputStream(Uri.fromFile(photoImage));
                Bitmap faceImageBitmap = BitmapFactory.decodeStream(is);
                faceImage.setImageBitmap(null);
                faceImage.setImageBitmap(faceImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error_load_image), Toast.LENGTH_LONG).show();
            }
        }

        ImageButton closeBtn = (ImageButton) findViewById(R.id.btn_img_close);
        closeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}

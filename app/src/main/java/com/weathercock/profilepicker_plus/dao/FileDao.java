package com.weathercock.profilepicker_plus.dao;

import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.weathercock.profilepicker_plus.util.CommonUtil;

/**
 * 保持画像へアクセスするDAOクラス
 * 
 * @author Kensuke
 * 
 */
public class FileDao {

    /**
     * 画像ファイル名
     */
    final private static String FACE_IMAGE_NAME = "faceImage.jpg";
    /**
     * ロールバック用の一時保存用画像に付加する文字列(拡張子)
     */
    final private static String ROLLBACK_EXTENTION = ".old";

    /**
     * コンテキスト
     */
    private final Context mContext;

    public FileDao(Context context) {
        this.mContext = context;
    }

    /**
     * 画像のFileクラスを取得
     * 
     * @return
     */
    public File getFaceImage() {
        return new File(mContext.getFilesDir(), FACE_IMAGE_NAME);
    }

    /**
     * 一時保存用の画像のFileクラスを取得
     * 
     * @return
     */
    public File getTempFaceImage() {
        return new File(mContext.getExternalFilesDir(null), FACE_IMAGE_NAME);
    }

    /**
     * ロールバック用の画像のFileクラスを取得
     * 
     * @return
     */
    public File getRollbackFaceImage() {
        return new File(mContext.getFilesDir(), FACE_IMAGE_NAME + ROLLBACK_EXTENTION);
    }

    /**
     * 画像があればロールバック用に退避させる
     * 
     * @throws IOException
     */
    public void setupRollbackImage() throws IOException {
        File currentPicture = getFaceImage();
        File rollbackPicture = getRollbackFaceImage();
        if (currentPicture.exists()) CommonUtil.copyTransfer(currentPicture.getAbsolutePath(), rollbackPicture.getAbsolutePath());
    }

    /**
     * 画像のロールバック処理<br>
     * 元の画像があればロールバックを行う
     */
    public void rollbackPicture() {
        try {
            File currentPicture = getFaceImage();
            File rollbackPicture = getRollbackFaceImage();
            if (rollbackPicture.exists()) CommonUtil.copyTransfer(rollbackPicture.getAbsolutePath(), currentPicture.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ロールバック用の画像があれば削除する
     * 
     * @return
     */
    public boolean deleteRollbackPicture() {
        File rollbackPicture = getRollbackFaceImage();
        return rollbackPicture.delete();
    }

}

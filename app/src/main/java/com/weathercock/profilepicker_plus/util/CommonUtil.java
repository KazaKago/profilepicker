package com.weathercock.profilepicker_plus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;

import com.weathercock.profilepicker_plus.BuildConfig;

/**
 * ユーティリティークラス
 * 
 * @author PTAMURA
 * 
 */
public class CommonUtil {

    /**
     * デバッグモード
     */
    final private static boolean DEBUG_MODE = false;

    /**
     * Simeji関連の定数
     */
    final private static String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    /**
     * Simeji関連の定数2
     */
    final private static String REPLACE_KEY = "replace_key";

    /**
     * vold.fstabファイルのリスト
     */
    final private static String[] FSTAB_FILES = { "/system/etc/vold.fstab", "/etc/vold.fstab" };

    /**
     * int→Boolean変換
     * 
     * @param num
     * @return
     */
    public static Boolean intToBoolean(int num) {
        return (num != 0) ? true : false;
    }

    /**
     * Boolean→int変換
     * 
     * @param bool
     * @return
     */
    public static int booleanToInt(Boolean bool) {
        return (bool == true) ? 1 : 0;
    }

    /**
     * 文字列をクリップボードへ格納
     * 
     * @param context
     * @param text
     */
    public static void copyToClipboard(Context context, String text) {
        //クリップボードに格納するItemを作成
        ClipData.Item data = new ClipData.Item(text);
        //MIMETYPEの作成
        String[] mimeType = new String[1];
        mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
        //クリップボードに格納するClipDataオブジェクトの作成
        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), data);
        //クリップボードに格納
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);
    }

    /**
     * 文字列をマッシュルームへ送り、自アクティビティは終了
     * 
     * @param activity
     * @param text
     */
    public static void copyToMushroom(Activity activity, String text) {
        Intent data = new Intent();
        data.putExtra(REPLACE_KEY, text);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    /**
     * マッシュルームから呼び出されたか判定
     * 
     * @param activity
     * 
     * @return
     */
    public static boolean isMushroom(Activity activity) {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        return (action != null && ACTION_INTERCEPT.equals(action));
    }

    /**
     * デバッグモード判別<br>
     * 判定条件にリリースビルドかどうかも含めているためリリースにデバッグコードが入ることはない
     * 
     * @return
     */
    @SuppressWarnings("unused")
    public static boolean isDebugMode() {
        if (BuildConfig.DEBUG && DEBUG_MODE) return true;
        else return false;
    }

    /**
     * コピー元のパス[srcPath]から、コピー先のパス[destPath]へ ファイルのコピーを行う。 <br>
     * コピー処理にはFileChannel#transferToメソッドを利用します。<br>
     * 尚、コピー処理終了後、入力・出力のチャネルをクローズします。
     * 
     * @param srcPath
     *            コピー元のパス
     * @param destPath
     *            コピー先のパス
     * @throws IOException
     *             何らかの入出力処理例外が発生した場合
     */
    public static void copyTransfer(String srcPath, String destPath) throws IOException {

        FileInputStream is = new FileInputStream(srcPath);
        FileChannel srcChannel = is.getChannel();
        FileOutputStream os = new FileOutputStream(destPath);
        FileChannel destChannel = os.getChannel();

        srcChannel.transferTo(0, srcChannel.size(), destChannel);

        is.close();
        srcChannel.close();
        os.close();
        destChannel.close();

    }

    /**
     * 最適と思われる外部ストレージパスを取得。 <br>
     * 複数外部ストレージがある場合には先に見つかったほうを採用する。<br>
     * 取得できなければ内部ストレージを返す。
     * 
     * @return
     */
    public static String getPreferredStorageDir() {
        List<String> storageList = getStorageDirs();

        for (int i = 0; i < storageList.size(); i++) {
            if (!isMounted(storageList.get(i))) {
                storageList.remove(i--);
            }
        }

        if (storageList.size() > 1) {
            // 複数のストレージパスが存在する場合は内部ストレージパスを削除
            storageList.remove(Environment.getExternalStorageDirectory().getPath());
        } else if (storageList.isEmpty()) {
            // あり得ないが念のため内部ストレージパスを追加
            storageList.add(Environment.getExternalStorageDirectory().getPath());
        }

        return storageList.get(0);
    }

    /**
     * ストレージパスのリストを取得。
     * 
     * @return
     */
    private static List<String> getStorageDirs() {
        List<String> mountList = new ArrayList<String>();
        Scanner scanner = null;

        try {
            File fstabFile = null;

            for (int i = 0; i < FSTAB_FILES.length; i++) {
                fstabFile = new File(FSTAB_FILES[i]);

                if (fstabFile.exists()) {
                    break;
                }
            }

            scanner = new Scanner(new FileInputStream(fstabFile));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {
                    String[] args = line.replaceAll("\t", " ").split(" ");

                    if (args.length > 2) {
                        String path = args[2];

                        if (!mountList.contains(path)) {
                            mountList.add(path);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // ファイルが存在しない場合は内部ストレージは必ず存在するものとしてリストに追加
            mountList.add(Environment.getExternalStorageDirectory().getPath());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        for (int i = 0; i < mountList.size(); i++) {
            if (!isMounted(mountList.get(i))) {
                mountList.remove(i--);
            }
        }

        if (mountList.size() > 1) {
            // 外部ストレージが存在する場合は内部ストレージパスを削除
            mountList.remove(Environment.getExternalStorageDirectory().getPath());
        } else if (mountList.isEmpty()) {
            // ストレージが存在しない場合は内部ストレージパスを追加(あり得ないはずだが一応)
            mountList.add(Environment.getExternalStorageDirectory().getPath());
        }

        return mountList;
    }

    /**
     * 指定パスのマウント状態を取得
     * 
     * @param path
     * @return
     */
    private static boolean isMounted(String path) {
        boolean isMounted = false;
        Scanner scanner = null;

        try {
            scanner = new Scanner(new FileInputStream(new File("/proc/mounts")));

            while (scanner.hasNextLine()) {
                if (scanner.nextLine().contains(path)) {
                    isMounted = true;

                    break;
                }
            }
        } catch (FileNotFoundException e) {
            // 流石に存在しない場合はランタイムエラーで強制終了
            throw new RuntimeException(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return isMounted;
    }

    /**
     * .nomediaファイルをフォルダに作成
     * 
     * @param file
     * @throws IOException
     */
    public static void createNomedia(File file) throws IOException {
        File nomediaFile = new File(file.getPath(), ".nomedia");
        nomediaFile.createNewFile();
    }

    /**
     * 自身のバージョン名を取得する
     * 
     * @param context
     * @return
     * @throws NameNotFoundException
     */
    public static String getVersionName(Context context) throws NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    /**
     * 互換性を考慮した背景画像を設定メソッド
     * 
     * @param view
     * @param drawable
     * @return
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static View setBackground(View view, Drawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
        return view;
    }

}

package com.weathercock.profilepicker_plus.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell;
import com.weathercock.profilepicker_plus.entity.ProfileListViewCell.ProfileListRowMode;

/**
 * プロフィール編集リストのアダプタークラス
 * 
 * @author Kensuke
 * 
 */
public class EditProfileListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<ProfileListViewCell> mItems;

    private int mButtonIndex;
    private boolean mIsButtonEnabled;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param objects
     */
    public EditProfileListAdapter(Context context, ArrayList<ProfileListViewCell> objects) {
        mItems = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIsButtonEnabled = true;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ProfileListViewCell getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 1行操作するごとに呼ばれるメソッド ここでビューにテキストなりをセットする
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ProfileListViewCell item = mItems.get(pos);
        switch (item.getRowMode()) {
            case HEADER:
                convertView = getHeaderView(convertView, item);
                break;
            case CONTENT:
                convertView = getContentView(convertView, item);
                break;
            case NEW_ADDITION:
                convertView = getAdditionalView(convertView, item, pos, parent);
                break;
            default:
                break;
        }
        return convertView;
    }

    /**
     * ヘッダー行の生成
     * 
     * @param convertView
     * @param item
     * @return
     */
    public View getHeaderView(View convertView, ProfileListViewCell item) {
        if (convertView == null || convertView.getId() != R.layout.adapter_profile_header) convertView = mInflater.inflate(R.layout.adapter_profile_header, null);

        TextView headerText = (TextView) convertView.findViewById(R.id.text_header_profile);
        headerText.setText(item.getHeaderText());

        return convertView;
    }

    /**
     * ユーザー情報行の生成
     * 
     * @param convertView
     * @param item
     * @return
     */
    public View getContentView(View convertView, ProfileListViewCell item) {
        if (convertView == null || convertView.getId() != R.layout.adapter_profile_content) convertView = mInflater.inflate(R.layout.adapter_profile_content, null);

        TextView tagText = (TextView) convertView.findViewById(R.id.text_tag_profile);
        tagText.setText(item.getTagText());
        if (item.getTagText().length() != 0) tagText.setVisibility(View.VISIBLE);
        else tagText.setVisibility(View.GONE);
        TextView contentText = (TextView) convertView.findViewById(R.id.text_content_profile);
        contentText.setText(item.getContentText());
        ImageView shareImage = (ImageView) convertView.findViewById(R.id.image_share_profile);
        if (item.getProfileData().getAllowShare()) shareImage.setVisibility(View.VISIBLE);
        else shareImage.setVisibility(View.GONE);

        return convertView;
    }

    /**
     * 新規追加行の生成
     * 
     * @param convertView
     * @param item
     * @param pos
     * @param parent
     * @return
     */
    public View getAdditionalView(View convertView, ProfileListViewCell item, final int pos, final ViewGroup parent) {
        if (convertView == null || convertView.getId() != R.layout.adapter_profile_new) convertView = mInflater.inflate(R.layout.adapter_profile_new, null);

        Button newAdditionBtn = (Button) convertView.findViewById(R.id.btn_addition);
        if (mButtonIndex == pos) newAdditionBtn.setEnabled(mIsButtonEnabled);

        //親ListViewのonItemClickListenerへボタンイベントを転送
        newAdditionBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, pos, 0);
            }
        });

        return convertView;
    }

    /**
     * 内容の行以外はタップ無効
     */
    @Override
    public boolean isEnabled(int position) {
        if (mItems.get(position).getRowMode() == ProfileListRowMode.CONTENT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * セル内のボタンの有効無効を設定する
     * 
     * @param pos
     * @param isEnabled
     */
    public void setButtonEnabled(int pos, boolean isEnabled) {
        mButtonIndex = pos;
        mIsButtonEnabled = isEnabled;
    }

}

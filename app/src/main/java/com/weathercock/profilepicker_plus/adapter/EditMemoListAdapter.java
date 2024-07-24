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

/**
 * メモ編集リストのアダプタークラス
 * 
 * @author Kensuke
 * 
 */
public class EditMemoListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<ProfileListViewCell> mItems;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param objects
     */
    public EditMemoListAdapter(Context context, ArrayList<ProfileListViewCell> objects) {
        mItems = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
     * ユーザー情報行の生成
     * 
     * @param convertView
     * @param item
     * @return
     */
    public View getContentView(View convertView, ProfileListViewCell item) {
        if (convertView == null || convertView.getId() != R.layout.adapter_memo_content) convertView = mInflater.inflate(R.layout.adapter_memo_content, null);

        TextView titleText = (TextView) convertView.findViewById(R.id.text_title_memo);
        titleText.setText(item.getTagText());
        TextView contentText = (TextView) convertView.findViewById(R.id.text_content_memo);
        contentText.setText(item.getContentText());
        ImageView shareImage = (ImageView) convertView.findViewById(R.id.image_share_memo);
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
        if (convertView == null || convertView.getId() != R.layout.adapter_memo_new) convertView = mInflater.inflate(R.layout.adapter_memo_new, null);
        Button newAdditionBtn = (Button) convertView.findViewById(R.id.btn_addition);
        //親ListViewのonItemClickListenerへボタンイベントを転送
        newAdditionBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, pos, 0);
            }
        });

        return convertView;
    }

}

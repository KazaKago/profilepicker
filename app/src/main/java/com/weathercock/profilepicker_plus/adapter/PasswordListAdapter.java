package com.weathercock.profilepicker_plus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.entity.PasswordListViewCell;

import java.util.ArrayList;

/**
 * パスワードリストのアダプタークラス
 * 
 * @author Kensuke
 * 
 */
public class PasswordListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<PasswordListViewCell> mItems;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param objects
     */
    public PasswordListAdapter(Context context, ArrayList<PasswordListViewCell> objects) {
        mItems = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public PasswordListViewCell getItem(int position) {
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
        PasswordListViewCell item = mItems.get(pos);
        switch (item.getRowMode()) {
            case HEADER:
                convertView = getHeaderView(convertView, item);
                break;
            case CONTENT:
                convertView = getContentView(convertView, item);
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
    public View getHeaderView(View convertView, PasswordListViewCell item) {
        if (convertView == null || convertView.getId() != R.layout.adapter_password_header) convertView = mInflater.inflate(R.layout.adapter_password_header, null);

        TextView headerText = (TextView) convertView.findViewById(R.id.text_header_password);
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
    public View getContentView(View convertView, PasswordListViewCell item) {
        if (convertView == null || convertView.getId() != R.layout.adapter_password_content) convertView = mInflater.inflate(R.layout.adapter_password_content, null);

        TextView tagText = (TextView) convertView.findViewById(R.id.text_tag_password);
        tagText.setText(item.getTagText());
        if (item.getTagText().length() != 0) tagText.setVisibility(View.VISIBLE);
        else tagText.setVisibility(View.GONE);
        TextView contentText = (TextView) convertView.findViewById(R.id.text_content_password);
        contentText.setText(item.getContentText());

        return convertView;
    }

}

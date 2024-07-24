package com.weathercock.profilepicker_plus.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.weathercock.profilepicker_plus.R;
import com.weathercock.profilepicker_plus.entity.MemoListViewCell;

/**
 * メモリストを表示するリストアダプター
 * 
 * @author Kensuke
 * 
 */
public class MemoListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<MemoListViewCell> mItems;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param objects
     */
    public MemoListAdapter(Context context, ArrayList<MemoListViewCell> objects) {
        mItems = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MemoListViewCell getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = mInflater.inflate(R.layout.adapter_memo_content, null);

        MemoListViewCell item = mItems.get(pos);
        TextView titleText = (TextView) convertView.findViewById(R.id.text_title_memo);
        titleText.setText(item.getTitleText());
        TextView contentText = (TextView) convertView.findViewById(R.id.text_content_memo);
        contentText.setText(item.getContentText());
        ImageView shareImage = (ImageView) convertView.findViewById(R.id.image_share_memo);
        shareImage.setVisibility(View.GONE);

        return convertView;
    }

}

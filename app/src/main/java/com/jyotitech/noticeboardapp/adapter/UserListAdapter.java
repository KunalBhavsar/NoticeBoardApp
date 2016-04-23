package com.jyotitech.noticeboardapp.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adityagohad on 24/4/16.
 */
public class UserListAdapter extends BaseAdapter {
    List<User> userList;

    public UserListAdapter() {
        userList = new ArrayList<>();
    }

    public void addDataSource(List<User> userList) {
        this.userList.clear();
        this.userList.addAll(userList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        return convertView;
    }

    class ViewHolder {
        TextView txtUserName;
        CheckBox chkbox;
    }
}

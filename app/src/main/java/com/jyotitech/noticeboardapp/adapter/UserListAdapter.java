package com.jyotitech.noticeboardapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.ui.NoticeBoardListActivity;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KunalBhavsar on 24/4/16.
 */
public class UserListAdapter extends BaseAdapter {
    List<UserMember> userList;
    List<UserMember> selectedUserList;
    Context context;
    LayoutInflater layoutInflater;

    public UserListAdapter(Context context) {
        userList = new ArrayList<>();
        selectedUserList = new ArrayList<>();
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void addDataSource(List<UserMember> userList) {
        this.userList.clear();
        this.selectedUserList.clear();
        this.userList.addAll(userList);
        notifyDataSetChanged();
    }

    public void clearSelectedList() {
        this.selectedUserList.clear();
        notifyDataSetChanged();
    }

    public List<UserMember> getSelectedUserMembersList() {
        return selectedUserList;
    }

    public boolean isAllSelected() {
        return !userList.isEmpty() && selectedUserList.size() == userList.size();
    }

    public void setAllSelection(boolean setAllSelected) {
        if (setAllSelected) {
            this.selectedUserList.clear();
            this.selectedUserList.addAll(userList);
        } else {
            this.selectedUserList.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public UserMember getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserMember user = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_user_list, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtUserName.setText(user.getFullname());
        boolean selectedListContainUser = selectedUserList.contains(user);
        viewHolder.chkbox.setChecked(selectedListContainUser);
        viewHolder.chkbox.setTag(user);
        viewHolder.chkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserMember clickedUser = (UserMember) v.getTag();
                if (selectedUserList.contains(clickedUser)) {
                    clickedUser.setPermissions(KeyConstants.PERMISSION_READ);
                    selectedUserList.remove(clickedUser);
                } else {
                    selectedUserList.add(clickedUser);
                }
                ((NoticeBoardListActivity) context).setAllSelectedCheckbox();
                notifyDataSetChanged();
            }
        });

        viewHolder.relPermissions.setVisibility(selectedListContainUser ? View.VISIBLE : View.GONE);
        viewHolder.switchPermission.setChecked(user.getPermissions().equals(KeyConstants.PERMISSION_WRITE));
        viewHolder.switchPermission.setTag(user);
        viewHolder.switchPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch switchBox = (Switch)v;
                UserMember clickedUser = (UserMember) v.getTag();
                if(switchBox.isChecked()) {
                    clickedUser.setPermissions(KeyConstants.PERMISSION_WRITE);
                }
                else {
                    clickedUser.setPermissions(KeyConstants.PERMISSION_READ);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView txtUserName;
        CheckBox chkbox;
        RelativeLayout relPermissions;
        Switch switchPermission;

        public ViewHolder(View view) {
            txtUserName = (TextView) view.findViewById(R.id.txt_name);
            chkbox = (CheckBox) view.findViewById(R.id.checkbox);
            relPermissions = (RelativeLayout) view.findViewById(R.id.rel_permissions);
            switchPermission = (Switch) view.findViewById(R.id.switch_permission);
        }
    }
}

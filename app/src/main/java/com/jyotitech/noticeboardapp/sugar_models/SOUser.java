package com.jyotitech.noticeboardapp.sugar_models;

import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SOUser extends SugarRecord {

    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_ID = "user_id";
    public static final String COLUMN_IS_APP_OWNER = "app_owner";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    private String email;
    private String fullname;
    private long userId;
    private String mobile;
    private String password;
    private String username;
    private String key;
    private boolean appOwner;

    public SOUser() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isAppOwner() {
        return appOwner;
    }

    public void setAppOwner(boolean appOwner) {
        this.appOwner = appOwner;
    }

    public static SOUser findByKey(String key) {
        List<SOUser> list = find(SOUser.class, COLUMN_KEY + "=?", new String[]{key}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static SOUser findByUserId(long id) {
        List<SOUser> list = find(SOUser.class, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static SOUser getAppOwner() {
        List<SOUser> list = find(SOUser.class, COLUMN_ID + "=?",
                new String[]{String.valueOf(AppPreferences.getInstance().getAppOwnerId())}, null, null, "1");
        if(list.isEmpty()) return null;
        return list.get(0);
    }
}

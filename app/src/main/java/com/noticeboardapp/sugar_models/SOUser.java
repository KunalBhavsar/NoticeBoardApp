package com.noticeboardapp.sugar_models;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SOUser extends SugarRecord {

    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_IS_APP_OWNER = "APP_OWNER";

    private String email;
    private String fullname;
    private long userId;
    private String mobile;
    private String password;
    private String username;
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

    public boolean isAppOwner() {
        return appOwner;
    }

    public void setAppOwner(boolean appOwner) {
        this.appOwner = appOwner;
    }

    public static SOUser findByUserId(long userId) {
        List<SOUser> list = find(SOUser.class, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static SOUser getAppOwner() {
        List<SOUser> list = find(SOUser.class, COLUMN_IS_APP_OWNER + "=?",
                new String[]{String.valueOf(1)}, null, null, "1");
        if(list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SOUser soUser = (SOUser) o;

        return userId == soUser.userId;

    }

    @Override
    public int hashCode() {
        return (int) (userId ^ (userId >>> 32));
    }
}

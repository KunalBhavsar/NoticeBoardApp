package com.jyotitech.noticeboardapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class AppPreferences {
    private static AppPreferences ourInstance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static final String SPREF_KEY_APP_OWNER_ID = "app_onwer_id";
    public static final String SPREF_KEY_FOR_USERNAME = "username";
    public static final String SPREF_KEY_FOR_PASSWORD = "password";
    public static final String SPREF_KEY_FOR_EMAIL = "email";
    public static final String SPREF_KEY_FOR_MOBILE_NUMBER = "mobile_number";
    public static final String SPREF_KEY_FOR_FULL_NAME = "fullname";
    public static final String SPREF_KEY_FOR_USER_LOGGED_IN = "logged_in";
    public static final String SPREF_NAME = "notice_board_spref";

    private AppPreferences() {
    }

    public static void init(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppPreferences();
            sharedPreferences = context.getSharedPreferences(SPREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    public static AppPreferences getInstance() {
        return ourInstance;
    }

    public long getAppOwnerId() {
        return sharedPreferences.getLong(SPREF_KEY_APP_OWNER_ID, -1);
    }

    public void setAppOwnerId(long deviceId) {
        editor.putLong(SPREF_KEY_APP_OWNER_ID, deviceId);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(SPREF_KEY_FOR_USER_LOGGED_IN, false);
    }

    public void setIsLoggedIn(boolean userLoggedIn) {
        editor.putBoolean(SPREF_KEY_FOR_USER_LOGGED_IN, userLoggedIn);
        editor.apply();
    }

    public void clear() {
        editor.clear().commit();
    }
}

package com.jyotitech.noticeboardapp.utils;

import com.jyotitech.noticeboardapp.NoticeBoardApplication;

/**
 * Created by kiran on 20-Apr-16.
 */
public class KeyConstants {
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String FIREBASE_BASE_URL = "https://notice-board-app.firebaseio.com/";
    public static final String FIREBASE_RESOURCE_NOTICEBOARD = FIREBASE_BASE_URL + "noticeBoards";
    public static final String FIREBASE_RESOURCE_USER = FIREBASE_BASE_URL + "users";
    public static final String EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY = "extra_from_noticeboard_list_to_notice_list_activity";

    public static final String SPREF_KEY_APP_OWNER_ID = "app_onwer_id";
    public static final String SPREF_KEY_FOR_USERNAME = "username";
    public static final String SPREF_KEY_FOR_PASSWORD = "password";
    public static final String SPREF_KEY_FOR_EMAIL = "email";
    public static final String SPREF_KEY_FOR_MOBILE_NUMBER = "mobile_number";
    public static final String SPREF_KEY_FOR_FULL_NAME = "fullname";
    public static final String SPREF_KEY_FOR_USER_LOGGED_IN = "logged_in";
    public static final String SPREF_NAME = "notice_board_spref";

}

package com.jyotitech.noticeboardapp.utils;

/**
 * Created by kiran on 20-Apr-16.
 */
public class KeyConstants {
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String FIREBASE_BASE_URL = "https://notice-board-app.firebaseio.com/";
    public static final String FIREBASE_KEY_NOTICEBOARD = "noticeBoards";
    public static final String FIREBASE_KEY_USER = "users";
    public static final String FIREBASE_KEY_NOTICE = "notices";
    public static final String FIREBASE_RESOURCE_NOTICEBOARD = FIREBASE_BASE_URL + FIREBASE_KEY_NOTICEBOARD;
    public static final String FIREBASE_RESOURCE_USER = FIREBASE_BASE_URL + FIREBASE_KEY_USER;
    public static final String FIREBASE_RESOURCE_NOTICE = FIREBASE_BASE_URL + FIREBASE_KEY_NOTICE;
    public static final String EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY = "extra_from_noticeboard_list_to_notice_list_activity";

    public static final String OUTDATED_RESOURCE_USER = "user";
    public static final String OUTDATED_RESOURCE_NOTICE_BOARD = "notice_board";
    public static final String OUTDATED_RESOURCE_NOTICE = "notice";
}

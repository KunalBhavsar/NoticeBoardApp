package com.noticeboardapp.utils;

/**
 * Created by Pinky Walve on 20-Apr-16.
 */
public class KeyConstants {
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String FIREBASE_BASE_URL_FOR_HEAVY_DATA = "https://notice-board-app.firebaseio.com/heavy_data/";
    public static final String FIREBASE_BASE_URL_FOR_LIGHT_DATA = "https://notice-board-app.firebaseio.com/light_data/";
    public static final String FIREBASE_KEY_NOTICEBOARD = "noticeBoards";
    public static final String FIREBASE_KEY_USER = "users";
    public static final String FIREBASE_KEY_NOTICE = "notices";
    public static final String FIREBASE_KEY_MEDIAS = "medias";
    public static final String FIREBASE_PATH_NOTICEBOARD = FIREBASE_BASE_URL_FOR_LIGHT_DATA + FIREBASE_KEY_NOTICEBOARD;
    public static final String FIREBASE_PATH_USER = FIREBASE_BASE_URL_FOR_LIGHT_DATA + FIREBASE_KEY_USER;
    public static final String FIREBASE_PATH_NOTICE = FIREBASE_BASE_URL_FOR_LIGHT_DATA + FIREBASE_KEY_NOTICE;
    public static final String FIREBASE_PATH_MEDIA = FIREBASE_BASE_URL_FOR_HEAVY_DATA + FIREBASE_KEY_MEDIAS;
    public static final String EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY = "extra_from_noticeboard_list_to_notice_list_activity";
    public static final String EXTRA_FROM_NOTICE_LIST_TO_NOTICE_DETAIL_ACTIVITY = "extra_from_notice_list_to_notice_detail_activity";

    public static final String OUTDATED_RESOURCE_USER = "user";
    public static final String OUTDATED_RESOURCE_NOTICE_BOARD = "notice_board";
    public static final String OUTDATED_RESOURCE_NOTICE = "notice";

    public static final String EXTRA_FROM_NOTICE_LIST_TO_IMAGE_VIEW_ACTIVITY = "attachment_id";
    public static final String EXTRA_FROM_NOTICE_BOARD_LIST_TO_REGISTER_ACTIVITY = "edit_user_profile";

    public static final String MEDIA_TYPE_IMAGE = "Image";
    public static final String MEDIA_TYPE_PDF = "PDF";
}

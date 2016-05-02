package com.noticeboardapp.sugar_models;

import com.orm.SugarRecord;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SOUserMember extends SugarRecord {
    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_NOTICE_BOARD_ID = "NOTICE_BOARD_ID";
    public static final String COLUMN_PERMISSIONS = "PERMISSIONS";

    private String permissions;
    private long noticeBoardId;
    private long userId;

    public SOUserMember() {
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public long getNoticeBoardId() {
        return noticeBoardId;
    }

    public void setNoticeBoardId(long noticeBoardId) {
        this.noticeBoardId = noticeBoardId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public static void deleteUserMembersRelatedToNoticeBoard(long noticeBoardId) {
        deleteAll(SOUserMember.class, COLUMN_NOTICE_BOARD_ID + "=?", String.valueOf(noticeBoardId));
    }
}

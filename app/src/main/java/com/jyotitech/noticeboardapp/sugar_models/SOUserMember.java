package com.jyotitech.noticeboardapp.sugar_models;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SOUserMember extends SugarRecord {

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

    public static SOUserMember findByUserIdAndNoticeBoardId(long userId, long noticeBoardId) {
        List<SOUserMember> list = find(SOUserMember.class, "notice_board_id=? AND user_id=?",
                new String[]{String.valueOf(noticeBoardId), String.valueOf(userId)}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }
}

package com.jyotitech.noticeboardapp.sugar_models;

import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SONoticeBoard extends SugarRecord {

    private long noticeBoardId;
    private String title;
    private long lastModifiedAt;
    private long lastVisitedAt;
    private String key;

    public SONoticeBoard() {
    }

    public long getNoticeBoardId() {
        return noticeBoardId;
    }

    public void setNoticeBoardId(long noticeBoardId) {
        this.noticeBoardId = noticeBoardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public long getLastVisitedAt() {
        return lastVisitedAt;
    }

    public void setLastVisitedAt(long lastVisitedAt) {
        this.lastVisitedAt = lastVisitedAt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static SONoticeBoard findByKey(String key) {
        List<SONoticeBoard> list = find(SONoticeBoard.class, "key=?", new String[]{key}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static SONoticeBoard findNoticeBoardById(long id) {
        List<SONoticeBoard> list = find(SONoticeBoard.class, "notice_board_id=?", new String[]{String.valueOf(id)}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public List<SOUserMember> getUserMembers() {
        return find(SOUserMember.class, "notice_board_id=?", new String[]{String.valueOf(noticeBoardId)}, null, null, null);
    }

    public boolean isAppOwnerIsOwnerOfNoticeBoard() {
        return count(SOUserMember.class, "notice_board_id=? AND user_id=? AND permissions=?",
                new String[]{
                    String.valueOf(noticeBoardId),
                    String.valueOf(AppPreferences.getInstance().getAppOwnerId()),
                        KeyConstants.PERMISSION_WRITE}) > 0;
    }
}

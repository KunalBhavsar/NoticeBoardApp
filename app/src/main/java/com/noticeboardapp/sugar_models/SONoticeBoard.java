package com.noticeboardapp.sugar_models;

import com.noticeboardapp.utils.AppPreferences;
import com.noticeboardapp.utils.KeyConstants;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SONoticeBoard extends SugarRecord {

    public static final String COLUMN_NOTICE_BOARD_ID = "NOTICE_BOARD_ID";

    private long noticeBoardId;
    private String title;
    private long lastModifiedAt;
    private long lastVisitedAt;

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

    public static SONoticeBoard findNoticeBoardById(long noticeBoardId) {
        List<SONoticeBoard> list = find(SONoticeBoard.class, COLUMN_NOTICE_BOARD_ID + "=?", new String[]{String.valueOf(noticeBoardId)}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public List<SOUserMember> getUserMembers() {
        return find(SOUserMember.class, SOUserMember.COLUMN_NOTICE_BOARD_ID + "=?", new String[]{String.valueOf(noticeBoardId)}, null, null, null);
    }

    public boolean isAppOwnerIsOwnerOfNoticeBoard() {
        return count(SOUserMember.class, SOUserMember.COLUMN_NOTICE_BOARD_ID + "=? AND "
                + SOUserMember.COLUMN_USER_ID + "=? AND " + SOUserMember.COLUMN_PERMISSIONS + "=?",
                new String[]{
                    String.valueOf(noticeBoardId),
                    String.valueOf(AppPreferences.getInstance().getAppOwnerId()),
                        KeyConstants.PERMISSION_WRITE}) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SONoticeBoard that = (SONoticeBoard) o;

        return noticeBoardId == that.noticeBoardId;

    }

    @Override
    public int hashCode() {
        return (int) (noticeBoardId ^ (noticeBoardId >>> 32));
    }
}

package com.jyotitech.noticeboardapp.sugar_models;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class SONotice extends SugarRecord {
    private long noticeId;
    private String title;
    private String description;
    private String attachments;
    private long owner;
    private long createdAt;
    private long noticeBoardId;
    private String key;

    public SONotice() {
    }

    public long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(long noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getNoticeBoardId() {
        return noticeBoardId;
    }

    public void setNoticeBoardId(long noticeBoardId) {
        this.noticeBoardId = noticeBoardId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static SONotice findByKey(String key) {
        List<SONotice> list = find(SONotice.class, "key=?", new String[]{key}, null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static SONotice findById(long noticeId) {
        List<SONotice> list = find(SONotice.class, "notice_id=?", new String[]{String.valueOf(noticeId)},
                null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }
}

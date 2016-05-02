package com.noticeboardapp.sugar_models;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Pinky Walve on 1/5/16.
 */
public class SOAttachment extends SugarRecord {

    public static final String COLUMN_ATTACHMENT_ID = "ATTACHMENT_ID";

    private long attachmentId;
    private String attachmentType;
    private String localFilePath;
    private long noticeId;
    private String data;
    private boolean isDownloaded;
    private String name;

    public SOAttachment() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(long noticeId) {
        this.noticeId = noticeId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public static SOAttachment findByAttachmentId(long attachmentId) {
        List<SOAttachment> list = find(SOAttachment.class, COLUMN_ATTACHMENT_ID + "=?", new String[]{String.valueOf(attachmentId)},
                null, null, "1");
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SOAttachment that = (SOAttachment) o;

        return attachmentId == that.attachmentId;

    }

    @Override
    public int hashCode() {
        return (int) (attachmentId ^ (attachmentId >>> 32));
    }
}

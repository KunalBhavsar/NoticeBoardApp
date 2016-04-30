package com.jyotitech.noticeboardapp.model;

import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class Notice {

    public static final String CHILD_ID = "id";
    public static final String CHILD_TITLE = "title";
    public static final String CHILD_DESCRIPTION = "description";
    public static final String CHILD_ATTACHMENT = "attachments";
    public static final String CHILD_OWNER = "owner";
    public static final String CHILD_CREATED_AT = "createdAt";
    public static final String CHILD_NOTICE_BOARD_ID = "noticeBoardId";

    private Long id;
    private String title;
    private String description;
    private String attachments;
    private UserMember owner;
    private Long createdAt;
    private Long noticeBoardId;
    public Notice() {
    }

    public Notice(String title, String description, UserMember owner, String attachments) {
        this.title = title;
        this.description = description;
        this.attachments = attachments;
        this.owner = owner;
    }

    public long getNoticeBoardId() {
        return noticeBoardId;
    }

    public void setNoticeBoardId(long noticeBoardId) {
        this.noticeBoardId = noticeBoardId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public UserMember getOwner() {
        return owner;
    }

    public void setOwner(UserMember owner) {
        this.owner = owner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}


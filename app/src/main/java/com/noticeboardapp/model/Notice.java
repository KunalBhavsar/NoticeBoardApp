package com.noticeboardapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pinky Walve on 20-Apr-16.
 */
public class Notice {
    private Long id;
    private String title;
    private String description;
    private List<MediaMini> attachments;
    private UserMember owner;
    private Long createdAt;
    private Long noticeBoardId;

    public Notice() {
        attachments = new ArrayList<>();
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

    public List<MediaMini> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MediaMini> attachments) {
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


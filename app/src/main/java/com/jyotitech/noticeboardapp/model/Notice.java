package com.jyotitech.noticeboardapp.model;

import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class Notice {
    private long id;
    private String title;
    private String description;
    private List<String> attachments;
    private UserMember owner;
    private long createdAt;
    public Notice() {
    }

    public Notice(String title, String description, List<String> attachments, UserMember owner) {
        this.title = title;
        this.description = description;
        this.attachments = attachments;
        this.owner = owner;
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

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
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


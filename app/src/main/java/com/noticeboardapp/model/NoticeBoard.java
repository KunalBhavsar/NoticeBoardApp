package com.noticeboardapp.model;

import java.util.List;

/**
 * Created by Pinky Walve on 20-Apr-16.
 */
public class NoticeBoard {

    private long id;
    private String title;
    private List<UserMember> members;
    private long lastModifiedAt;

    public NoticeBoard() {
    }

    public NoticeBoard(String title, List<UserMember> members) {
        this.title = title;
        this.members = members;
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

    public List<UserMember> getMembers() {
        return members;
    }

    public void setMembers(List<UserMember> members) {
        this.members = members;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}

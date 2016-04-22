package com.jyotitech.noticeboardapp.model;

/**
 * Created by kiran on 20-Apr-16.
 */
public class UserMember {
    private long id;
    private String permissions;
    private String fullname;

    public UserMember() {
    }

    public UserMember(String permissions, String fullname) {
        this.permissions = permissions;
        this.fullname = fullname;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserMember that = (UserMember) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}

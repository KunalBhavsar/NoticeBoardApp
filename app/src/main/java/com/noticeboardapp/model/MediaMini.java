package com.noticeboardapp.model;

/**
 * Created by Pinky Walve on 1/5/16.
 */
public class MediaMini {
    private long id;
    private String mediaType;

    public MediaMini() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}

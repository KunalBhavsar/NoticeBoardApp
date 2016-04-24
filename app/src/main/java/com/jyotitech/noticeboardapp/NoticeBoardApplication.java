package com.jyotitech.noticeboardapp;

import android.app.Application;

import com.firebase.client.Firebase;
import com.jyotitech.noticeboardapp.model.NoticeBoard;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeBoardApplication extends Application {

    private NoticeBoard transientNoticeBoard;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

    public NoticeBoard getTransientNoticeBoard() {
        return transientNoticeBoard;
    }

    public void setTransientNoticeBoard(NoticeBoard transientNoticeBoard) {
        this.transientNoticeBoard = transientNoticeBoard;
    }
}

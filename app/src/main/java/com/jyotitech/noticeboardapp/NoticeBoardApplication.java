package com.jyotitech.noticeboardapp;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeBoardApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

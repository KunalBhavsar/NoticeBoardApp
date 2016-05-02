package com.noticeboardapp.ui;

import android.support.v7.app.AppCompatActivity;

import com.noticeboardapp.NoticeBoardApplication;
import com.noticeboardapp.interfaces.OutdatedResourceSubscriber;

/**
 * Created by Pinky Walve on 30/4/16.
 */
public class OutdatedResourceSubscriberActivity extends AppCompatActivity implements OutdatedResourceSubscriber {
    @Override
    public void onDatasetChanged(String dataset) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NoticeBoardApplication)getApplication()).attach(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((NoticeBoardApplication)getApplication()).detach(this);
    }
}

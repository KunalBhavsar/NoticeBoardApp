package com.jyotitech.noticeboardapp.interfaces;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public interface OutdatedResourceObserver {
    void attach(OutdatedResourceSubscriber subscriber);
    void detach(OutdatedResourceSubscriber subscriber);
    void notifyDatasetChanged(String dataset);
}

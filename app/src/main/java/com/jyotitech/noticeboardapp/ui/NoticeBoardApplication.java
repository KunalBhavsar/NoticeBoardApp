package com.jyotitech.noticeboardapp.ui;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.interfaces.OutdatedResourceObserver;
import com.jyotitech.noticeboardapp.interfaces.OutdatedResourceSubscriber;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.User;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.sugar_models.SONotice;
import com.jyotitech.noticeboardapp.sugar_models.SONoticeBoard;
import com.jyotitech.noticeboardapp.sugar_models.SOUser;
import com.jyotitech.noticeboardapp.sugar_models.SOUserMember;
import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NotificationHandler;
import com.orm.SugarApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pinky Walve on 20-Apr-16.
 */
public class NoticeBoardApplication extends SugarApp implements OutdatedResourceObserver {
    private static final String TAG = NoticeBoardApplication.class.getSimpleName();

    private NoticeBoard transientNoticeBoard;
    private List<OutdatedResourceSubscriber> subscriberList;
    private ValueEventListener valueEventListener;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        AppPreferences.init(this);
        NotificationHandler.init(this);
        subscriberList = new ArrayList<>();
        if (AppPreferences.getInstance().isLoggedIn())
            listenForDataChanges();
    }

    public NoticeBoard getTransientNoticeBoard() {
        return transientNoticeBoard;
    }

    public void setTransientNoticeBoard(NoticeBoard transientNoticeBoard) {
        this.transientNoticeBoard = transientNoticeBoard;
    }

    public void removeGlobalDataListner() {
        new Firebase(KeyConstants.FIREBASE_BASE_URL).removeEventListener(valueEventListener);
    }
    public void listenForDataChanges() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (AppPreferences.getInstance().isLoggedIn()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        switch (key) {
                            case KeyConstants.FIREBASE_KEY_USER:
                                processUsers(snapshot);
                                break;
                            case KeyConstants.FIREBASE_KEY_NOTICEBOARD:
                                processNoticeBoards(snapshot);
                                break;
                            case KeyConstants.FIREBASE_KEY_NOTICE:
                                processNotices(snapshot);
                                break;
                            default:
                                Log.e(TAG, "New key resource found " + key);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Error while loading users : " + firebaseError.getMessage());
            }
        };

        new Firebase(KeyConstants.FIREBASE_BASE_URL).addValueEventListener(valueEventListener);
    }

    public void processUsers(DataSnapshot snapshot) {
        if(AppPreferences.getInstance().isLoggedIn()) {
            if (snapshot != null) {
                SOUser soUser;
                boolean newUser;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String key = userSnapshot.getKey();
                    soUser = SOUser.findByKey(key);
                    if (soUser == null) {
                        soUser = new SOUser();
                        newUser = true;
                    } else {
                        newUser = false;
                    }
                    soUser.setKey(key);
                    soUser.setUserId((long) userSnapshot.child(User.CHILD_ID).getValue());
                    soUser.setAppOwner(((long) userSnapshot.child(User.CHILD_ID).getValue())
                            == AppPreferences.getInstance().getAppOwnerId());
                    soUser.setEmail((String) userSnapshot.child(User.CHILD_EMAIL).getValue());
                    soUser.setFullname((String) userSnapshot.child(User.CHILD_FULLNAME).getValue());
                    soUser.setEmail((String) userSnapshot.child(User.CHILD_EMAIL).getValue());
                    soUser.setMobile((String) userSnapshot.child(User.CHILD_MOBILE).getValue());
                    soUser.setUsername((String) userSnapshot.child(User.CHILD_USERNAME).getValue());

                    if (newUser)
                        soUser.save();
                    else
                        soUser.update();
                }
                notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_USER);
            } else {
                Log.e(TAG, "Snapshot notice board entity is null..");
            }
        }
        else {
            Log.e(TAG, "User is not logged in, cant process user snapshot..");
        }
    }

    public void processNoticeBoards(DataSnapshot snapshot) {
        if(AppPreferences.getInstance().isLoggedIn()) {
            if (snapshot != null) {
                SONoticeBoard soNoticeBoard;
                SOUserMember soUserMember;
                boolean noticeBoardRelatedToAppOwner;
                boolean newNoticeBoard;
                for (DataSnapshot noticeBoardSnapshot : snapshot.getChildren()) {
                    noticeBoardRelatedToAppOwner = false;
                    for (DataSnapshot membersSnapshot : noticeBoardSnapshot.child(NoticeBoard.CHILD_MEMBERS).getChildren()) {
                        if ((long) membersSnapshot.child(UserMember.CHILD_ID).getValue() == AppPreferences.getInstance().getAppOwnerId()) {
                            noticeBoardRelatedToAppOwner = true;
                            break;
                        }
                    }
                    if (noticeBoardRelatedToAppOwner) {
                        String key = noticeBoardSnapshot.getKey();
                        soNoticeBoard = SONoticeBoard.findByKey(key);
                        if (soNoticeBoard == null) {
                            soNoticeBoard = new SONoticeBoard();
                            soNoticeBoard.setLastVisitedAt(System.currentTimeMillis());
                            newNoticeBoard = true;
                        } else {
                            newNoticeBoard = false;
                        }
                        soNoticeBoard.setKey(key);
                        soNoticeBoard.setNoticeBoardId((long) noticeBoardSnapshot.child(NoticeBoard.CHILD_ID).getValue());
                        soNoticeBoard.setTitle((String) noticeBoardSnapshot.child(NoticeBoard.CHILD_TITLE).getValue());
                        soNoticeBoard.setLastModifiedAt((long) noticeBoardSnapshot.child(NoticeBoard.CHILD_LAST_NOTIFIED_AT).getValue());
                        for (DataSnapshot membersSnapshot : noticeBoardSnapshot.child(NoticeBoard.CHILD_MEMBERS).getChildren()) {
                            soUserMember = SOUserMember.findByUserIdAndNoticeBoardId((long) membersSnapshot.child(UserMember.CHILD_ID).getValue(), soNoticeBoard.getNoticeBoardId());
                            if (soUserMember == null) {
                                soUserMember = new SOUserMember();
                                soUserMember.setNoticeBoardId(soNoticeBoard.getNoticeBoardId());
                                soUserMember.setUserId((long) membersSnapshot.child(UserMember.CHILD_ID).getValue());
                            }
                            soUserMember.setPermissions((String) membersSnapshot.child(UserMember.CHILD_PERMISSION).getValue());
                            soUserMember.save();
                        }
                        if (newNoticeBoard)
                            soNoticeBoard.save();
                        else
                            soNoticeBoard.update();

                        if (subscriberList.isEmpty()) {
                            NotificationHandler.getInstance().showNotification("New notice board added!!", soNoticeBoard.getTitle());
                        }
                    }
                }
                notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_NOTICE_BOARD);
            }
            else {
                Log.e(TAG, "Snapshot notice board entity is null..");
            }
        }
        else {
            Log.e(TAG, "User is not logged in, cant process notice board snapshot..");
        }
    }


    public void processNotices(DataSnapshot snapshot) {
        if(AppPreferences.getInstance().isLoggedIn()) {
            if (snapshot != null) {
                SONotice soNotice;
                boolean newNotice;

                for (DataSnapshot noticeSnapshot : snapshot.getChildren()) {
                    if (SONoticeBoard.findNoticeBoardById((long) noticeSnapshot.child(Notice.CHILD_NOTICE_BOARD_ID).getValue()) == null) {
                        continue;
                    }
                    String key = noticeSnapshot.getKey();
                    soNotice = SONotice.findByKey(key);
                    if (soNotice == null) {
                        soNotice = new SONotice();
                        newNotice = true;
                    } else {
                        newNotice = false;
                    }
                    soNotice.setKey(key);
                    soNotice.setNoticeId((long) noticeSnapshot.child(Notice.CHILD_ID).getValue());
                    soNotice.setCreatedAt((long) noticeSnapshot.child(Notice.CHILD_CREATED_AT).getValue());
                    soNotice.setTitle((String) noticeSnapshot.child(Notice.CHILD_TITLE).getValue());
                    soNotice.setDescription((String) noticeSnapshot.child(Notice.CHILD_DESCRIPTION).getValue());
                    DataSnapshot ownerSnapshot = noticeSnapshot.child(Notice.CHILD_OWNER);
                    soNotice.setOwner((long) ownerSnapshot.child(UserMember.CHILD_ID).getValue());
                    soNotice.setNoticeBoardId((long) noticeSnapshot.child(Notice.CHILD_NOTICE_BOARD_ID).getValue());

                    if (newNotice)
                        soNotice.save();
                    else
                        soNotice.update();

                    if (subscriberList.isEmpty()) {
                        SONoticeBoard soNoticeBoard = SONoticeBoard.findNoticeBoardById(soNotice.getNoticeBoardId());
                        if (soNoticeBoard != null) {
                            NotificationHandler.getInstance().showNotification("New notice in " + soNoticeBoard.getTitle() + "!!",
                                    soNotice.getTitle());
                        }
                    }
                }
                notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_NOTICE);
            }
            else {
                Log.e(TAG, "Snapshot notice entity is null..");
            }
        }
        else {
            Log.e(TAG, "User is not logged in, cant process notice snapshot..");
        }
    }

    @Override
    public void attach(OutdatedResourceSubscriber subscriber) {
        subscriberList.add(subscriber);
    }

    @Override
    public void detach(OutdatedResourceSubscriber subscriber) {
        subscriberList.remove(subscriber);
    }

    @Override
    public void notifyDatasetChanged(String dataset) {
        for (OutdatedResourceSubscriber subscriber : subscriberList) {
            subscriber.onDatasetChanged(dataset);
        }
    }
}

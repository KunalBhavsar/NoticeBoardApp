package com.noticeboardapp;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.noticeboardapp.interfaces.OutdatedResourceObserver;
import com.noticeboardapp.interfaces.OutdatedResourceSubscriber;
import com.noticeboardapp.model.Notice;
import com.noticeboardapp.model.NoticeBoard;
import com.noticeboardapp.model.User;
import com.noticeboardapp.model.UserMember;
import com.noticeboardapp.sugar_models.SONotice;
import com.noticeboardapp.sugar_models.SONoticeBoard;
import com.noticeboardapp.sugar_models.SOUser;
import com.noticeboardapp.sugar_models.SOUserMember;
import com.noticeboardapp.utils.AppPreferences;
import com.noticeboardapp.utils.FileUtils;
import com.noticeboardapp.utils.KeyConstants;
import com.noticeboardapp.utils.NotificationHandler;
import com.orm.SugarApp;
import com.orm.SugarTransactionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pinky Walve on 20-Apr-16.
 */
public class NoticeBoardApplication extends SugarApp implements OutdatedResourceObserver {
    private static final String TAG = NoticeBoardApplication.class.getSimpleName();

    private List<OutdatedResourceSubscriber> subscriberList;
    private ValueEventListener valueEventListener;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        AppPreferences.init(this);
        NotificationHandler.init(this);
        FileUtils.init(this);

        subscriberList = new ArrayList<>();

        if (AppPreferences.getInstance().isLoggedIn())
            listenForDataChanges();
    }

    public void removeGlobalDataListner() {
        new Firebase(KeyConstants.FIREBASE_BASE_URL).removeEventListener(valueEventListener);
    }

    public void listenForDataChanges() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Received snapshot, now processing it");
                processDataSnapshotInTransaction(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "Error while loading users : " + firebaseError.getMessage());
            }
        };

        Firebase firebase = new Firebase(KeyConstants.FIREBASE_BASE_URL);
        firebase.addListenerForSingleValueEvent(valueEventListener);
        firebase.addValueEventListener(valueEventListener);
    }

    public synchronized void processDataSnapshotInTransaction(final DataSnapshot snapshot) {
        Log.e(TAG, "processing snapshot");
        SugarTransactionHelper.doInTransaction(new SugarTransactionHelper.Callback() {
            @Override
            public void manipulateInTransaction() {
                if (AppPreferences.getInstance().isLoggedIn()) {
                    if (snapshot != null) {
                        processUsers(snapshot.child(KeyConstants.FIREBASE_KEY_USER));
                        processNoticeBoards(snapshot.child(KeyConstants.FIREBASE_KEY_NOTICEBOARD));
                        processNotices(snapshot.child(KeyConstants.FIREBASE_KEY_NOTICE));
                    } else {
                        Log.e(TAG, "Snapshot entity is null..");
                    }
                } else {
                    Log.e(TAG, "User is not logged in, cant process notice snapshot..");
                }
            }
        });
    }

    public synchronized void processUsers(DataSnapshot snapshot) {
        SOUser soUser;
        boolean newUser;
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            User user = userSnapshot.getValue(User.class);
            soUser = SOUser.findByUserId(user.getId());
            if (soUser == null) {
                Log.e(TAG, "User with id " + user.getId() + " not found in database");
                soUser = new SOUser();
                newUser = true;
            } else {
                Log.e(TAG, "User with id " + user.getId() + " found in database");
                newUser = false;
            }
            soUser.setUserId(user.getId());
            soUser.setAppOwner(user.getId()
                    == AppPreferences.getInstance().getAppOwnerId());
            soUser.setEmail(user.getEmail());
            soUser.setFullname(user.getFullname());
            soUser.setMobile(user.getMobile());
            soUser.setUsername(user.getUsername());

            if (newUser) {
                notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_USER);
                soUser.save();
            }
            else {
                SOUser.deleteAll(SOUser.class, SOUser.COLUMN_USER_ID + "=?", String.valueOf(soUser.getUserId()));
                soUser.save();
            }
        }
    }

    public synchronized void processNoticeBoards(DataSnapshot snapshot) {
        SONoticeBoard soNoticeBoard;
        SOUserMember soUserMember;
        boolean noticeBoardRelatedToAppOwner;
        boolean newNoticeBoard;
        for (DataSnapshot noticeBoardSnapshot : snapshot.getChildren()) {
            NoticeBoard noticeBoard = noticeBoardSnapshot.getValue(NoticeBoard.class);
            noticeBoardRelatedToAppOwner = false;
            for (UserMember userMember : noticeBoard.getMembers()) {
                if (userMember.getId() == AppPreferences.getInstance().getAppOwnerId()) {
                    noticeBoardRelatedToAppOwner = true;
                    break;
                }
            }
            if (noticeBoardRelatedToAppOwner) {
                soNoticeBoard = SONoticeBoard.findNoticeBoardById(noticeBoard.getId());
                if (soNoticeBoard == null) {
                    Log.e(TAG, "Notice board with id " + noticeBoard.getId() + " not found in database");
                    soNoticeBoard = new SONoticeBoard();
                    soNoticeBoard.setLastVisitedAt(System.currentTimeMillis());
                    newNoticeBoard = true;
                } else {
                    Log.e(TAG, "Notice board with id " + noticeBoard.getId() + " found in database");
                    newNoticeBoard = false;
                }
                soNoticeBoard.setNoticeBoardId(noticeBoard.getId());
                soNoticeBoard.setTitle(noticeBoard.getTitle());
                soNoticeBoard.setLastModifiedAt(noticeBoard.getLastModifiedAt());
                if (newNoticeBoard) {
                    soNoticeBoard.save();
                    if (subscriberList.isEmpty()) {
                        NotificationHandler.getInstance().showNotification("New notice board added!!", soNoticeBoard.getTitle());
                    }
                    else {
                        notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_NOTICE_BOARD);
                    }
                }
                else {
                    SONoticeBoard.deleteAll(SONoticeBoard.class, SONoticeBoard.COLUMN_NOTICE_BOARD_ID + "=?", String.valueOf(soNoticeBoard.getNoticeBoardId()));
                    soNoticeBoard.save();
                }

                SOUserMember.deleteUserMembersRelatedToNoticeBoard(soNoticeBoard.getNoticeBoardId());
                for (UserMember userMember : noticeBoard.getMembers()) {
                    soUserMember = new SOUserMember();
                    soUserMember.setNoticeBoardId(soNoticeBoard.getNoticeBoardId());
                    soUserMember.setUserId(userMember.getId());
                    soUserMember.setPermissions(userMember.getPermissions());
                    soUserMember.save();
                }
            }
        }
    }


    public synchronized void processNotices(DataSnapshot snapshot) {
        SONotice soNotice;
        boolean newNotice;

        for (DataSnapshot noticeSnapshot : snapshot.getChildren()) {
            Notice notice = noticeSnapshot.getValue(Notice.class);
            if (SONoticeBoard.findNoticeBoardById(notice.getNoticeBoardId()) == null) {
                continue;
            }
            soNotice = SONotice.findByNoticeId(notice.getId());
            if (soNotice == null) {
                Log.e(TAG, "Notice with id " + notice.getId() + " not found in database");
                soNotice = new SONotice();
                newNotice = true;
            } else {
                Log.e(TAG, "Notice with id " + notice.getId() + " found in database");
                newNotice = false;
            }
            soNotice.setNoticeId(notice.getId());
            soNotice.setCreatedAt(notice.getCreatedAt());
            soNotice.setTitle(notice.getTitle());
            soNotice.setDescription(notice.getDescription());
            soNotice.setOwner(notice.getOwner().getId());
            soNotice.setNoticeBoardId(notice.getNoticeBoardId());

            if (newNotice) {
                soNotice.save();
                if (subscriberList.isEmpty()) {
                    SONoticeBoard soNoticeBoard = SONoticeBoard.findNoticeBoardById(soNotice.getNoticeBoardId());
                    if (soNoticeBoard != null) {
                        NotificationHandler.getInstance().showNotification("New notice in " + soNoticeBoard.getTitle() + "!!",
                                soNotice.getTitle());
                    }
                }
                else {
                    notifyDatasetChanged(KeyConstants.OUTDATED_RESOURCE_NOTICE);
                }
            }
            else {
                SONotice.deleteAll(SONotice.class, SONotice.COLUMN_NOTICE_ID + "=?", String.valueOf(soNotice.getNoticeId()));
                soNotice.save();
            }
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

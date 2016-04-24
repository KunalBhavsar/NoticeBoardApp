package com.jyotitech.noticeboardapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.adapter.NoticeBoardListAdapter;
import com.jyotitech.noticeboardapp.adapter.NoticeListAdapter;
import com.jyotitech.noticeboardapp.adapter.UserListAdapter;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListActivity extends AppCompatActivity {

    private static final String TAG = NoticeListActivity.class.getSimpleName();
    private List<Notice> notices;
    private NoticeListAdapter noticeListAdapter;
    private SharedPreferences sharedPreferences;
    private Dialog dialogAddNotice;
    private Firebase firebaseNoticeBoard;
    private EditText edtTitle;
    private EditText edtDescription;
    private Activity mActivityContext;
    private Context mAppContext;
    private NoticeBoard noticeBoard;
    private String noticeBoardKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_or_notice_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActivityContext = this;
        mAppContext = getApplicationContext();

        long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
        sharedPreferences = getSharedPreferences(KeyConstants.SPREF_NAME, Context.MODE_PRIVATE);
        firebaseNoticeBoard = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICEBOARD);

        firebaseNoticeBoard.orderByChild("id").equalTo(selectedNoticeBoardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    noticeBoardKey = snapshot.getKey();
                    noticeBoard = snapshot.getValue(NoticeBoard.class);
                    break;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Create custom dialog object
        dialogAddNotice = new Dialog(this);
        // Include dialog.xml file
        dialogAddNotice.setContentView(R.layout.dialog_add_notice);

        // set values for custom dialog components - text, image and button
        Button btnAdd = (Button) dialogAddNotice.findViewById(R.id.btn_add);
        Button btnCancel = (Button) dialogAddNotice.findViewById(R.id.btn_cancel);
        edtTitle = (EditText) dialogAddNotice.findViewById(R.id.edt_notice_title);
        edtDescription = (EditText) dialogAddNotice.findViewById(R.id.edt_notice_description);

        // if decline button is clicked, close the custom dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString().trim();
                if(title.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_title, mActivityContext);
                    return;
                }
                String description = edtDescription.getText().toString().trim();
                if(description.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_description, mActivityContext);
                    return;
                }

                long largestNoticeId = 0;
                if(noticeBoard.getNotices() == null) {
                    noticeBoard.setNotices(new ArrayList<Notice>());
                }
                else {
                    for (Notice notice : noticeBoard.getNotices()) {
                        if (notice.getId() > largestNoticeId) {
                            largestNoticeId = notice.getId();
                        }
                    }
                }
                Notice notice = new Notice();
                notice.setCreatedAt(Calendar.getInstance().getTimeInMillis());
                notice.setDescription(description);
                notice.setTitle(title);
                UserMember userMember = new UserMember();
                userMember.setId(sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0));
                userMember.setFullname(sharedPreferences.getString(KeyConstants.SPREF_KEY_FOR_FULL_NAME, ""));
                userMember.setPermissions(KeyConstants.PERMISSION_WRITE);
                notice.setOwner(userMember);
                notice.setId(++largestNoticeId);

                Map<String, Object> updateHashmap = new HashMap<>();
                updateHashmap.put("lastModifiedAt", notice.getCreatedAt());
                //Push notice board to firebase
                firebaseNoticeBoard.child(noticeBoardKey).updateChildren(updateHashmap);
                firebaseNoticeBoard.child(noticeBoardKey).child("notices").push().setValue(notice);
                // Close dialog
                dialogAddNotice.hide();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialogAddNotice.hide();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtTitle.setText("");
                edtDescription.setText("");
                dialogAddNotice.show();
            }
        });

        RecyclerView recList = (RecyclerView) findViewById(R.id.recycler_view);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        notices = new ArrayList<>();
        long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
        noticeListAdapter = new NoticeListAdapter(this, notices, appOwnerId);
        recList.setAdapter(noticeListAdapter);

        Firebase firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICEBOARD);
        firebase.orderByChild("id").equalTo(selectedNoticeBoardId)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                notices.clear();
                Log.i(TAG, snapshot.toString());

                NoticeBoard noticeBoard = snapshot.getValue(NoticeBoard.class);
                //NoticeListActivity.this.getActionBar().setTitle(noticeBoard.getTitle());
                notices.addAll(noticeBoard.getNotices() != null ? noticeBoard.getNotices() : new ArrayList<Notice>());
                noticeListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if(dialogAddNotice != null) {
            dialogAddNotice.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notice_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


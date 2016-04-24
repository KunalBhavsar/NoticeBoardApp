package com.jyotitech.noticeboardapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.adapter.NoticeBoardListAdapter;
import com.jyotitech.noticeboardapp.adapter.UserListAdapter;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class NoticeBoardListActivity extends AppCompatActivity {

    private static final String TAG = NoticeBoardListActivity.class.getSimpleName();
    private List<NoticeBoard> noticeBoards;
    private NoticeBoardListAdapter noticeBoardListAdapter;
    private SharedPreferences sharedPreferences;
    private NoticeBoardListActivity mActivityContext;
    private Context mAppContext;
    private EditText edtTitle;
    private UserListAdapter userListAdapter;
    private CheckBox chkSelectAll;
    private Dialog dialogAddNoticeBoard;
    private long largestNoticeBoardId;
    private Firebase firebaseUser;
    private Firebase firebaseNoticeBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_or_notice_list);

        mActivityContext = this;
        mAppContext = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(KeyConstants.SPREF_NAME, Context.MODE_PRIVATE);

        firebaseUser = new Firebase(KeyConstants.FIREBASE_RESOURCE_USER);
        firebaseNoticeBoard = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICEBOARD);

        // Create custom dialog object
        dialogAddNoticeBoard = new Dialog(this);
        // Include dialog.xml file
        dialogAddNoticeBoard.setContentView(R.layout.dialog_add_notice_board);

        // set values for custom dialog components - text, image and button
        ListView lstUsers = (ListView) dialogAddNoticeBoard.findViewById(R.id.lst_names);
        Button btnAdd = (Button) dialogAddNoticeBoard.findViewById(R.id.btn_add);
        Button btnCancel = (Button) dialogAddNoticeBoard.findViewById(R.id.btn_cancel);
        edtTitle = (EditText) dialogAddNoticeBoard.findViewById(R.id.edt_notice_board_title);
        chkSelectAll = (CheckBox) dialogAddNoticeBoard.findViewById(R.id.checkbox);

        chkSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkbox = (CheckBox)v;
                userListAdapter.setAllSelection(checkbox.isChecked());
            }
        });

        userListAdapter = new UserListAdapter(mActivityContext);
        lstUsers.setAdapter(userListAdapter);

        // if decline button is clicked, close the custom dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = edtTitle.getText().toString().trim();
                if(title.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_title, mActivityContext);
                    return;
                }
                List<UserMember> selectedUsers = userListAdapter.getSelectedUserMembersList();
                if(selectedUsers.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_select_user, mActivityContext);
                    return;
                }
                //Add appowner with write permissions
                UserMember userMember = new UserMember();
                userMember.setId(sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0));
                userMember.setFullname(sharedPreferences.getString(KeyConstants.SPREF_KEY_FOR_FULL_NAME, ""));
                userMember.setPermissions(KeyConstants.PERMISSION_WRITE);
                selectedUsers.add(userMember);

                //Push notice board to firebase
                NoticeBoard noticeBoard =  new NoticeBoard();
                noticeBoard.setTitle(title);
                noticeBoard.setMembers(selectedUsers);
                noticeBoard.setNotices(new ArrayList<Notice>());
                noticeBoard.setLastModifiedAt(Calendar.getInstance().getTimeInMillis());
                noticeBoard.setId(++largestNoticeBoardId);
                firebaseNoticeBoard.push().setValue(noticeBoard);
                // Close dialog
                dialogAddNoticeBoard.hide();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialogAddNoticeBoard.hide();
            }
        });

        firebaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
                List<UserMember> userMemberList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long childId = (long) snapshot.child("id").getValue();
                    if(appOwnerId != childId) {
                        UserMember userMember = new UserMember();
                        userMember.setFullname((String) snapshot.child("fullname").getValue());
                        userMember.setId(childId);
                        userMember.setPermissions(KeyConstants.PERMISSION_READ);
                        userMemberList.add(userMember);
                    }
                }
                userListAdapter.addDataSource(userMemberList);
                setAllSelectedCheckbox();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.getMessage());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtTitle.setText("");
                userListAdapter.clearSelectedList();
                dialogAddNoticeBoard.show();
            }
        });

        RecyclerView recList = (RecyclerView) findViewById(R.id.recycler_view);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        noticeBoards = new ArrayList<>();
        long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
        noticeBoardListAdapter = new NoticeBoardListAdapter(this, noticeBoards, appOwnerId);
        recList.setAdapter(noticeBoardListAdapter);

        firebaseNoticeBoard.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                noticeBoards.clear();
                long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
                for (DataSnapshot noticeBoardSnapshot : snapshot.getChildren()) {
                    NoticeBoard noticeBoard = noticeBoardSnapshot.getValue(NoticeBoard.class);
                    if(noticeBoard.getId() > largestNoticeBoardId) {
                        largestNoticeBoardId = noticeBoard.getId();
                    }
                    for (UserMember userMember : noticeBoard.getMembers()) {
                        if (userMember.getId() == appOwnerId) {
                            noticeBoards.add(noticeBoard);
                        }
                    }
                }
                noticeBoardListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });

    }

    @Override
    protected void onDestroy() {
        if(dialogAddNoticeBoard != null) {
            dialogAddNoticeBoard.dismiss();
        }
        super.onDestroy();
    }

    public void setAllSelectedCheckbox() {
        chkSelectAll.setChecked(userListAdapter.isAllSelected());
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

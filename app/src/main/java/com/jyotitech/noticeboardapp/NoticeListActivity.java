package com.jyotitech.noticeboardapp;

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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.adapter.NoticeBoardListAdapter;
import com.jyotitech.noticeboardapp.adapter.NoticeListAdapter;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListActivity extends AppCompatActivity {

    private static final String TAG = NoticeListActivity.class.getSimpleName();
    private List<Notice> notices;
    private NoticeListAdapter noticeListAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_or_notice_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(KeyConstants.SPREF_NAME, Context.MODE_PRIVATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
        Firebase firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICEBOARD);
        firebase.orderByChild("id").equalTo(selectedNoticeBoardId)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                notices.clear();
                Log.i(TAG, snapshot.toString());

                NoticeBoard noticeBoard = snapshot.getValue(NoticeBoard.class);
                //NoticeListActivity.this.getActionBar().setTitle(noticeBoard.getTitle());
                notices.addAll(noticeBoard.getNotices());
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


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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.adapter.NoticeBoardListAdapter;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.User;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

public class NoticeBoardListActivity extends AppCompatActivity {

    private static final String TAG = NoticeBoardListActivity.class.getSimpleName();
    private List<NoticeBoard> noticeBoards;
    private NoticeBoardListAdapter noticeBoardListAdapter;
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
        noticeBoards = new ArrayList<>();
        long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
        noticeBoardListAdapter = new NoticeBoardListAdapter(this, noticeBoards, appOwnerId);
        recList.setAdapter(noticeBoardListAdapter);

        Firebase firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICEBOARD);
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                noticeBoards.clear();
                long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
                for (DataSnapshot noticeBoardSnapshot: snapshot.getChildren()) {
                    NoticeBoard noticeBoard = noticeBoardSnapshot.getValue(NoticeBoard.class);
                    for (UserMember userMember : noticeBoard.getMembers()) {
                        if(userMember.getId() == appOwnerId){
                            noticeBoards.add(noticeBoard);
                        }
                    }
                }
                noticeBoardListAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(FirebaseError error) {
                Log.e(TAG, error.getMessage());
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

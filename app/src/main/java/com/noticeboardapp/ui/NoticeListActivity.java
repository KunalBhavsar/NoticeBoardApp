package com.noticeboardapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.noticeboardapp.R;
import com.noticeboardapp.adapter.AttachmentAdapter;
import com.noticeboardapp.adapter.NoticeListAdapter;
import com.noticeboardapp.interfaces.OutdatedResourceSubscriber;
import com.noticeboardapp.model.Media;
import com.noticeboardapp.model.MediaMini;
import com.noticeboardapp.model.Notice;
import com.noticeboardapp.model.UserMember;
import com.noticeboardapp.sugar_models.SOAttachment;
import com.noticeboardapp.sugar_models.SONotice;
import com.noticeboardapp.sugar_models.SONoticeBoard;
import com.noticeboardapp.sugar_models.SOUser;
import com.noticeboardapp.utils.AppPreferences;
import com.noticeboardapp.utils.FileUtils;
import com.noticeboardapp.utils.KeyConstants;
import com.noticeboardapp.utils.NetworkUtils;
import com.noticeboardapp.utils.ToastMaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListActivity extends OutdatedResourceSubscriberActivity implements OutdatedResourceSubscriber {

    private static final String TAG = NoticeListActivity.class.getSimpleName();

    private Activity mActivityContext;
    private Context mAppContext;

    private NoticeListAdapter noticeListAdapter;

    private RecyclerView recList;
    private FloatingActionButton fab;
    private RelativeLayout rltProgress;
    private SONoticeBoard selectedNoticeBoard;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_or_notice_list);

        mActivityContext = this;
        mAppContext = getApplicationContext();
        rltProgress = (RelativeLayout) findViewById(R.id.rlt_progress);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recList = (RecyclerView) findViewById(R.id.recycler_view);
        edtSearch = (EditText)findViewById(R.id.edt_search);
        edtSearch.setHint(R.string.hint_search_notice);
        setProgress(true);

        long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
        selectedNoticeBoard = SONoticeBoard.findNoticeBoardById(selectedNoticeBoardId);

        if (selectedNoticeBoard == null) {
            finish();
            return;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            toolbar.setTitle(selectedNoticeBoard.getTitle());
        setSupportActionBar(toolbar);

        if (fab != null) {
            if (!selectedNoticeBoard.isAppOwnerIsOwnerOfNoticeBoard()) {
                Log.i(TAG, "App owner is not owner of notice board");
                fab.setVisibility(View.GONE);
            } else {
                Log.i(TAG, "App owner is owner of notice board");
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mActivityContext, NoticeDetailActivity.class);
                        intent.putExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, selectedNoticeBoard.getNoticeBoardId());
                        mActivityContext.startActivity(intent);
                    }
                });
            }
        }

        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        noticeListAdapter = new NoticeListAdapter(this);
        recList.setAdapter(noticeListAdapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noticeListAdapter.getFilter().filter(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        updateNotices();
        super.onResume();
    }

    private void setProgress(boolean flag) {
        if (flag) {
            rltProgress.setVisibility(View.VISIBLE);
        } else {
            rltProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_notice_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateNotices() {
        new AsyncTask<String, Void, List<SONotice>>() {
            @Override
            protected void onPreExecute() {
                setProgress(true);
            }

            @Override
            protected List<SONotice> doInBackground(String... params) {
                return SONotice.find(SONotice.class, "notice_board_id=?", params[0]);
            }

            @Override
            protected void onPostExecute(List<SONotice> notices) {
                noticeListAdapter.setDataSource(notices);
                setProgress(false);
            }
        }.execute(String.valueOf(selectedNoticeBoard.getNoticeBoardId()));
    }


    @Override
    public void onDatasetChanged(String dataset) {
        if (dataset.equals(KeyConstants.OUTDATED_RESOURCE_NOTICE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateNotices();
                }
            });
        }
    }
}


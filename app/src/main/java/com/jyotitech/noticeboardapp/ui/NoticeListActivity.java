package com.jyotitech.noticeboardapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.adapter.NoticeListAdapter;
import com.jyotitech.noticeboardapp.interfaces.OutdatedResourceSubscriber;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.sugar_models.SONotice;
import com.jyotitech.noticeboardapp.sugar_models.SONoticeBoard;
import com.jyotitech.noticeboardapp.sugar_models.SOUser;
import com.jyotitech.noticeboardapp.sugar_models.SOUserMember;
import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NetworkUtils;
import com.jyotitech.noticeboardapp.utils.NotificationHandler;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListActivity extends OutdatedResourceSubscriberActivity implements OutdatedResourceSubscriber {

    private static final String TAG = NoticeListActivity.class.getSimpleName();
    private static final int SELECT_FILE = 200;
    private static final int REQUEST_CAMERA = 100;

    private NoticeListAdapter noticeListAdapter;
    private Dialog dialogAddNotice;

    private EditText edtTitle;
    private TextView txtAttachment;
    private EditText edtDescription;
    private Activity mActivityContext;
    private Context mAppContext;
    private Bitmap thumbnail;
    private String imgString;
    private RecyclerView recList;
    private FloatingActionButton fab;
    private ImageView imgDialog;
    private RelativeLayout rltProgress;
    private long lastNoticeId = 0;
    private SONoticeBoard selectedNoticeBoard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_or_notice_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActivityContext = this;
        mAppContext = getApplicationContext();
        rltProgress = (RelativeLayout) findViewById(R.id.rlt_progress);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recList = (RecyclerView) findViewById(R.id.recycler_view);

        setProgress(true);

        long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
        selectedNoticeBoard = SONoticeBoard.findNoticeBoardById(selectedNoticeBoardId);

        if(selectedNoticeBoard == null) {
            finish();
            return;
        }

        if (fab != null) {
            if(selectedNoticeBoard.isAppOwnerIsOwnerOfNoticeBoard()) {
                fab.setVisibility(View.GONE);
            }
            else {
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        edtTitle.setText("");
                        edtDescription.setText("");
                        txtAttachment.setText("Add attachment");
                        txtAttachment.setTextColor(getResources().getColor(R.color.grey_555555));
                        //imgDialog.setImageBitmap(null);
                        imgString = "";
                        thumbnail = null;
                        dialogAddNotice.show();
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

        createAddNoticeDialog();
        updateNotices();
    }

    private void createAddNoticeDialog() {
        // Create custom dialog object
        dialogAddNotice = new Dialog(this);
        // Include dialog.xml file
        dialogAddNotice.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAddNotice.setContentView(R.layout.dialog_add_notice);

        // set values for custom dialog components - text, image and button
        Button btnAdd = (Button) dialogAddNotice.findViewById(R.id.btn_add);
        Button btnCancel = (Button) dialogAddNotice.findViewById(R.id.btn_cancel);
        edtTitle = (EditText) dialogAddNotice.findViewById(R.id.edt_notice_title);
        txtAttachment = (TextView) dialogAddNotice.findViewById(R.id.edt_attachment);
        edtDescription = (EditText) dialogAddNotice.findViewById(R.id.edt_notice_description);
        imgDialog = (ImageView) dialogAddNotice.findViewById(R.id.img_photo);
        txtAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        // if decline button is clicked, close the custom dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTitle.getText().toString().trim().isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_title, mActivityContext);
                    return;
                }
                if (edtDescription.getText().toString().trim().isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_description, mActivityContext);
                    return;
                }

                /**Process the request **/
                if (NetworkUtils.isConnectedToInternet(mAppContext)) {
                    processAddNoticeRequest();
                } else {
                    ToastMaker.createShortToast(R.string.toast_internet_connection_error, mActivityContext);
                }
            }
        });

        btnCancel.setTag(dialogAddNotice);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                ((Dialog) v.getTag()).hide();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (dialogAddNotice != null) {
            dialogAddNotice.dismiss();
        }
        super.onDestroy();
    }

    private void setProgress(boolean flag) {
        if (flag) {
            rltProgress.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            recList.setVisibility(View.GONE);
        }
        else {
            rltProgress.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            recList.setVisibility(View.VISIBLE);
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

    private void processAddNoticeRequest() {
        new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE).orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notice notice = snapshot.getValue(Notice.class);
                    lastNoticeId = notice.getId();
                }

                Notice notice = new Notice();
                notice.setCreatedAt(Calendar.getInstance().getTimeInMillis());
                notice.setDescription(edtDescription.getText().toString().trim());
                notice.setTitle(edtTitle.getText().toString().trim());
                if (imgString != null)
                    notice.setAttachments(imgString);
                notice.setNoticeBoardId(selectedNoticeBoard.getNoticeBoardId());

                SOUser appOwner = SOUser.findByUserId(AppPreferences.getInstance().getAppOwnerId());
                if (appOwner != null) {
                    UserMember userMember = new UserMember();
                    userMember.setId(appOwner.getUserId());
                    userMember.setFullname(appOwner.getFullname());
                    userMember.setPermissions(KeyConstants.PERMISSION_WRITE);
                    notice.setOwner(userMember);
                }
                notice.setId(++lastNoticeId);

                SONotice soNotice = new SONotice();
                soNotice.setTitle(notice.getTitle());
                soNotice.setDescription(notice.getDescription());
                soNotice.setCreatedAt(notice.getCreatedAt());
                soNotice.setNoticeBoardId(notice.getNoticeBoardId());
                soNotice.setNoticeId(notice.getId());
                soNotice.setOwner(AppPreferences.getInstance().getAppOwnerId());
                soNotice.save();

                noticeListAdapter.addDataToDatasource(soNotice);

                //Push notice to firebase
                new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE).push().setValue(notice, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Log.e(TAG, getString(R.string.toast_error_in_data_sync) + "\n" + firebaseError.getMessage());
                        } else {
                            Log.e(TAG, getString(R.string.toast_data_synced));
                        }
                    }
                });
                dialogAddNotice.dismiss();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.getMessage());
            }
        });
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

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(NoticeListActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {
                thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                thumbnail = BitmapFactory.decodeFile(selectedImagePath, options);
            }

            if (thumbnail != null) {

                txtAttachment.setText("Attachment added");
                txtAttachment.setTextColor(getResources().getColor(R.color.green));
                //imgDialog.setImageBitmap(thumbnail);
                ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
                Log.i("bitmap", "bitmap thumbnail " + thumbnail.getWidth());
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
                thumbnail.recycle();

                byte[] byteArray = bYtE.toByteArray();
                if(requestCode == REQUEST_CAMERA) {
                    imgString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }else {
                    imgString = Base64.encodeToString(byteArray, Base64.URL_SAFE);
                }

            }
        }
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


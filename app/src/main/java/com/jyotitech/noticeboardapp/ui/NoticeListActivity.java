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
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NotificationHandler;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListActivity extends AppCompatActivity {

    private static final String TAG = NoticeListActivity.class.getSimpleName();
    private static final int SELECT_FILE = 200;
    private static final int REQUEST_CAMERA = 100;

    private List<Notice> notices;
    private NoticeListAdapter noticeListAdapter;
    private SharedPreferences sharedPreferences;
    private Dialog dialogAddNotice;
    private Firebase firebaseNotice;
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

        final long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
        sharedPreferences = getSharedPreferences(KeyConstants.SPREF_NAME, Context.MODE_PRIVATE);
        firebaseNotice = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE);

        firebaseNotice.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notice notice = snapshot.getValue(Notice.class);
                    lastNoticeId = notice.getId();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
                String title = edtTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_title, mActivityContext);
                    return;
                }
                String description = edtDescription.getText().toString().trim();
                if (description.isEmpty()) {
                    ToastMaker.createShortToast(R.string.error_enter_description, mActivityContext);
                    return;
                }

                //long largestNoticeId = 0;

                Notice notice = new Notice();
                notice.setCreatedAt(Calendar.getInstance().getTimeInMillis());
                notice.setDescription(description);
                notice.setTitle(title);
                if (imgString != null)
                    notice.setAttachments(imgString);
                notice.setNoticeBoardId(selectedNoticeBoardId);
                UserMember userMember = new UserMember();
                userMember.setId(sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0));
                userMember.setFullname(sharedPreferences.getString(KeyConstants.SPREF_KEY_FOR_FULL_NAME, ""));
                userMember.setPermissions(KeyConstants.PERMISSION_WRITE);
                notice.setOwner(userMember);
                notice.setId(++lastNoticeId);

                Map<String, Object> updateHashmap = new HashMap<>();
                updateHashmap.put("lastModifiedAt", notice.getCreatedAt());
                //Push notice to firebase
                firebaseNotice.push().setValue(notice);
                notices.add(notice);
                noticeListAdapter.notifyDataSetChanged();
                dialogAddNotice.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialogAddNotice.hide();
            }
        });

        if (fab != null) {
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

        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        notices = new ArrayList<>();
        final long appOwnerId = sharedPreferences.getLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, 0);
        noticeListAdapter = new NoticeListAdapter(this, notices, appOwnerId);
        recList.setAdapter(noticeListAdapter);

        Firebase firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE);
        firebase.orderByChild("noticeBoardId").equalTo(selectedNoticeBoardId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        notices.clear();
                        for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                            Notice notice = snapshot1.getValue(Notice.class);
                            notices.add(notice);
                        }

                        noticeListAdapter.notifyDataSetChanged();
                        setProgress(false);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Notice notice = dataSnapshot.getValue(Notice.class);
                Log.e(TAG, "notify " + appOwnerId + " " + notice.getOwner().getId());
                if(appOwnerId != notice.getOwner().getId()) {
                    NotificationHandler notificationHandler = new NotificationHandler(NoticeListActivity.this);
                    notificationHandler.showNotification("Notice added by "+ notice.getOwner().getFullname(), notice.getDescription());
                }
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
        if (dialogAddNotice != null) {
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
        if (id == R.id.action_logout) {
            SharedPreferences sPref = getSharedPreferences(KeyConstants.SPREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.clear().commit();

            Intent i = new Intent(NoticeListActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setProgress(boolean flag) {
        if (flag) {
            rltProgress.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            recList.setVisibility(View.GONE);
        } else {
            rltProgress.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            recList.setVisibility(View.VISIBLE);
        }
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

}


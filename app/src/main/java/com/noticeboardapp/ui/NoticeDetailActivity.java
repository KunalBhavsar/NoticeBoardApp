package com.noticeboardapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.noticeboardapp.R;
import com.noticeboardapp.adapter.AttachmentAdapter;
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

/**
 * Created by Kunal Bhavsar on 2/5/16.
 */
public class NoticeDetailActivity extends OutdatedResourceSubscriberActivity {

    private static final String TAG = NoticeDetailActivity.class.getSimpleName();

    private EditText edtTitle;
    private Button btnAttachment;
    private EditText edtDescription;
    private Activity mActivityContext;
    private Context mAppContext;
    private RecyclerView lstAttachments;
    private long lastNoticeId = 0;
    private long lastMediaId = 0;
    private AttachmentAdapter attachmentAdapter;

    private static final String SAVED_STATE_TEMP_IMAGE_PATH = "temp_image_path";
    private static final int SELECT_PDF_FILE = 201;
    private static final int SELECT_IMAGE_FILE = 200;
    private static final int REQUEST_CAMERA = 100;

    private boolean isEditNotice;
    private String tempImageFilePath;
    private SONoticeBoard selectedNoticeBoard;
    private SONotice soNoticeSelectedForEdit;
    private RelativeLayout rltProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        mActivityContext = this;
        mAppContext = getApplicationContext();

        // set values for custom dialog components - text, image and button
        Button btnAdd = (Button) findViewById(R.id.btn_add);
        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        edtTitle = (EditText) findViewById(R.id.edt_notice_title);
        btnAttachment = (Button) findViewById(R.id.btn_attachment);
        edtDescription = (EditText) findViewById(R.id.edt_notice_description);
        lstAttachments = (RecyclerView) findViewById(R.id.lst_attachments);
        rltProgress = (RelativeLayout)findViewById(R.id.rlt_progress);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        lstAttachments.setLayoutManager(llm);
        attachmentAdapter = new AttachmentAdapter(NoticeDetailActivity.this);
        lstAttachments.setAdapter(attachmentAdapter);
        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        if(getIntent().hasExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY)) {
            long selectedNoticeBoardId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, 0);
            selectedNoticeBoard = SONoticeBoard.findNoticeBoardById(selectedNoticeBoardId);

            if (selectedNoticeBoard == null) {
                ToastMaker.createShortToast(R.string.toast_error_in_loading_image, mAppContext);
                finish();
                return;
            }
        }
        else if(getIntent().hasExtra(KeyConstants.EXTRA_FROM_NOTICE_LIST_TO_NOTICE_DETAIL_ACTIVITY)) {
            long selectedNoticeId = getIntent().getLongExtra(KeyConstants.EXTRA_FROM_NOTICE_LIST_TO_NOTICE_DETAIL_ACTIVITY, 0);
            soNoticeSelectedForEdit = SONotice.findByNoticeId(selectedNoticeId);
            isEditNotice = true;
            if (soNoticeSelectedForEdit == null) {
                ToastMaker.createShortToast(R.string.toast_error_in_loading_image, mAppContext);
                finish();
                return;
            }

            edtTitle.setText(soNoticeSelectedForEdit.getTitle());
            edtDescription.setText(soNoticeSelectedForEdit.getDescription());
            if(btnAdd != null)
                btnAdd.setText("Update");
            ImageButton btnDelete = (ImageButton)findViewById(R.id.btn_delete);
            btnDelete.setVisibility(View.VISIBLE);
            if(btnDelete != null)
                btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivityContext);
                    alertDialogBuilder.setMessage(R.string.alert_delete_notice)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(NetworkUtils.isConnectedToInternet(mAppContext)) {
                                        setProgress(true);
                                        new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE).runTransaction(new Transaction.Handler() {
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                Log.i(TAG, "select notice id " + soNoticeSelectedForEdit.getNoticeId());
                                                for (MutableData mutableDataChild : mutableData.getChildren()) {
                                                    Log.i(TAG, "select notice id " + (long) mutableDataChild.child("id").getValue());
                                                    if (((long) mutableDataChild.child("id").getValue() == soNoticeSelectedForEdit.getNoticeId())) {
                                                        mutableDataChild.setValue(null); // This removes the node.
                                                        break;
                                                    }
                                                }
                                                return Transaction.success(mutableData);
                                            }

                                            public void onComplete(FirebaseError error, boolean b, DataSnapshot data) {
                                                // Handle completion
                                                setProgress(false);
                                                if(b) {
                                                    soNoticeSelectedForEdit.delete();
                                                    ToastMaker.createShortToast(R.string.toast_notice_deleted, mAppContext);
                                                    finish();
                                                }
                                                else {
                                                    ToastMaker.createShortToast(R.string.toast_error_in_data_sync, mAppContext);
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        ToastMaker.createShortToast(R.string.toast_internet_connection_error, mAppContext);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialogBuilder.show();
                }
            });
        }
        else {
            ToastMaker.createShortToast(R.string.toast_error_in_loading_image, mAppContext);
            finish();
            return;
        }

        if (savedInstanceState != null) {
            tempImageFilePath = savedInstanceState.getString(SAVED_STATE_TEMP_IMAGE_PATH);
        }

        // if decline button is clicked, close the custom dialog
        if(btnAdd != null) {
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
                        processAddOrUpdateNoticeRequest();
                    } else {
                        ToastMaker.createShortToast(R.string.toast_internet_connection_error, mActivityContext);
                    }
                }
            });
        }
        if(btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void setProgress(boolean flag) {
        if (flag) {
            rltProgress.setVisibility(View.VISIBLE);
            btnAttachment.setBackgroundColor(getResources().getColor(R.color.gray_glass));
        } else {
            rltProgress.setVisibility(View.GONE);
            btnAttachment.setBackgroundColor(getResources().getColor(R.color.accent));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_STATE_TEMP_IMAGE_PATH, tempImageFilePath);
        super.onSaveInstanceState(outState);
    }

    private void processAddOrUpdateNoticeRequest() {
        setProgress(true);
        new Firebase(KeyConstants.FIREBASE_RESOURCE_NOTICE).orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notice notice = snapshot.getValue(Notice.class);
                    lastNoticeId = notice.getId();
                }
                ++lastNoticeId;
                final Notice notice = new Notice();
                notice.setCreatedAt(Calendar.getInstance().getTimeInMillis());
                notice.setDescription(edtDescription.getText().toString().trim());
                notice.setTitle(edtTitle.getText().toString().trim());
                new Firebase(KeyConstants.FIREBASE_RESOURCE_MEDIA).orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Media media = snapshot.getValue(Media.class);
                            lastMediaId = media.getId();
                        }
                        for (SOAttachment soAttachment : attachmentAdapter.getAttachments()) {
                            Media media = new Media();
                            media.setNoticeId(lastNoticeId);
                            media.setName(soAttachment.getName());
                            media.setMediaType(soAttachment.getAttachmentType());
                            media.setId(++lastMediaId);
                            if(media.getMediaType().equals(KeyConstants.MEDIA_TYPE_IMAGE)) {
                                media.setData(FileUtils.getInstance().scaleAndCompressImageToBase64(BitmapFactory.decodeFile(soAttachment.getLocalFilePath())));
                            }
                            else {
                                try {
                                    media.setData(FileUtils.getInstance().encodeFileTOBase64(soAttachment.getLocalFilePath()));
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                            new Firebase(KeyConstants.FIREBASE_RESOURCE_MEDIA).push().setValue(media);
                            MediaMini mediaMini = new MediaMini();
                            mediaMini.setId(media.getId());
                            mediaMini.setMediaType(media.getMediaType());
                            notice.getAttachments().add(mediaMini);
                            soAttachment.setNoticeId(lastNoticeId);
                            soAttachment.setAttachmentId(lastMediaId);
                            soAttachment.save();
                        }
                        notice.setNoticeBoardId(selectedNoticeBoard.getNoticeBoardId());

                        SOUser appOwner = SOUser.findByUserId(AppPreferences.getInstance().getAppOwnerId());
                        if (appOwner != null) {
                            UserMember userMember = new UserMember();
                            userMember.setId(appOwner.getUserId());
                            userMember.setFullname(appOwner.getFullname());
                            userMember.setPermissions(KeyConstants.PERMISSION_WRITE);
                            notice.setOwner(userMember);
                        }
                        notice.setId(lastNoticeId);

                        SONotice soNotice = new SONotice();
                        soNotice.setTitle(notice.getTitle());
                        soNotice.setDescription(notice.getDescription());
                        soNotice.setCreatedAt(notice.getCreatedAt());
                        soNotice.setNoticeBoardId(notice.getNoticeBoardId());
                        soNotice.setNoticeId(notice.getId());
                        soNotice.setOwner(AppPreferences.getInstance().getAppOwnerId());
                        soNotice.save();

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
                        setProgress(false);
                        finish();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                        setProgress(false);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.getMessage());
            }
        });
    }


    private void selectImage() {
        final CharSequence[] items = {"Take Photo using camera", "Choose photo from Library",
                "Choose PDF from storage", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(NoticeDetailActivity.this);
        builder.setTitle("Add File!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo using camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = FileUtils.getInstance().getOutputMediaFileUri(KeyConstants.MEDIA_TYPE_IMAGE);
                    tempImageFilePath = uri.getPath();
                    Log.i(TAG, "Path of image : " + tempImageFilePath);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose photo from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), SELECT_IMAGE_FILE);
                } else if (items[item].equals("Choose PDF from storage")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), SELECT_PDF_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter title");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString();
                    if (name.trim().isEmpty()) {
                        ToastMaker.createShortToast(R.string.error_enter_file_name, mAppContext);
                        return;
                    }
                    SOAttachment soAttachment = new SOAttachment();
                    soAttachment.setName(name);
                    switch (requestCode) {
                        case REQUEST_CAMERA:
                            Bitmap bitmap = FileUtils.getInstance().compressAndScaleImageToBitmap(BitmapFactory.decodeFile(tempImageFilePath));
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(tempImageFilePath);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            soAttachment.setLocalFilePath(tempImageFilePath);
                            soAttachment.setIsDownloaded(true);
                            soAttachment.setAttachmentType(KeyConstants.MEDIA_TYPE_IMAGE);
                            break;
                        case SELECT_IMAGE_FILE:
                            if (data != null) {
                                String imagePath = processPicSelectedFromGallery(data);
                                try {
                                    File srcFile = new File(imagePath);
                                    File destFile = FileUtils.getInstance().getOutputMediaFile(KeyConstants.MEDIA_TYPE_IMAGE);
                                    FileUtils.copyFile(srcFile, destFile);
                                    Log.i(TAG, "Storing file on absolute path " + destFile.getAbsolutePath());
                                    bitmap = FileUtils.getInstance().compressAndScaleImageToBitmap(BitmapFactory.decodeFile(destFile.getAbsolutePath()));
                                    out = null;
                                    try {
                                        out = new FileOutputStream(destFile.getAbsolutePath());
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (out != null) {
                                                out.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    soAttachment.setLocalFilePath(destFile.getAbsolutePath());
                                    soAttachment.setIsDownloaded(true);
                                    soAttachment.setAttachmentType(KeyConstants.MEDIA_TYPE_IMAGE);

                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                            break;
                        case SELECT_PDF_FILE:
                            Uri uri = data.getData();
                            if(uri.getLastPathSegment().endsWith("pdf")) {
                                String pdfPath = uri.getPath();
                                File srcFile = new File(pdfPath);
                                File destFile = FileUtils.getInstance().getOutputMediaFile(KeyConstants.MEDIA_TYPE_PDF);
                                try {
                                    FileUtils.copyFile(srcFile, destFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                soAttachment.setLocalFilePath(pdfPath);
                                soAttachment.setIsDownloaded(true);
                                soAttachment.setAttachmentType(KeyConstants.MEDIA_TYPE_PDF);
                            }
                            else {
                                ToastMaker.createShortToast(R.string.toast_error_in_loading_image, mActivityContext);
                            }
                        default:
                            break;
                    }
                    attachmentAdapter.addAttachment(soAttachment);
                }
            });
            builder.show();
        }
    }

    private String processPicSelectedFromGallery(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = mActivityContext.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        String galleryPicturePath = null;
        if(cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            galleryPicturePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return galleryPicturePath;
    }

}

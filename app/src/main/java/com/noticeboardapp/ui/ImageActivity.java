package com.noticeboardapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.noticeboardapp.R;
import com.noticeboardapp.utils.KeyConstants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends OutdatedResourceSubscriberActivity {

    private static final String TAG = ImageActivity.class.getSimpleName();
    private static final String SAVED_STATE_ATTACHMENT_IMAGE_PATH = "attachment_id";
    ImageView image;
    PhotoViewAttacher mAttacher;

    ImageActivity activity;
    RelativeLayout rltProgress;

    //SOAttachment soAttachment;
    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        image = (ImageView) findViewById(R.id.image);
        rltProgress = (RelativeLayout) findViewById(R.id.rlt_progress);
        activity = this;

        if(savedInstanceState == null) {
            imagePath = getIntent().getStringExtra(KeyConstants.EXTRA_FROM_NOTICE_LIST_TO_IMAGE_VIEW_ACTIVITY);
        }
        else {
            imagePath = savedInstanceState.getString(SAVED_STATE_ATTACHMENT_IMAGE_PATH);
        }

        Log.i(TAG, "Image path recceived : " + imagePath);

        if(isStoragePermissionGranted())
            setImage();
        /*soAttachment = SOAttachment.findByAttachmentId(attachmentId);

        if(soAttachment == null) {
            ToastMaker.createShortToast(R.string.toast_error_in_loading_image, activity);
            finish();
            return;
        }*/
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private void setProgress(boolean flag) {
        if (flag) {
            rltProgress.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
        } else {
            rltProgress.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
        }
    }

    private void setImage() {
        /*if (soAttachment.isDownloaded()) {
            try {
                SOAttachment.delete(soAttachment);
                String imagePath = FileUtils.getInstance().decodeFileFromBase64(soAttachment.getData(),
                        System.currentTimeMillis() + "_" + AppPreferences.getInstance().getAppOwnerId() + ".jpg");
                soAttachment.setIsDownloaded(true);
                soAttachment.setLocalFilePath(imagePath);
                soAttachment.save();
            }
            catch (IOException e) {
                ToastMaker.createShortToast(R.string.toast_error_in_loading_image, activity);
                Log.e(TAG, e.getMessage(), e);
                finish();
                return;
            }
        }*/
        File file = new File(imagePath);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        double screenWidthInPixels = (double) config.screenWidthDp * dm.density;
        double screenHeightInPixels = screenWidthInPixels * dm.heightPixels / dm.widthPixels;

        Picasso.with(activity)
        .load(file)
        .resize((int) (screenWidthInPixels + .5),
                (int) (screenHeightInPixels + .5))
        .centerInside()
        .placeholder(R.drawable.ic_account)
        .into(image, new Callback() {
            @Override
            public void onSuccess() {
                setProgress(false);
                mAttacher = new PhotoViewAttacher(image);
                mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                mAttacher.setZoomable(true);

                //Forcefully executing touch event to get image in screen visibility
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;
                float x = 0.0f;
                float y = 0.0f;
                int metaState = 0;
                MotionEvent motionEvent = MotionEvent.obtain(
                        downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState
                );
                image.dispatchTouchEvent(motionEvent);
            }

            @Override
            public void onError() {
                setProgress(false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_STATE_ATTACHMENT_IMAGE_PATH, imagePath);
        super.onSaveInstanceState(outState);
    }
}

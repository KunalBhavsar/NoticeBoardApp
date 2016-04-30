package com.jyotitech.noticeboardapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.User;
import com.jyotitech.noticeboardapp.sugar_models.SOUser;
import com.jyotitech.noticeboardapp.utils.ActivityUtils;
import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NetworkUtils;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

/**
 * Created by Pinky Walve on 22-Apr-16.
 */
public class LoginActivity extends OutdatedResourceSubscriberActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText edtUsername;
    EditText edtPassword;
    Button btnLogin;
    Button btnRegister;
    LoginActivity mActivityContext;
    Context mAppContext;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAppContext = getApplicationContext();
        mActivityContext = this;

        if (AppPreferences.getInstance().isLoggedIn()) {
            Intent intent = new Intent(mActivityContext, NoticeBoardListActivity.class);
            mActivityContext.startActivity(intent);
            finish();
        }

        //getActionBar().setTitle(getString(R.string.title_login));
        progressDialog = new ProgressDialog(mActivityContext);
        progressDialog.setMessage(getString(R.string.loading_text));
        progressDialog.setCancelable(false);

        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister = (Button) findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivityContext, RegisterActivity.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.hideKeyboard(mActivityContext);
                if (NetworkUtils.isConnectedToInternet(mAppContext)) {
                    progressDialog.show();
                    new Firebase(KeyConstants.FIREBASE_RESOURCE_USER).orderByChild("username").equalTo(edtUsername.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // do some stuff once
                            boolean usernameNotPresent = true;
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = dataSnapshot.getValue(User.class);
                                usernameNotPresent = false;
                                if (!user.getPassword().equals(edtPassword.getText().toString())) {
                                    progressDialog.dismiss();
                                    ToastMaker.createShortToast(R.string.toast_wrong_login_input, mActivityContext);
                                }
                                else {
                                    AppPreferences.getInstance().setAppOwnerId(user.getId());
                                    AppPreferences.getInstance().setIsLoggedIn(true);

                                    new Firebase(KeyConstants.FIREBASE_BASE_URL).orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.i(TAG, "Called on data changed");
                                            ((NoticeBoardApplication) mAppContext).processUsers(dataSnapshot.child(KeyConstants.FIREBASE_KEY_USER));
                                            ((NoticeBoardApplication) mAppContext).processNoticeBoards(dataSnapshot.child(KeyConstants.FIREBASE_KEY_NOTICEBOARD));
                                            ((NoticeBoardApplication) mAppContext).processNotices(dataSnapshot.child(KeyConstants.FIREBASE_KEY_NOTICE));

                                            progressDialog.dismiss();
                                            Intent intent = new Intent(mActivityContext, NoticeBoardListActivity.class);
                                            mActivityContext.startActivity(intent);

                                            ((NoticeBoardApplication) mAppContext).listenForDataChanges();
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            errorInSyncingData();
                                            progressDialog.dismiss();
                                            Log.e(TAG, "Error in syncing users data : s" + firebaseError.getDetails());
                                        }
                                    });
                                }
                            }
                            if (usernameNotPresent) {
                                progressDialog.dismiss();
                                ToastMaker.createShortToast(R.string.toast_register_first, mActivityContext);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            progressDialog.dismiss();
                            Log.e(TAG, firebaseError.getMessage());
                        }
                    });
                } else {
                    ToastMaker.createShortToast(R.string.toast_internet_connection_error, mActivityContext);
                }
            }
        });
    }

    public void errorInSyncingData() {
        progressDialog.dismiss();
        ToastMaker.createShortToast(R.string.toast_error_in_data_sync, LoginActivity.this);
        AppPreferences.getInstance().setAppOwnerId(0);
        AppPreferences.getInstance().setIsLoggedIn(false);
        SOUser.deleteAll(SOUser.class);
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}

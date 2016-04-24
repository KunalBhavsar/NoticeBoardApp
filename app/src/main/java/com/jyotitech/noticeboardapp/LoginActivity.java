package com.jyotitech.noticeboardapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.model.User;
import com.jyotitech.noticeboardapp.utils.ActivityUtils;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NetworkUtils;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

/**
 * Created by kiran on 22-Apr-16.
 */
public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    SharedPreferences sPref;
    EditText edtUsername;
    EditText edtPassword;
    Button btnLogin;
    Button btnRegister;
    Firebase firebase;
    LoginActivity mActivityContext;
    Context mAppContext;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAppContext = getApplicationContext();
        mActivityContext = this;

        sPref = getSharedPreferences(KeyConstants.SPREF_NAME, Context.MODE_PRIVATE);

        if(sPref.getBoolean(KeyConstants.SPREF_KEY_FOR_USER_LOGGED_IN, false)) {
            Intent intent = new Intent(mActivityContext, NoticeBoardListActivity.class);
            mActivityContext.startActivity(intent);
            finish();
        }

        progressDialog = new ProgressDialog(mActivityContext);
        progressDialog.setMessage(getString(R.string.loading_text));
        progressDialog.setCancelable(false);

        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister = (Button) findViewById(R.id.btn_register);

        firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_USER);

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
                    firebase.orderByChild("username").equalTo(edtUsername.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // do some stuff once
                            boolean usernameNotPresent = true;
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = dataSnapshot.getValue(User.class);
                                usernameNotPresent = false;
                                if (!user.getPassword().equals(edtPassword.getText().toString())) {
                                    ToastMaker.createShortToast(R.string.toast_wrong_login_input, mActivityContext);
                                }
                                else {
                                    SharedPreferences.Editor editor = sPref.edit();
                                    editor.putLong(KeyConstants.SPREF_KEY_APP_OWNER_ID, user.getId());
                                    editor.putString(KeyConstants.SPREF_KEY_FOR_USERNAME, user.getUsername());
                                    editor.putString(KeyConstants.SPREF_KEY_FOR_PASSWORD, user.getPassword());
                                    editor.putString(KeyConstants.SPREF_KEY_FOR_EMAIL, user.getEmail());
                                    editor.putString(KeyConstants.SPREF_KEY_FOR_MOBILE_NUMBER, user.getMobile());
                                    editor.putString(KeyConstants.SPREF_KEY_FOR_FULL_NAME, user.getFullname());
                                    editor.putBoolean(KeyConstants.SPREF_KEY_FOR_USER_LOGGED_IN, true);
                                    editor.apply();

                                    Intent intent = new Intent(mActivityContext, NoticeBoardListActivity.class);
                                    mActivityContext.startActivity(intent);
                                    finish();
                                }
                            }
                            if (usernameNotPresent) {
                                ToastMaker.createShortToast(R.string.toast_register_first, mActivityContext);
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            progressDialog.dismiss();
                            Log.e(TAG, firebaseError.getMessage());
                        }
                    });
                }
                else {
                    ToastMaker.createShortToast(R.string.toast_internet_connection_error, mActivityContext);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}

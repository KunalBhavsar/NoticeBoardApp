package com.noticeboardapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.noticeboardapp.R;
import com.noticeboardapp.model.User;
import com.noticeboardapp.sugar_models.SOUser;
import com.noticeboardapp.utils.ActivityUtils;
import com.noticeboardapp.utils.KeyConstants;
import com.noticeboardapp.utils.NetworkUtils;
import com.noticeboardapp.utils.ToastMaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pinky Walve on 22-Apr-16.
 */
public class RegisterActivity extends OutdatedResourceSubscriberActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    EditText edtFullname;
    EditText edtUsername;
    EditText edtPassword;
    EditText edtConfirmPassword;
    EditText edtMobileNumber;
    EditText edtEmail;
    Button btnRegister;
    RegisterActivity mActivityContext;
    Context mAppContext;
    ProgressDialog progressDialog;
    Firebase firebase;
    boolean editUserProfile;
    SOUser appOwner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mActivityContext = this;
        mAppContext = getApplicationContext();
        progressDialog = new ProgressDialog(mActivityContext);
        progressDialog.setMessage(getString(R.string.loading_text));
        progressDialog.setCancelable(false);

        firebase = new Firebase(KeyConstants.FIREBASE_PATH_USER);

        edtFullname = (EditText)findViewById(R.id.edt_name);
        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        edtConfirmPassword =(EditText)findViewById(R.id.edt_confirm_pass);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtMobileNumber = (EditText) findViewById(R.id.edt_mobile);
        btnRegister = (Button) findViewById(R.id.btn_register);

        if(getIntent().hasExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_REGISTER_ACTIVITY)) {
            editUserProfile = getIntent().getBooleanExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_REGISTER_ACTIVITY, false);
            appOwner = SOUser.getAppOwner();
            edtFullname.setText(appOwner.getFullname());
            edtUsername.setText(appOwner.getUsername());
            edtEmail.setText(appOwner.getEmail());
            edtMobileNumber.setText(appOwner.getMobile());
            btnRegister.setText("Update");
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.hideKeyboard(mActivityContext);
                if(validate()) {
                    if(NetworkUtils.isConnectedToInternet(mAppContext)) {
                        progressDialog.show();
                        firebase.orderByChild("id").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                // do some stuff once
                                boolean everythingOk = true;
                                String username = edtUsername.getText().toString();
                                String email = edtEmail.getText().toString();
                                String mobileNumber = edtMobileNumber.getText().toString();
                                if (editUserProfile) {
                                    String key = null;
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        Log.e(TAG, "user id is "+user.getId() + " app owner id "+appOwner.getId());
                                        if (user.getId() == appOwner.getId()) {
                                            key = dataSnapshot.getKey();
                                        } else {
                                            if (user.getUsername().equals(username)) {
                                                ToastMaker.createLongToast(R.string.toast_username_already_in_use, mActivityContext);
                                                everythingOk = false;
                                                break;
                                            }
                                            else if (user.getEmail().equals(email)) {
                                                ToastMaker.createLongToast(R.string.toast_email_already_in_use, mActivityContext);
                                                everythingOk = false;
                                                break;
                                            }
                                            else if (user.getMobile().equals(mobileNumber)) {
                                                ToastMaker.createLongToast(R.string.toast_mobile_already_in_use, mActivityContext);
                                                everythingOk = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (everythingOk && key != null) {
                                        appOwner.setUsername(username);
                                        appOwner.setPassword(edtPassword.getText().toString());
                                        appOwner.setFullname(edtFullname.getText().toString());
                                        appOwner.setMobile(mobileNumber);
                                        appOwner.setEmail(email);
                                        appOwner.update();

                                        Map<String, Object> updateChildrenMap = new HashMap<String, Object>();
                                        updateChildrenMap.put("username", appOwner.getUsername());
                                        updateChildrenMap.put("password", appOwner.getPassword());
                                        updateChildrenMap.put("fullname", appOwner.getFullname());
                                        updateChildrenMap.put("email", appOwner.getEmail());
                                        updateChildrenMap.put("mobile", appOwner.getMobile());

                                        firebase.child(key).updateChildren(updateChildrenMap);

                                        ToastMaker.createShortToast(R.string.toast_profile_updated_successfully, mActivityContext);
                                        progressDialog.dismiss();
                                        mActivityContext.finish();
                                    }
                                    else {
                                        progressDialog.dismiss();
                                        ToastMaker.createShortToast(R.string.toast_error_in_data_sync, mActivityContext);
                                    }
                                }
                                else {
                                    long lastUserId = 0;
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (user.getUsername().equals(username)) {
                                            ToastMaker.createLongToast(R.string.toast_username_already_in_use, mActivityContext);
                                            everythingOk = false;
                                            break;
                                        } else if (user.getEmail().equals(email)) {
                                            ToastMaker.createLongToast(R.string.toast_email_already_in_use, mActivityContext);
                                            everythingOk = false;
                                            break;
                                        } else if (user.getMobile().equals(mobileNumber)) {
                                            ToastMaker.createLongToast(R.string.toast_mobile_already_in_use, mActivityContext);
                                            everythingOk = false;
                                            break;
                                        }
                                        lastUserId = user.getId();
                                    }
                                    if (everythingOk) {
                                        User user = new User();
                                        user.setId(++lastUserId);
                                        user.setUsername(username);
                                        user.setPassword(edtPassword.getText().toString());
                                        user.setFullname(edtFullname.getText().toString());
                                        user.setMobile(mobileNumber);
                                        user.setEmail(email);
                                        firebase.push().setValue(user);
                                        ToastMaker.createShortToast(R.string.toast_register_successfully, mActivityContext);
                                        mActivityContext.finish();
                                    }
                                    progressDialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                progressDialog.dismiss();
                                Log.e(TAG, firebaseError.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    private boolean validate() {
        boolean validated = true;
        if(edtFullname.getText().toString().trim().isEmpty()) {
            edtFullname.setError(getString(R.string.error_enter_fullname));
            validated = false;
        }
        if(edtUsername.getText().toString().trim().isEmpty()) {
            edtUsername.setError(getString(R.string.error_enter_username));
            validated = false;
        }
        if(edtPassword.getText().toString().trim().isEmpty()) {
            edtPassword.setError(getString(R.string.error_enter_password));
            validated = false;
        }
        if(edtConfirmPassword.getText().toString().trim().isEmpty()) {
            edtConfirmPassword.setError(getString(R.string.error_enter_confirm_password));
            validated = false;
        }
        String mobileNumber = edtMobileNumber.getText().toString();
        if(mobileNumber.trim().isEmpty()) {
            edtMobileNumber.setError(getString(R.string.error_enter_mobile_number));
            validated = false;
        }
        if(mobileNumber.length() != 10) {
            edtMobileNumber.setError(getString(R.string.error_enter_proper_mobile_number));
            validated = false;
        }
        String email = edtEmail.getText().toString();
        if(email.trim().isEmpty()) {
            edtEmail.setError(getString(R.string.error_enter_email));
            validated = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.error_enter_proper_email));
            validated = false;
        }
        if(validated && !edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
            ToastMaker.createShortToast(R.string.error_password_mismatch, mActivityContext);
            validated = false;
        }
        return validated;
    }

    @Override
    protected void onDestroy() {
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}

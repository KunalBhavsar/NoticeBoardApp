package com.jyotitech.noticeboardapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.User;
import com.jyotitech.noticeboardapp.utils.ActivityUtils;
import com.jyotitech.noticeboardapp.utils.KeyConstants;
import com.jyotitech.noticeboardapp.utils.NetworkUtils;
import com.jyotitech.noticeboardapp.utils.ToastMaker;

/**
 * Created by kiran on 22-Apr-16.
 */
public class RegisterActivity extends AppCompatActivity {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mActivityContext = this;
        mAppContext = getApplicationContext();
        progressDialog = new ProgressDialog(mActivityContext);
        progressDialog.setMessage(getString(R.string.loading_text));
        progressDialog.setCancelable(false);

        firebase = new Firebase(KeyConstants.FIREBASE_RESOURCE_USER);

        edtFullname = (EditText)findViewById(R.id.edt_name);
        edtUsername = (EditText) findViewById(R.id.edt_username);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        edtConfirmPassword =(EditText)findViewById(R.id.edt_confirm_pass);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtMobileNumber = (EditText) findViewById(R.id.edt_mobile);
        btnRegister = (Button) findViewById(R.id.btn_register);

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
                                long lastUserId = 0;
                                String username = edtUsername.getText().toString();
                                String email = edtEmail.getText().toString();
                                String mobileNumber = edtMobileNumber.getText().toString();
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

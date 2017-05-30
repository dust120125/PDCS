package tw.idv.poipoi.pdcs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tw.idv.poipoi.pdcs.user.Response;
import tw.idv.poipoi.pdcs.user.User;
import tw.idv.poipoi.pdcs.user.UserCallbacks;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements UserCallbacks{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserRegisterTask mRegTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Thread", "LoginAcivity: " + Thread.currentThread().getId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        mUser = User.getInstance();
        mUser.addListener(this);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        showProgress(true);

        if (!mUser.isLogin()) {
            if (!mUser.isCheckedLogin()) {
                if (!mUser.checkLogin()){
                    showProgress(false);
                }
            }
        } else {
            loginSuccess();
        }
    }

    private void showRegisterDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setTitle("提醒");
        alert.setMessage("確定要註冊?");
        alert.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptRegister();
            }
        });
        alert.show();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null || !checkInput()) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask(email, password);
        mAuthTask.execute((Void) null);
    }

    private void attemptRegister(){
        if (mRegTask != null || !checkInput()) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        showProgress(true);
        mRegTask = new UserRegisterTask(email, password);
        mRegTask.execute();
    }

    private boolean checkInput(){

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void registerSuccess(){
        Toast.makeText(this, "註冊成功！",  Toast.LENGTH_LONG).show();
    }

    private void loginSuccess(){
        //Toast.makeText(this, "登入成功！",  Toast.LENGTH_LONG).show();
        mUser.removeListener(this);
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onLogin() {
        loginSuccess();
    }

    @Override
    public void onLoginFail(String error) {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onReceive(Response response) {

    }

    @Override
    public void onCheckedLogin(boolean login) {
        if (!login) {
            showProgress(false);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            return User.getInstance().login(mEmail, mPassword, Core.android_id);
        }

        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            showProgress(false);
            if (result == null){
                Toast.makeText(LoginActivity.this, "伺服器沒有回應，請稍後再試", Toast.LENGTH_LONG).show();
                return;
            }

            if (result.startsWith(User.LOGIN_OR_REGISTER_SUCCESS)) {
                loginSuccess();
            } else {
                if (result.startsWith("error")){
                    String errCode = result.split(":")[1].trim();
                    switch (errCode){
                        case User.USER_NO_FOUND:
                            mEmailView.setError(getString(R.string.error_user_no_found));
                            mEmailView.requestFocus();
                            break;
                        case User.EMAIL_INVALID:
                            mEmailView.setError(getString(R.string.error_invalid_email));
                            mEmailView.requestFocus();
                            break;
                        case User.WORNG_PASSWORD:
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                            break;
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        UserRegisterTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            return User.getInstance().register(mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(final String result) {
            mRegTask = null;
            showProgress(false);
            if (result == null){
                Toast.makeText(LoginActivity.this, "伺服器沒有回應，請稍後再試", Toast.LENGTH_LONG).show();
                return;
            }

            if (result.equals(User.LOGIN_OR_REGISTER_SUCCESS)) {
                registerSuccess();
            } else {
                if (result.startsWith("error")){
                    String errCode = result.split(":")[1].trim();
                    switch (errCode){
                        case User.USER_ALREADY_EXISTED:
                            mEmailView.setError(getString(R.string.error_existed_email));
                            mEmailView.requestFocus();
                            break;
                        case User.EMAIL_INVALID:
                            mEmailView.setError(getString(R.string.error_invalid_email));
                            mEmailView.requestFocus();
                            break;
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRegTask = null;
            showProgress(false);
        }
    }
}


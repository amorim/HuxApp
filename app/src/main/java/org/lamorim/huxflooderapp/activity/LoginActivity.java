package org.lamorim.huxflooderapp.activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.utility.ProgressHelper;
import org.lamorim.huxflooderapp.R;

import codetail.graphics.drawables.DrawableHotspotTouch;
import codetail.graphics.drawables.LollipopDrawable;
import codetail.graphics.drawables.LollipopDrawablesCompat;
import cz.msebera.android.httpclient.Header;
public class LoginActivity extends AppCompatActivity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar pb;
    private Button mEmailSignInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("prefDisclaimer", Context.MODE_PRIVATE);
        boolean ok = sp.getBoolean("userDidViewDisclaimer", false);
        if (!ok) {
            startActivity(new Intent(LoginActivity.this, InfoActivity.class));
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        pb = (ProgressBar)findViewById(R.id.progressLogin);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            mEmailSignInButton.setBackground(getDrawable2(R.drawable.shape_green));
        else
            mEmailSignInButton.setBackgroundDrawable(getDrawable2(R.drawable.shape_green));
        mEmailSignInButton.setClickable(true);
        mEmailSignInButton.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) mEmailSignInButton.getBackground()));
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }
    public Drawable getDrawable2(int id){
        return LollipopDrawablesCompat.getDrawable(getResources(), id, getTheme());
    }

    private void attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        final String user = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(user)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mEmailSignInButton.setEnabled(false);
            pb.setVisibility(View.VISIBLE);
            RequestParams rp = new RequestParams();
            rp.add("username", user);
            rp.add("password", password);

            FlooderAPIClient.post("hux/checklogin", rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    String vem, token = "";
                    try {
                        vem = response.getString(0);
                        token = response.getString(2);
                    }
                    catch (Throwable ex) {
                        vem = "erro";
                    }
                    pb.setVisibility(View.INVISIBLE);
                    if (vem.equalsIgnoreCase("ok")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putString("username", user);
                        edit.putString("password", password);
                        edit.putString("auth_token", token);
                        edit.apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        ProgressHelper.showCustomDialog(LoginActivity.this, getString(R.string.warning_credentials_incorrect), getString(R.string.interjection_1), true);
                    }
                    mEmailSignInButton.setEnabled(true);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    pb.setVisibility(View.INVISIBLE);
                    ProgressHelper.showCustomDialog(LoginActivity.this, getString(R.string.warning_connection_failed), getString(R.string.interjection_2), true);
                    mEmailSignInButton.setEnabled(true);
                }
                @Override
                public void onRetry(int retryNo) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), String.format(getString(R.string.trying_to_connect), retryNo), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
            }
    }
}


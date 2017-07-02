package org.lamorim.huxflooderapp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.api.HuxAPIClient;
import org.lamorim.huxflooderapp.adapter.ProblemAutoCompleteAdapter;
import org.lamorim.huxflooderapp.models.Job;
import org.lamorim.huxflooderapp.models.Problem;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.utility.DelayAutoCompleteTextView;
import org.lamorim.huxflooderapp.utility.ProgressHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import codetail.graphics.drawables.DrawableHotspotTouch;
import codetail.graphics.drawables.LollipopDrawable;
import codetail.graphics.drawables.LollipopDrawablesCompat;
import cz.msebera.android.httpclient.Header;

public class CreateTaskActivity extends AppCompatActivity {
    //]ProgressDialog dialog;
    MaterialDialog dialog;
    int subsWanted = 0;
    public static Job insertedJob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbartask);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Button btn = (Button) findViewById(R.id.btnCreateTask);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        btn.setBackground(getDrawable2(R.drawable.shape_red));
        else
            btn.setBackgroundDrawable(getDrawable2(R.drawable.shape_red));
        btn.setClickable(true);
        btn.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) btn.getBackground()));
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/HardGrunge.ttf");
        btn.setTypeface(font);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View vieww = CreateTaskActivity.this.getCurrentFocus();
                if (vieww != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(vieww.getWindowToken(), 0);
                }

                try {
                    TextView txtSubs = (TextView) findViewById(R.id.txtQtde);
                    subsWanted = Integer.parseInt(txtSubs.getText().toString());
                } catch (Throwable ex) {
                    ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.fill_submissions_correctly), getString(R.string.interjection_1), true);
                    return;
                }
                String problem = ((TextView) findViewById(R.id.txtProblem)).getText().toString();
                dialog = new MaterialDialog.Builder(CreateTaskActivity.this)
                        .title(getString(R.string.title_progress_dialog))
                        .content(getString(R.string.message_progress_dialog_create_task))
                        .progress(true, 0)
                        .cancelable(false)
                        .autoDismiss(false)
                        .canceledOnTouchOutside(false)
                        .show();
                HuxAPIClient hux = new HuxAPIClient();
                try {
                    String vemlogo = getQueryHux(problem);
                    String query = URLEncoder.encode(vemlogo, "utf-8");
                    boolean escolhe;
                    try {
                        Integer.parseInt(vemlogo);
                        escolhe = true;
                    }
                    catch (Throwable ex) {
                        escolhe = false;
                    }
                    if (!escolhe) {
                        hux.get("problems?max=10&q=" + query, null, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                if (response.length() > 1) {
                                    dialog.dismiss();
                                    ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.be_more_specific), getString(R.string.interjection_2), true);
                                    return;
                                }
                                if (response.length() == 0) {
                                    dialog.dismiss();
                                    ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.no_problem_match), getString(R.string.interjection_1), true);
                                    return;
                                }
                                try {
                                    int id = response.getJSONObject(0).getInt("id");
                                    SharedPreferences sp = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                                    newJob(sp.getString("username", ""), sp.getString("password", ""), String.valueOf(id), String.valueOf(subsWanted));

                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                    dialog.dismiss();
                                    ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                dialog.dismiss();
                                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                dialog.dismiss();
                                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                            }
                        });
                    }
                    else {
                        hux.get("problems/" + query, null, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    int id = response.getInt("id");
                                    SharedPreferences sp = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                                    newJob(sp.getString("username", ""), sp.getString("password", ""), String.valueOf(id), String.valueOf(subsWanted));

                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                    dialog.dismiss();
                                    ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                    if (errorResponse.getString("status") != null && errorResponse.getString("status").equals("404")) {
                                        dialog.dismiss();
                                        ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.no_problem_match), getString(R.string.interjection_1), true);
                                        return;
                                    }
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                dialog.dismiss();
                                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                dialog.dismiss();
                                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                            }
                        });
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                }
            }
        });
        DelayAutoCompleteTextView txt = (DelayAutoCompleteTextView) findViewById(R.id.txtProblem);
        txt.setAdapter(new ProblemAutoCompleteAdapter(this));
        txt.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        txt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Problem problem = (Problem) adapterView.getItemAtPosition(position);
                DelayAutoCompleteTextView txt2 = (DelayAutoCompleteTextView) findViewById(R.id.txtProblem);
                txt2.setText(problem.getId() + " - " + problem.getName());
                txt2.setSelection(txt2.getText().length());
            }
        });
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public Drawable getDrawable2(int id) {
        return LollipopDrawablesCompat.getDrawable(getResources(), id, getTheme());
    }

    void newJob(String username, String password, String problemID, String desiredSubmissions) {
        RequestParams rp = new RequestParams();
        rp.add("username", username);
        rp.add("password", password);
        rp.add("problemID", problemID);
        rp.add("desiredSubmissions", desiredSubmissions);
        FlooderAPIClient.post("jobs/newJob", rp, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    int codResposta = response.getInt(3);
                    if (codResposta != 5) {
                        dialog.dismiss();
                        ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.server_message) + ": " + response.getString(1), getString(R.string.interjection_1), true);
                        return;
                    } else {
                        final int id = response.getInt(5);
                                Job job = new Job();
                                job.setJobID(id);
                                job.setjobStatus(response.getString(6));
                                job.setDesiredSubmissions(response.getInt(7));
                                job.setSubmissionsUntilNow(response.getInt(8));
                                job.setProblemID(response.getString(9));
                                insertedJob = job;
                        }
                        dialog.dismiss();
                        finish();
                } catch (Exception e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    Log.e("error", errorResponse.toString());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                try {

                    Log.e("error", errorResponse.toString());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
                ProgressHelper.showCustomDialog(CreateTaskActivity.this, getString(R.string.sorry_error), getString(R.string.interjection_1), true);
            }
        });
    }

    public String getQueryHux(String vem) {
        if (vem.contains("-")) {
            String[] arr = vem.split(" - ", 2);
            String oi = "";
            try {
                oi = arr[0];
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            return oi;
        } else {
            int id = 0;
            try {
                id = Integer.parseInt(vem);
                return String.valueOf(id);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return vem;
            }
        }
    }
}

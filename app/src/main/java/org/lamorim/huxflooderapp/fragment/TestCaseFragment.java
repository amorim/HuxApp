package org.lamorim.huxflooderapp.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;


import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.adapter.ProblemAutoCompleteAdapter;
import org.lamorim.huxflooderapp.adapter.TestCaseRecyclerViewAdapter;
import org.lamorim.huxflooderapp.api.HuxAPIClient;
import org.lamorim.huxflooderapp.models.Problem;
import org.lamorim.huxflooderapp.models.TestCase;
import org.lamorim.huxflooderapp.utility.DelayAutoCompleteTextView;
import org.lamorim.huxflooderapp.utility.HttpRequest;
import org.lamorim.huxflooderapp.utility.ProgressHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import codetail.graphics.drawables.DrawableHotspotTouch;
import codetail.graphics.drawables.LollipopDrawable;
import codetail.graphics.drawables.LollipopDrawablesCompat;
import cz.msebera.android.httpclient.Header;


public class TestCaseFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    RecyclerView recycler;
    Button btn;
    PuxaTestCase ptc;
    LinearLayout layoutProgress;
    public static int recyclerPosition = 0;
    ArrayList<TestCase> arrr;

    public TestCaseFragment() {

    }

    public static TestCaseFragment newInstance() {
        TestCaseFragment fragment = new TestCaseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vai = inflater.inflate(R.layout.fragment_test_case, container, false);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null && fab.isShown())
            fab.hide();
        Context c = vai.getContext();
        recycler = (RecyclerView) vai.findViewById(R.id.listTestCase);
        layoutProgress = (LinearLayout) vai.findViewById(R.id.layout_progress_test_case);
        ptc = new PuxaTestCase();
        DelayAutoCompleteTextView txt = (DelayAutoCompleteTextView) vai.findViewById(R.id.txtProblemTestCase);
        txt.setAdapter(new ProblemAutoCompleteAdapter(getContext()));
        txt.setLoadingIndicator(
                (android.widget.ProgressBar) vai.findViewById(R.id.pb_loading_indicator_test_case));
        txt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Problem problem = (Problem) adapterView.getItemAtPosition(position);
                DelayAutoCompleteTextView txt2 = (DelayAutoCompleteTextView) getActivity().findViewById(R.id.txtProblemTestCase);
                txt2.setText(problem.getId() + " - " + problem.getName());
                txt2.setSelection(txt2.getText().length());
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(c));
        btn = (Button) vai.findViewById(R.id.btnGetTestCases);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            btn.setBackground(getDrawable2(R.drawable.shape_red));
        else
            btn.setBackgroundDrawable(getDrawable2(R.drawable.shape_red));
        btn.setClickable(true);
        btn.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) btn.getBackground()));
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HardGrunge.ttf");
        btn.setTypeface(font);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String problem = ((EditText) getActivity().findViewById(R.id.txtProblemTestCase)).getText().toString();
                getActivity().findViewById(R.id.txtProblemTestCase).clearFocus();
                View vieww = getActivity().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(vieww.getWindowToken(), 0);
                TextView txt = (TextView)layoutProgress.findViewById(R.id.txt_progress_test_case);
                txt.setText(getString(R.string.getting_info));
                btn.setEnabled(false);
                recycler.setVisibility(View.GONE);
                layoutProgress.setVisibility(View.VISIBLE);
                HuxAPIClient hux = new HuxAPIClient();
                try {
                    String vemlogo = getQueryHux(problem);
                    String query = URLEncoder.encode(vemlogo, "utf-8");
                    boolean escolhe;
                    try {
                        Integer.parseInt(vemlogo);
                        escolhe = true;
                    } catch (Throwable ex) {
                        escolhe = false;
                    }
                    if (!escolhe) {
                        hux.get("problems?max=10&q=" + query, null, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                if (response.length() > 1) {
                                    layoutProgress.setVisibility(View.GONE);
                                    ProgressHelper.showCustomDialog(getActivity(), getString(R.string.be_more_specific), getString(R.string.interjection_2), true);
                                    btn.setEnabled(true);
                                    return;
                                }
                                if (response.length() == 0) {
                                    layoutProgress.setVisibility(View.GONE);
                                    ProgressHelper.showCustomDialog(getActivity(), getString(R.string.no_problem_match), getString(R.string.interjection_1), true);
                                    btn.setEnabled(true);
                                    return;
                                }
                                try {
                                    int id = response.getJSONObject(0).getInt("id");
                                    ptc = new PuxaTestCase();
                                    ptc.execute(id);
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                    layoutProgress.setVisibility(View.GONE);
                                    ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                    btn.setEnabled(true);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                layoutProgress.setVisibility(View.GONE);
                                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                btn.setEnabled(true);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                layoutProgress.setVisibility(View.GONE);
                                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                btn.setEnabled(true);
                            }
                        });
                    } else {
                        hux.get("problems/" + query, null, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    int id = response.getInt("id");
                                    ptc = new PuxaTestCase();
                                    ptc.execute(id);
                                } catch (Throwable ex) {
                                    btn.setEnabled(true);
                                    ex.printStackTrace();
                                    layoutProgress.setVisibility(View.GONE);
                                    ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                    if (errorResponse.getInt("error") == 404) {
                                        layoutProgress.setVisibility(View.GONE);
                                        ProgressHelper.showCustomDialog(getActivity(), getString(R.string.no_problem_match), getString(R.string.interjection_1), true);
                                        btn.setEnabled(true);
                                        return;
                                    }
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                layoutProgress.setVisibility(View.GONE);
                                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                btn.setEnabled(true);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                try {
                                    Log.e("errozao", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                                layoutProgress.setVisibility(View.GONE);
                                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
                                btn.setEnabled(true);
                            }
                        });
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        });
        return vai;
    }

    public Drawable getDrawable2(int id) {
        return LollipopDrawablesCompat.getDrawable(getResources(), id, getActivity().getTheme());
    }
    @Override
    public void onPause() {
        super.onPause();
        if (!ptc.isCancelled())
            ptc.cancel(true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (item.getItemId() == 1) {
            ClipData clip = ClipData.newPlainText("input", ((TestCaseRecyclerViewAdapter)recycler.getAdapter()).getmValues().get(recyclerPosition).getInput());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), getActivity().getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == 2) {
            ClipData clip = ClipData.newPlainText("output", ((TestCaseRecyclerViewAdapter)recycler.getAdapter()).getmValues().get(recyclerPosition).getOutput());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), getActivity().getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class PuxaTestCase extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            TextView txt = (TextView)layoutProgress.findViewById(R.id.txt_progress_test_case);
            txt.setText(getString(R.string.getting_info));
        }
        @Override
        protected void onProgressUpdate(Integer... vemprogress) {
            int prog = vemprogress[0];
            TextView txt = (TextView)layoutProgress.findViewById(R.id.txt_progress_test_case);
            txt.setText(getStringBySituation(prog));
        }
        @Override
        protected Integer doInBackground(Integer... problemIds) {
            int problemId = problemIds[0];
            SharedPreferences sp = getActivity().getSharedPreferences("credentials", Context.MODE_PRIVATE);
            HashMap<String, String> hm = new HashMap<>();
            hm.put("username", sp.getString("username", ""));
            hm.put("password", sp.getString("password", ""));
            StringBuffer response;
            try {
                publishProgress(1);
                String token = "";
                HttpRequest request = new HttpRequest("https://casaamorim.no-ip.biz:5053/hux/checkLogin");
                String json = request.prepare(HttpRequest.Method.POST).withData(hm).sendAndReadString();
                JSONArray arr = new JSONArray(json);
                if (arr.getString(0).equals("OK"))
                    token = arr.getString(2);
                //region Submete payload pra obter o caso de teste
                int tents = 0;
                while (true) {
                    publishProgress(2);
                    URL agoravai = new URL("https://thehuxley.com/api/v1/user/problems/" + problemId + "/submissions");
                    HttpsURLConnection connection = (HttpsURLConnection) agoravai.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-type", "multipart/form-data; boundary=heheheh");
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                    connection.setRequestProperty("Accept", "application/json, text/plain, */*");
                    String formBoundary = "--heheheh";
                    String payload = "";
                    payload += formBoundary + "\r\n";
                    payload += "Content-Disposition: form-data; name=\"language\"";
                    payload += "\r\n\r\n";
                    payload += "1" + "\r\n" + formBoundary + "\r\n";
                    payload += "Content-Disposition: form-data; name=\"file\"; filename=\"testezao.c\"";
                    payload += "\r\n";
                    payload += "Content-Type: text/plain";
                    payload += "\r\n" + "\r\n";
                    payload += "#include <stdio.h>\r\n" +
                            "main() {\r\n" +
                            "    char str[10000];\r\n" +
                            "    int tam = 0;\r\n" +
                            "    char vem;\r\n" +
                            "    while (scanf(\"%c\", &vem) != EOF) {\r\n" +
                            "        if (vem == '\\n')\r\n" +
                            "        vem = '`';\r\n" +
                            "        if (tam > 9998)\r\n" +
                            "        break;\r\n" +
                            "        str[tam++] = vem;\r\n" +
                            "    }\r\n" +
                            "    str[tam] = '\\0';\r\n" +
                            "    printf(\"%s\", str);\r\n" +
                            "} ";
                    payload += "\r\n";
                    payload += formBoundary + "--\r\n";
                    connection.setRequestProperty("Content-Length", "" + payload.getBytes().length);
                    connection.setDoOutput(true);
                    DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    dataOutputStream.writeBytes(payload);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    if (connection.getResponseCode() == 500) {
                        if (++tents > 20)
                            return 3;
                        publishProgress(3);
                        Thread.sleep(5000);
                        continue;
                    }
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    break;
                }
                //para uso futuro
                JSONObject jsonhux = new JSONObject(response.toString());
                //endregion
                //region Aguarda o servidor avaliar o código malicioso
                int tries = 0;
                JSONArray vemjson = null;
                while (true) {
                    publishProgress(4);
                    if (tries > 20)
                        return 2;
                    HttpRequest req = new HttpRequest("https://www.thehuxley.com/api/v1/submissions?max=1&order=desc&problem=" + problemId + "&sort=submissionDate");
                    String jsonn = req.prepare().withHeaders("Authorization:Bearer " + token).sendAndReadString();
                    vemjson = new JSONArray(jsonn);
                    if (!vemjson.getJSONObject(0).getString("evaluation").equals("WAITING"))
                        break;
                    tries++;
                    publishProgress(5);
                    Thread.sleep(3000);
                }
                publishProgress(6);
                JSONArray testes = vemjson.getJSONObject(0).getJSONArray("testCaseEvaluations");
                //endregion
                ArrayList<TestCase> cases = new ArrayList<>();
                for (int i = 0; i < testes.length(); i++) {
                    TestCase tc = new TestCase();
                    String difff = testes.getJSONObject(i).getString("diff");
                    if (difff.equals("null"))
                        continue;
                    JSONObject diff = new JSONObject(difff);
                    JSONArray linhas = diff.getJSONArray("lines");
                        tc.setInput(TestCase.deserializeInput(linhas.getJSONObject(0).getString("actual")));
                    String possibleOutput = "";
                    for (int j = 0; j < linhas.length(); j++)
                        possibleOutput += linhas.getJSONObject(j).getString("expected") + "\n";
                    tc.setOutput(possibleOutput);
                    tc.setId(testes.getJSONObject(i).getInt("testCaseId"));
                    cases.add(tc);
                }
                arrr = cases;
                return 1;
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer cod) {
            layoutProgress.setVisibility(View.GONE);
            if (cod == 1) {
                if (recycler.getAdapter() == null)
                    recycler.setAdapter(new TestCaseRecyclerViewAdapter(arrr));
                else {
                    ((TestCaseRecyclerViewAdapter) recycler.getAdapter()).setmValues(arrr);
                    recycler.getAdapter().notifyDataSetChanged();
                }
                recycler.scrollToPosition(0);
                recycler.setVisibility(View.VISIBLE);
            } else if (cod == -1)
                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.sorry_error), getString(R.string.interjection_1), true);
            else if (cod == 2)
                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.submission_time_expired), getString(R.string.interjection_1), true);
            else if (cod == 3)
                ProgressHelper.showCustomDialog(getActivity(), getString(R.string.wait_to_submit), getString(R.string.interjection_1), true);
            btn.setEnabled(true);
        }

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

    int getStringBySituation(int situation) {
        switch (situation) {
            case 1:
                return R.string.querying_login_info;
            case 2:
                return R.string.submitting;
            case 3:
                return R.string.waiting_period_not_finished;
            case 4:
                return R.string.waiting_code_evaluation;
            case 5:
                return R.string.evaluation_not_over_yet;
            case 6:
                return R.string.final_data;
        }
        return R.string.querying_login_info;
    }
}

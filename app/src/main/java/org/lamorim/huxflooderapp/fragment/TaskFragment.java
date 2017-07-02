package org.lamorim.huxflooderapp.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.activity.MainActivity;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.activity.CreateTaskActivity;
import org.lamorim.huxflooderapp.models.Job;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.adapter.TaskRecyclerViewAdapter;
import org.lamorim.huxflooderapp.notification.TaskBroadcastReceiver;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TaskFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    public static boolean FRAGMENT_IS_IN_FOREGROUND = true;
    public static boolean didSelectItemInMenu = false;
    public static int recyclerPosition = 0;
    public static MaterialDialog dialog;
    private int mColumnCount = 1;
    private boolean menuClosing = false;
    private OnListFragmentInteractionListener mListener;
    RecyclerView recyclerView;
    LinearLayout layoutempty;
    LinearLayout layoutProgress;
    FloatingActionButton fab;
    String user;
    ProgressBar infinite;
    SwipeRefreshLayout swipe;
    SwipeRefreshLayout swipe2;
    SharedPreferences prefs;
    public static final Handler handler = new Handler();

    public TaskFragment() {
    }

    @SuppressWarnings("unused")
    public static TaskFragment newInstance(int columnCount) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }
    public TaskRecyclerViewAdapter getRecyclerAdapter() {
        if (recyclerView != null && recyclerView.getAdapter() != null)
            return (TaskRecyclerViewAdapter)recyclerView.getAdapter();
        return null;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view3 = inflater.inflate(R.layout.task_fragment, container, false);
        View view2 = view3.findViewById(R.id.swipe);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (!fab.isShown())
            fab.show();
        infinite = (ProgressBar)getActivity().findViewById(R.id.progressInfiniteScrolling);
        View view = view2.findViewById(R.id.list);
        swipe2 = (SwipeRefreshLayout)view3.findViewById(R.id.swipe2);
        layoutempty = (LinearLayout)view3.findViewById(R.id.layout_empty);
        layoutProgress = (LinearLayout)view3.findViewById(R.id.layoutProgress);
        if (view instanceof RecyclerView) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            swipe = (SwipeRefreshLayout)view2;
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            SharedPreferences sp = getActivity().getSharedPreferences("credentials", Context.MODE_PRIVATE);
            user = sp.getString("username", "");
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), CreateTaskActivity.class));
                }
            });
            swipe.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
            swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                        reloadJobs(true);

                }
            });
            swipe2.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
            swipe2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadJobs(true);

                }
            });
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0)
                        fab.hide();
                    else if (dy < 0)
                        fab.show();
                }
            });
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    TaskRecyclerViewAdapter adapter = (TaskRecyclerViewAdapter) recyclerView.getAdapter();
                    if (adapter.getmValues().size() - 1 == llm.findLastCompletelyVisibleItemPosition()) {
                        RequestParams rp = new RequestParams();
                        rp.add("user", user);
                        rp.add("offset", String.valueOf(adapter.getmValues().size()));
                        rp.add("fetchNext", "10");
                        infinite.setVisibility(View.VISIBLE);
                        FlooderAPIClient.post("jobs/getMyJobs", rp, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                Type type = new TypeToken<ArrayList<Job>>() {
                                }.getType();
                                List<Job> nova = new Gson().fromJson(response.toString(), type);
                                for (int i = 0; i < nova.size(); i++) {
                                    ((TaskRecyclerViewAdapter) recyclerView.getAdapter()).getmValues().add(nova.get(i));
                                    recyclerView.getAdapter().notifyItemInserted(((TaskRecyclerViewAdapter) recyclerView.getAdapter()).getmValues().size());
                                }
                                infinite.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                infinite.setVisibility(View.GONE);
                                try {
                                    Log.e("ERROR", errorResponse.toString());
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
            layoutProgress.setVisibility(View.VISIBLE);
        }
        return view3;
    }

    public void reloadJobs(final boolean swipeWork) {
        if (!FRAGMENT_IS_IN_FOREGROUND)
            return;
        if (!prefs.getBoolean("checkbox_auto_update", false) && !swipeWork)
            return;
        if (swipeWork)
            handler.removeCallbacksAndMessages(null);
        RequestParams rp = new RequestParams();
        rp.add("user", user);
        rp.add("offset", "0");
        String fetchNext;
        if (recyclerView.getAdapter() == null)
            fetchNext = "10";
        else
            fetchNext = String.valueOf(((TaskRecyclerViewAdapter) recyclerView.getAdapter()).getmValues().size());
        if (Integer.parseInt(fetchNext) < 10)
            fetchNext = "10";
        rp.add("fetchNext", fetchNext);
        FlooderAPIClient.post("jobs/getMyJobs", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (!FRAGMENT_IS_IN_FOREGROUND)
                    return;

                Type type = new TypeToken<ArrayList<Job>>() {
                }.getType();
                List<Job> lista = new Gson().fromJson(response.toString(), type);
                layoutProgress.setVisibility(View.GONE);
                if (lista.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    swipe2.setVisibility(View.VISIBLE);
                    TextView txt = (TextView)swipe2.findViewById(R.id.txt_empty_view);
                    txt.setText(getString(R.string.no_task));
                    if (!fab.isShown())
                        fab.show();
                    if (swipe.isRefreshing() && swipeWork)
                        swipe.setRefreshing(false);
                    if (swipe2.isRefreshing() && swipeWork)
                        swipe2.setRefreshing(false);
                    handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                reloadJobs(false);
                            }
                        }, 5000);
                    return;
                }
                if (recyclerView.getVisibility() == View.GONE) {
                    swipe2.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
               }
                if (recyclerView.getAdapter() == null)
                    recyclerView.setAdapter(new TaskRecyclerViewAdapter(lista, mListener));
                else {
                    List<Job> antiga = ((TaskRecyclerViewAdapter) recyclerView.getAdapter()).getmValues();
                    ((TaskRecyclerViewAdapter) recyclerView.getAdapter()).setmValues(lista);
                    boolean notifyAdapter = false;
                    if (antiga.size() == lista.size()) {
                        for (int i = 0; i < lista.size(); i++) {
                            if (antiga.get(i).getJobID() != lista.get(i).getJobID()) {
                                notifyAdapter = true;
                                break;
                            }
                        }
                        if (!notifyAdapter) {
                            for (int i = 0; i < lista.size(); i++) {
                                if (!antiga.get(i).equals(lista.get(i)))
                                    recyclerView.getAdapter().notifyItemChanged(i);
                            }
                        }
                    }
                    else
                        notifyAdapter = true;
                    if (notifyAdapter)
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
                if (!fab.isShown() && !(recyclerView.computeHorizontalScrollRange() > recyclerView.getWidth() || recyclerView.computeVerticalScrollRange() > recyclerView.getHeight()))
                    fab.show();
                if (swipe.isRefreshing())
                    swipe.setRefreshing(false);
                if (swipe2.isRefreshing())
                    swipe2.setRefreshing(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reloadJobs(false);
                        }
                    }, 5000);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (!FRAGMENT_IS_IN_FOREGROUND)
                    return;
                try {
                    Log.e("ERROR", errorResponse.toString());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                Activity activity = getActivity();
                if(activity != null && isAdded()) {
                    if (layoutProgress.getVisibility() == View.VISIBLE) {
                        layoutProgress.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        TextView txt = (TextView) swipe2.findViewById(R.id.txt_empty_view);
                        int resource;
                        if (prefs.getBoolean("checkbox_auto_update", false))
                            resource = R.string.no_connection_auto;
                        else
                            resource = R.string.no_connection_pull;
                        txt.setText(getString(resource));
                        swipe2.setVisibility(View.VISIBLE);

                    }
                    if (swipe.isRefreshing())
                        swipe.setRefreshing(false);
                    if (swipe2.isRefreshing())
                        swipe2.setRefreshing(false);
                }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reloadJobs(false);
                        }
                    }, 5000);

            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        FRAGMENT_IS_IN_FOREGROUND = true;
        if (CreateTaskActivity.insertedJob != null) {
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(new TaskRecyclerViewAdapter(new ArrayList<Job>(), mListener));
            }
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
                swipe2.setVisibility(View.GONE);
            }
            ((TaskRecyclerViewAdapter)recyclerView.getAdapter()).getmValues().add(0, CreateTaskActivity.insertedJob);
            CreateTaskActivity.insertedJob = null;
            recyclerView.getAdapter().notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
        }
        handler.removeCallbacksAndMessages(null);
        reloadJobs(true);
    }
    @Override
    public void onPause() {
        super.onPause();
        infinite.setVisibility(View.GONE);
        FRAGMENT_IS_IN_FOREGROUND = false;
        handler.removeCallbacksAndMessages(null);
        FlooderAPIClient.client.cancelRequests(getContext(), true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " tem que implementar OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        didSelectItemInMenu = true;
        if (item.getItemId() == 4) {
            FRAGMENT_IS_IN_FOREGROUND = true;
            restartFetch();
            return super.onContextItemSelected(item);
        }
        if (item.getItemId() != 3)
        dialog = new MaterialDialog.Builder(getActivity())
                .content(getString(R.string.title_progress_dialog))
                .progress(true, 0)
                .cancelable(false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .show();
        Job job = ((TaskRecyclerViewAdapter)recyclerView.getAdapter()).getmValues().get(recyclerPosition);
        switch (item.getItemId()) {
            case 1:
                job.setSubmissionsUntilNow(0);
                job.setjobStatus("pending");
                job.updateSelf(this);
                break;
            case 2:
                job.deleteSelf(this);
                break;
            case 3:
                job.setPendingCancellation(true);
                job.cancelSelf(this);
                recyclerView.getAdapter().notifyItemChanged(recyclerPosition);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void onContextMenuClosed(Menu menu) {
        if (!didSelectItemInMenu) {
            FRAGMENT_IS_IN_FOREGROUND = true;
            restartFetch();
        }
    }

    public void restartFetch() {
        FRAGMENT_IS_IN_FOREGROUND = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadJobs(false);
            }
        }, 5000);
    }
    public void changeToEmptyView() {
        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            swipe2.setVisibility(View.VISIBLE);
        }
    }
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Job item);
    }
}

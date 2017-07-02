package org.lamorim.huxflooderapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.adapter.ServerRecyclerViewAdapter;
import org.lamorim.huxflooderapp.models.Server;
import org.lamorim.huxflooderapp.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by lucas on 22/12/2016.
 */

public class ServerFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private ServerFragment.OnListFragmentInteractionListener mListener;
    RecyclerView recyclerView;
    LinearLayout layoutempty;
    LinearLayout layoutProgress;
    FloatingActionButton fab;
    SwipeRefreshLayout swipe;
    SwipeRefreshLayout swipe2;
    public ServerFragment() {
    }

    @SuppressWarnings("unused")
    public static ServerFragment newInstance(int columnCount) {
        ServerFragment fragment = new ServerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
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
        View view3 = inflater.inflate(R.layout.server_fragment, container, false);
        View view2 = view3.findViewById(R.id.swipeServer);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null && fab.isShown())
            fab.hide();
        View view = view2.findViewById(R.id.listServer);
        layoutempty = (LinearLayout)view3.findViewById(R.id.layout_empty_server);
        layoutProgress = (LinearLayout)view3.findViewById(R.id.layoutProgressServer);
        if (view instanceof RecyclerView) {
            swipe = (SwipeRefreshLayout)view2;
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            swipe.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
            swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadServers();

                }
            });
            swipe2 = (SwipeRefreshLayout)view3.findViewById(R.id.swipeServer2);
            swipe2.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
            swipe2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadServers();

                }
            });
            layoutProgress.setVisibility(View.VISIBLE);
        }
        return view3;
    }

    public void reloadServers() {
        FlooderAPIClient.get("servers/getServers", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Type type = new TypeToken<ArrayList<Server>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create();

                List<Server> lista = gson.fromJson(response.toString(), type);
                layoutProgress.setVisibility(View.GONE);
                if (lista.size() == 0) {
                    swipe.setVisibility(View.GONE);
                    swipe2.setVisibility(View.VISIBLE);
                    TextView txt = (TextView)swipe2.findViewById(R.id.txt_empty_view_server);
                    txt.setText(getString(R.string.no_server));
                    if (swipe.isRefreshing())
                        swipe.setRefreshing(false);
                    if (swipe2.isRefreshing())
                        swipe2.setRefreshing(false);
                    return;
                }
                if (swipe.getVisibility() == View.GONE) {
                    swipe2.setVisibility(View.GONE);
                    swipe.setVisibility(View.VISIBLE);
                }
                if (recyclerView.getAdapter() == null)
                    recyclerView.setAdapter(new ServerRecyclerViewAdapter(lista, mListener));
                else {
                    ((ServerRecyclerViewAdapter) recyclerView.getAdapter()).setmValues(lista);
                        recyclerView.getAdapter().notifyDataSetChanged();
                }
                if (swipe.isRefreshing())
                    swipe.setRefreshing(false);
                if (swipe2.isRefreshing())
                    swipe2.setRefreshing(false);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    Log.e("ERROR", errorResponse.toString());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                Activity activity = getActivity();
                if(activity != null && isAdded()) {
                    if (layoutProgress.getVisibility() == View.VISIBLE) {
                        layoutProgress.setVisibility(View.GONE);
                        swipe.setVisibility(View.GONE);
                        TextView txt = (TextView) swipe2.findViewById(R.id.txt_empty_view_server);
                        txt.setText(getString(R.string.no_connection_pull));
                        swipe2.setVisibility(View.VISIBLE);

                    }
                    if (swipe.isRefreshing())
                        swipe.setRefreshing(false);
                    if (swipe2.isRefreshing())
                        swipe2.setRefreshing(false);
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        reloadServers();
    }
    @Override
    public void onPause() {
        super.onPause();
        FlooderAPIClient.client.cancelRequests(getContext(), true);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ServerFragment.OnListFragmentInteractionListener) {
            mListener = (ServerFragment.OnListFragmentInteractionListener) context;
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
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Server item);
    }
}

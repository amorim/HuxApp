package org.lamorim.huxflooderapp.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.api.HuxAPIClient;
import org.lamorim.huxflooderapp.fragment.ServerFragment;
import org.lamorim.huxflooderapp.fragment.TestCaseFragment;
import org.lamorim.huxflooderapp.models.Job;
import org.lamorim.huxflooderapp.models.Server;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.fragment.TaskFragment;
import org.lamorim.huxflooderapp.notification.TaskBroadcastReceiver;
import org.lamorim.huxflooderapp.preferences.PrefsFragment;

import javax.net.ssl.SSLEngine;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpResponseException;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskFragment.OnListFragmentInteractionListener, ServerFragment.OnListFragmentInteractionListener, TestCaseFragment.OnFragmentInteractionListener {
    public static int selectedFragment;
    Fragment fragment;
    public static boolean IS_FOREGROUND = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        if (!sp.contains("username") || !sp.contains("password")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            super.onCreate(savedInstanceState);
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        IS_FOREGROUND = true;
        fragment = null;
        Class fragmentClass;
        fragmentClass = TaskFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
        selectedFragment = R.id.nav_flood;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_flood);
        setTitle(navigationView.getMenu().getItem(0).getTitle());
        View header = navigationView.getHeaderView(0);
        ImageView img = (ImageView)header.findViewById(R.id.imgExit);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                SharedPreferences sp = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("username");
                editor.remove("password");
                editor.apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
        loadUserInfoOnDrawer(sp, false);
    }
    @Override
    public void onPause() {
        super.onPause();
        IS_FOREGROUND = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("checkbox_notification", false))
        TaskBroadcastReceiver.setupAlarm(this);
        else
            TaskBroadcastReceiver.cancelAlarm(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        IS_FOREGROUND = false;
    }
    @Override
    public void onResume() {
        super.onResume();
        IS_FOREGROUND = true;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        fragment = null;
        Class fragmentClass;
        if (item.getItemId() == selectedFragment) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else
        selectedFragment = item.getItemId();
        switch (item.getItemId()) {
            case R.id.nav_flood:
                fragmentClass = TaskFragment.class;
                break;
            case R.id.nav_servers:
                fragmentClass = ServerFragment.class;
                break;
            case R.id.nav_preferences:
                fragmentClass = PrefsFragment.class;
                break;
            case R.id.nav_testcase:
                fragmentClass = TestCaseFragment.class;
                break;
            default:
                fragmentClass = TaskFragment.class;
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
        item.setChecked(true);
        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressWarnings("deprecation")
    void loadUserInfoOnDrawer(final SharedPreferences sp, final boolean didRetryToken) {
        String authorization = "Bearer " + sp.getString("auth_token", "");
        HuxAPIClient huxapi = new HuxAPIClient(authorization);
        huxapi.get("user", new RequestParams("", ""), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String username = "", avatar = "", institution = "";
                try {
                    username = response.getString("name");
                    avatar = response.getString("avatar");
                    institution = response.getJSONObject("institution").getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String escapado = TextUtils.htmlEncode(username);
                String userrrr = String.format(getString(R.string.user_greeting), escapado);
                CharSequence userfinalzao;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    userfinalzao = Html.fromHtml(userrrr, Html.FROM_HTML_MODE_LEGACY);
                 else
                    userfinalzao = Html.fromHtml(userrrr);
                CircleImageView circavatar = (CircleImageView) findViewById(R.id.profile_image);
                ((TextView) findViewById(R.id.username_txtview)).setText(userfinalzao);
                ((TextView) findViewById(R.id.institution_txtview)).setText(institution);
                Picasso.with(MainActivity.this).load(avatar).noFade().placeholder(R.drawable.hux_default).error(R.drawable.hux_default).into(circavatar);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                try {
                    Log.e("ERROR: ", responseString);
                    if (throwable instanceof HttpResponseException && ((HttpResponseException)throwable).getStatusCode() == 401) {
                        Log.e("info:", "tentando um token novamente");
                        if (!didRetryToken) {
                            tryToken(sp);
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }

        });


    }

    public void tryToken(final SharedPreferences sp) {
        RequestParams rp = new RequestParams();
        rp.add("username", sp.getString("username", ""));
        rp.add("password", sp.getString("password", ""));
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
                if (vem.equalsIgnoreCase("ok")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("auth_token", token);
                    edit.apply();
                    loadUserInfoOnDrawer(sp, true);
                }
                else {
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage(getString(R.string.error_invalid_credentials));
                    dlgAlert.setTitle(getString(R.string.interjection_1));
                    dlgAlert.setIcon(R.mipmap.ic_warning);
                    dlgAlert.setCancelable(false);
                    dlgAlert.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    finish();
                                }
                            });
                }
            }
        });

    }
    @Override
    public void onListFragmentInteraction(Job item) {

    }
    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
            if (fragment instanceof TaskFragment)
            ((TaskFragment)fragment).onContextMenuClosed(menu);
    }

    @Override
    public void onListFragmentInteraction(Server item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

package org.lamorim.huxflooderapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import org.lamorim.huxflooderapp.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        WebView wv = (WebView)findViewById(R.id.disclaimer);
        wv.setVerticalScrollBarEnabled(true);
        wv.setBackgroundColor(Color.TRANSPARENT);
        String html = "<html><body><p align=\"justify\">";
        html+=getResources().getString(R.string.disclaimer_userpass_huxley);
        html+="</p></body></html>";
        wv.loadData(html, "text/html; charset=utf-8", "utf-8");

        Button butao = (Button)findViewById(R.id.butaoEntrar);
        butao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("prefDisclaimer", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("userDidViewDisclaimer", true);
                edit.apply();
                startActivity(new Intent(InfoActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}

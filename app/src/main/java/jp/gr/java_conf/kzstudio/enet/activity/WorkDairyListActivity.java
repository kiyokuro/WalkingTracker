package jp.gr.java_conf.kzstudio.enet.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.UserPreference;

/**
 * Created by kiyokazu on 2016/10/27.
 */

public class WorkDairyListActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;

    private Button mInputWorkDiaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workdairy_list);
        setTitle("作業日誌");

        mInputWorkDiaryButton = (Button)findViewById(R.id.input_dairy);
        mInputWorkDiaryButton.setOnClickListener(this);

        webView = (WebView)findViewById(R.id.web_view_field_list);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAppCachePath("/WalkingTracker");
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient() {
            //ページの読み込み開始
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                findViewById(R.id.loadview).setVisibility(View.VISIBLE);
            }

            //ページの読み込み完了
            @Override
            public void onPageFinished(WebView view, String url) {
                findViewById(R.id.loadview).setVisibility(view.GONE);
            }

            //ページの読み込み失敗
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });
        UserPreference userPreference = new UserPreference(this, "UserPref");
        String userName = userPreference.loadUserPreference("USER_ID");
        String passWord = userPreference.loadUserPreference("USER_PASSWORD");
        webView.loadUrl("https://project-one.sakura.ne.jp/app/diary.php?username="+userName);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.input_dairy:
                Intent intent = new Intent(this, WorkDairyInputActivity.class);
                startActivity(intent);
                break;
        }
    }
}

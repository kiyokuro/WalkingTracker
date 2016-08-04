package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.JsonParser;

/**
 * Created by kiyokazu on 16/08/04.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    private  Button mLoginButton;
    LinearLayout mLoginLayout;

    private InputMethodManager inputMethodManager;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mContext = this;
        mLoginLayout = (LinearLayout)findViewById(R.id.login_layout);
        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.login_button:
                intent = new Intent(this,HomeActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void apiForLogin() {
        String url = "http://project-one.sakura.ne.jp/app/system/ajax.login.php";
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗
                new AlertDialog.Builder(mContext)
                        .setTitle("リトライ")
                        .setMessage("情報を再取得しますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                apiForLogin();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                findViewById(R.id.loadview).setVisibility(View.GONE);
                                Toast.makeText(mContext, "ログインできませんでした。", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", "");
                params.put("password", "");
                return params;
            }
        };
        requestQueue.add(jsonObjReq);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mLoginLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mLoginLayout.requestFocus();

        return false;
    }
}

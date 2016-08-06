package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.UserPreference;

/**
 * Created by kiyokazu on 16/08/04.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    private EditText mUserId;
    private EditText mUserPassword;
    private  Button mLoginButton;
    LinearLayout mLoginLayout;

    private InputMethodManager inputMethodManager;
    private Context mContext;
    UserPreference mUserPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mContext = this;
        mLoginLayout = (LinearLayout)findViewById(R.id.login_layout);
        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mUserId = (EditText)findViewById(R.id.user_id);
        mUserPassword = (EditText)findViewById(R.id.user_password);

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        mUserPreference = new UserPreference(mContext, "UserPref");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                String userId = mUserId.getText().toString();
                String userPassword = mUserPassword.getText().toString();
                apiForLogin(userId, userPassword);
                break;
        }
    }

    private void apiForLogin(final String userId, final String userPassword) {
        String url = "http://project-one.sakura.ne.jp/app/system/ajax.login.php";
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        StringRequest jsonObjReq = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    //通信成功
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("OK")){
                            String[] keys = {"USER_ID", "USER_PASSWORD"};
                            String[] values = {userId, userPassword};

                            mUserPreference.saveUserPreference(keys, values);
                            changeActivity();
                        }else {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("ログインできません")
                                    .setMessage("入力情報を確認してください")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @TargetApi(Build.VERSION_CODES.M)
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗
                new AlertDialog.Builder(mContext)
                        .setTitle("リトライ")
                        .setMessage("通信に失敗しました。情報を再取得しますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                apiForLogin(userId, userPassword);
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                params.put("username", userId);
                params.put("password", userPassword);
                return params;
            }
        };
        requestQueue.add(jsonObjReq);
    }

    private void changeActivity(){
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mLoginLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mLoginLayout.requestFocus();

        return false;
    }
}

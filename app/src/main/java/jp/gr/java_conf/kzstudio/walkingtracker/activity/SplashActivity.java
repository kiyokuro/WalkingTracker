package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.LodingIndicator;
import jp.gr.java_conf.kzstudio.walkingtracker.util.UserPreference;

public class SplashActivity extends Activity {

	private Context mContext;

	UserPreference mUserPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

		mContext = this;
		mUserPreference = new UserPreference(mContext, "UserPref");
		if(autoLogin()){
			apiForLogin(mUserPreference.loadUserPreference("USER_ID"), mUserPreference.loadUserPreference("USER_PASSWORD"));
		}else {
			moveLoginActivity();
		}
    }
    
    private void moveLoginActivity(){
    	Timer timer = new Timer();
		TimerTask myTask;

	   		myTask = new TimerTask(){
	   			public void run(){
	   				LodingIndicator.hideLoading();
					Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
	   		    	startActivity(intent);
	   		    	finish();
	   			}
	   		};
	   	timer.schedule(myTask, 2000);
    }

	private void changeActivity(){
		Timer timer = new Timer();
		TimerTask myTask;

		myTask = new TimerTask(){
			public void run(){
				LodingIndicator.hideLoading();
				Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
				startActivity(intent);
				finish();
			}
		};
		timer.schedule(myTask, 1500);
	}

	private void apiForLogin(final String userId, final String userPassword) {
		String url = "https://project-one.sakura.ne.jp/app/system/ajax.login.php";
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
						.setMessage("情報を再取得しますか？")
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

	private boolean autoLogin(){
		String userId = mUserPreference.loadUserPreference("USER_ID");
		String userPassword = mUserPreference.loadUserPreference("USER_PASSWORD");
		if(!userId.equals("") && !userPassword.equals("")){
			return true;
		}
		return false;
	}
}
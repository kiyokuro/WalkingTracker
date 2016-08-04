package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.LodingIndicator;

public class SplashActivity extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        moveLoginActivity();
    }
    
    private void moveLoginActivity(){
    	Timer timer = new Timer();
		TimerTask myTask;

	   		myTask = new TimerTask(){
	   			public void run(){
	   				LodingIndicator.hideLoading();
	   		    	startActivity(new Intent(SplashActivity.this, LoginActivity.class));
	   		    	finish();
	   			}
	   		};
	   	timer.schedule(myTask, 1000);
    }
    
  	
}
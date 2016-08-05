package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import jp.gr.java_conf.kzstudio.walkingtracker.R;

/**
 * Created by kiyokazu on 16/08/04.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mFieldList;
    private Button mWeatherButton;
    private Button mWalkingTrackButton;
    private Button mSettingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ホーム");

        mFieldList = (Button)findViewById(R.id.field_list_button);
        mFieldList.setOnClickListener(this);
        mWeatherButton = (Button)findViewById(R.id.weather_button);
        mWeatherButton.setOnClickListener(this);
        mWalkingTrackButton = (Button)findViewById(R.id.walking_track_button);
        mWalkingTrackButton.setOnClickListener(this);
        mSettingButton = (Button)findViewById(R.id.setting_button);
        mSettingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.field_list_button:
                break;
            case R.id.weather_button:
                break;
            case R.id.walking_track_button:
                intent = new Intent(this,FunctionHomeActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_button:
                intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(this)
                    .setTitle("e-Net cssの終了")
                    .setMessage("e-Net cssを終了してよろしいですか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ

                        }
                    })
                    .show();

            return true;
        }
        return false;
    }
}

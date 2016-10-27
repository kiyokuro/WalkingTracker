package jp.gr.java_conf.kzstudio.enet.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.UserPreference;

/**
 * Created by kiyokazu on 16/08/05.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    Button mLogoutButton;

    UserPreference mPreference;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("設定");

        mContext = this;
        mPreference = new UserPreference(this, "UserPref");

        mLogoutButton = (Button)findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout_button:
                new AlertDialog.Builder(mContext)
                        .setTitle("ログアウト")
                        .setMessage("ログアウトしますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
        }
    }

    private void logout(){
        //ログアウトしたら端末内のユーザIDとユーザパスワードを消す
        String[] keys = {"USER_ID", "USER_PASSWORD"};
        String[] values = {"", ""};
        mPreference.saveUserPreference(keys, values);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

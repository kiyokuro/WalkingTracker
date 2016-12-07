package jp.gr.java_conf.kzstudio.enet.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.CalenderDate;
import jp.gr.java_conf.kzstudio.enet.util.JsonParser;
import jp.gr.java_conf.kzstudio.enet.util.UserPreference;

/**
 * Created by kiyokazu on 2016/12/05.
 */

public class WorkContentsInsert extends AppCompatActivity implements View.OnClickListener {

    EditText mWokerName;
    EditText mFieldName;
    EditText mContents;
    Button mRegistButton;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_contents_insert);
        setTitle("新規業務登録");

        mWokerName = (EditText)findViewById(R.id.worker_name);
        mFieldName = (EditText)findViewById(R.id.field_name);
        mContents = (EditText)findViewById(R.id.work_contents);
        mRegistButton = (Button)findViewById(R.id.end_insert);
        mRegistButton.setOnClickListener(this);

        mContext = this;
    }

    private void endActivity(){
        Intent intent = new Intent(this,WorkContentsCheckList.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //登録ボタンをタップ。未入力項目があると通らない
            case R.id.end_insert:
                if(mWokerName.getText().toString().length() == 0 || mFieldName.getText().toString().length() == 0 || mContents.getText().toString().length() == 0){
                    new AlertDialog.Builder(mContext)
                            .setTitle("未入力の項目があります")
                            .setMessage("全ての項目を記入してください。")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else {
                    CalenderDate calenderDate = new CalenderDate();
                    String fixDay;
                    String fixMonth;
                    if(calenderDate.getTodayDate()[1].length() == 1){
                        fixMonth = "0"+calenderDate.getTodayDate()[1];
                    }else {
                        fixMonth = calenderDate.getTodayDate()[1];
                    }
                    if(calenderDate.getTodayDate()[2].length() == 1){
                        fixDay = "0"+calenderDate.getTodayDate()[2];
                    }else {
                        fixDay = calenderDate.getTodayDate()[2];
                    }

                    UserPreference userPreference = new UserPreference(mContext, "UserPref");
                    String companyCode = userPreference.loadUserPreference("COMPANY_CODE");
                    insertNewWork(mWokerName.getText().toString(), mFieldName.getText().toString(),
                            mContents.getText().toString(),calenderDate.getTodayDate()[0]+fixMonth+fixDay, companyCode);
                }
                break;
        }
    }

    /**
     * 作業の登録処理
     * @param workerName EditTextからの作業者名
     * @param fieldName EditTextからの水田名
     * @param contents EditTextからの作業内容
     * @param date 作業日（自動取得）
     */
    private void insertNewWork(final String workerName, final String fieldName, final String contents, final String date, final String companyCode) {

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        StringRequest strReq = new StringRequest(
                Request.Method.POST,
                "http://project-one.sakura.ne.jp/e-net_api/InsertWorkList.php",
                new Response.Listener<String>() {
                    //通信成功
                    @Override
                    public void onResponse(String response) {
                        //Log.i("dddd", response);

                        endActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 通信失敗
                        new AlertDialog.Builder(mContext)
                                .setTitle("情報の登録に失敗しました")
                                .setMessage("ネットワークを確認してください。")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        insertNewWork(workerName,fieldName,contents,date,companyCode);
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(mContext, "登録できませんでした", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("worker_name", workerName);
                params.put("field_name", fieldName);
                params.put("contents", contents);
                params.put("date", date);
                params.put("company_code", companyCode);
                return params;
            }
        };
        requestQueue.add(strReq);
    }
}

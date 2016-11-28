package jp.gr.java_conf.kzstudio.enet.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.CalenderDate;
import jp.gr.java_conf.kzstudio.enet.util.JsonParser;
import jp.gr.java_conf.kzstudio.enet.util.UserPreference;


/**
 * Created by kiyokazu on 2016/11/08.
 */

public class WorkContentsCheckList extends AppCompatActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener{

    Button mCalenderButton;

    CalenderDate mCalenderDate;
    DatePickerDialog datePickerDialog;

    Context mContext;
    String[] mDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_contents_check_list);
        setTitle("業務リスト");

        mCalenderButton = (Button)findViewById(R.id.culender);
        assert mCalenderButton != null;
        mCalenderButton.setOnClickListener(this);

        mContext = this;
        mCalenderDate = new CalenderDate();
        mDate = mCalenderDate.getTodayDate();

        //初期の日付をセット
        mCalenderButton.setText(mDate[1]+"月"+mDate[2]+"日");

        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        //ユーザのcompanyCodeを取得し会社の作業を取得
        getCompanyCode(userPreference.loadUserPreference("USER_ID"));
    }

    private void showWorkContents(ArrayList<WorkContents> workContentsList){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //datepickerを表示
            case R.id.culender:
                datePickerDialog = new DatePickerDialog(this,this,Integer.parseInt(mDate[0]),Integer.parseInt(mDate[1])-1,Integer.parseInt(mDate[2]));
                datePickerDialog.show();
                break;
        }
    }

    /**
     * datepickerのリスナー
     * @param view
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mDate[0] = String.valueOf(year);
        mDate[1] = String.valueOf(monthOfYear+1);
        mDate[2] = String.valueOf(dayOfMonth);
        mCalenderButton.setText(mDate[1]+"月"+mDate[2]+"日");
    }


    /**
     * userIdからCompanyCodeを取得
     * @param userId
     */
    private void getCompanyCode(final String userId){

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.GET,
                "http://project-one.sakura.ne.jp/e-net_api/SelectCompanyCodeAtUser.php?user_id="+userId,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JsonParser jsonParser = new JsonParser();
                            //companyCodeを取得して作業リストを取得する
                            String companyCode = jsonParser.parseObject(response, "company_code").get(0);
                            getWorkList(mDate[0],mDate[1],mDate[2],companyCode);
                        }catch (Exception e){

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 通信失敗
                        new AlertDialog.Builder(mContext)
                                .setTitle("情報の取得に失敗しました")
                                .setMessage("情報を再取得します。ネットワークを確認してください。")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getCompanyCode(userId);
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(mContext, "情報が取得できませんでした", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                    }
                }
        )
        /*{
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id",userId);
                return params;
            }
        }*/;
        requestQueue.add(jsonReq);
    }

    /**
     * 作業一覧を取得
     * @param year
     * @param month
     * @param day
     */
    private void getWorkList(final String year, final String month, final String day, final String companyCode){

        mCalenderButton.setText(month+"/"+day);
        String fixDay;
        if(day.length() == 1){
            fixDay = "0"+day;
        }else {
            fixDay = day;
        }
        //日付とユーザコードを元にサーバからデータを取得

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.GET,
                "http://project-one.sakura.ne.jp/e-net_api/SelectWorkList.php?company_code="+companyCode+"&date="+year+month+fixDay,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("aaa",response.toString());
                        ArrayList<String> dataList = new ArrayList<>();
                        ArrayList<WorkContents> workContentsList = new ArrayList<>();
                        JsonParser jsonParser = new JsonParser();
                        //取得データをパースしてリストのビューを組み立てる
                        for (int i=0;i<jsonParser.getJsonArrayLength(response);i++){
                            dataList = jsonParser.getWorkListContent(response,i);
                            workContentsList.add(new WorkContents(dataList.get(0),dataList.get(1),dataList.get(2),
                                    dataList.get(3),dataList.get(4),dataList.get(5)));
                        }
                        showWorkContents(workContentsList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 通信失敗
                        new AlertDialog.Builder(mContext)
                                .setTitle("情報の取得に失敗しました")
                                .setMessage("情報を再取得します。ネットワークを確認してください。")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getWorkList(year, month, day, companyCode);
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(mContext, "情報が取得できませんでした", Toast.LENGTH_SHORT).show();
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
                params.put("company_code", companyCode);
                params.put("date", mDate[0]+mDate[1]+mDate[2]);
                return params;
            }
        };
        requestQueue.add(jsonReq);
    }
}

class WorkContents{
    String id;
    String date;
    String name;
    String field;
    String contents;
    String checkbox;

    public WorkContents(String id,String date, String name, String field, String contents, String checkbox){
        this.id = id;
        this.date = date;
        this.name = name;
        this.field = field;
        this.contents = contents;
        this.checkbox = checkbox;
    }
}

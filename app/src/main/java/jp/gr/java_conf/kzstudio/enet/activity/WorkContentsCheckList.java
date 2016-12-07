package jp.gr.java_conf.kzstudio.enet.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.CalenderDate;
import jp.gr.java_conf.kzstudio.enet.util.GpsPoint;
import jp.gr.java_conf.kzstudio.enet.util.JsonParser;
import jp.gr.java_conf.kzstudio.enet.util.UserPreference;


/**
 * Created by kiyokazu on 2016/11/08.
 */

public class WorkContentsCheckList extends AppCompatActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener{

    Button mCalenderButton;
    Button mNewWorkButton;
    ListView mWorkContentList;

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
        mNewWorkButton = (Button)findViewById(R.id.new_work_button);
        assert mNewWorkButton != null;
        mNewWorkButton.setOnClickListener(this);
        mWorkContentList = (ListView)findViewById(R.id.work_comtent_list);

        mContext = this;
        mCalenderDate = new CalenderDate();
        mDate = mCalenderDate.getTodayDate();

        //初期の日付をセット
        mCalenderButton.setText(mDate[1]+"/"+mDate[2]);

        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        //ユーザのcompanyCodeを取得し会社の作業を取得
        getCompanyCode(userPreference.loadUserPreference("USER_ID"));
    }

    private void showWorkContents(final ArrayList<WorkContents> workContentsList){
        WorkContentListAdapter adapter = new WorkContentListAdapter(this,R.layout.item_work_content_list,workContentsList);
        mWorkContentList.setAdapter(adapter);

        mWorkContentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = (int) parent.getItemIdAtPosition(position);
                if(workContentsList.get(pos).getCheckbox().equals("0")){
                    new AlertDialog.Builder(mContext)
                            .setTitle("業務の完了を報告しますか")
                            .setMessage("業務内容「"+workContentsList.get(pos).getContents()+"」")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //チェックボックスの状態を変化。0→1
                                    Log.i("aaaa",workContentsList.get(pos).getId());
                                    changeCheckboxStatus(workContentsList.get(pos).getId(),"1",workContentsList);
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });
    }

    private void refleshListView(){
        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        //ユーザのcompanyCodeを取得し会社の作業を取得
        getCompanyCode(userPreference.loadUserPreference("USER_ID"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //datepickerを表示
            case R.id.culender:
                datePickerDialog = new DatePickerDialog(this,this,Integer.parseInt(mDate[0]),Integer.parseInt(mDate[1])-1,Integer.parseInt(mDate[2]));
                datePickerDialog.show();
                break;
            case R.id.new_work_button:
                UserPreference userPreference = new UserPreference(mContext, "UserPref");
                if(!userPreference.loadUserPreference("COMPANY_CODE").equals("")) {
                    Intent intent = new Intent(this, WorkContentsInsert.class);
                    startActivity(intent);
                    finish();
                }
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
        mCalenderButton.setText(mDate[1]+"/"+mDate[2]);
        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        //ユーザのcompanyCodeを取得し会社の作業を取得
        getCompanyCode(userPreference.loadUserPreference("USER_ID"));
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

                            UserPreference userPreference = new UserPreference(mContext, "UserPref");
                            userPreference.saveUserPreference(new String[]{"COMPANY_CODE"},new String[]{companyCode});

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
        String fixMonth;
        if(month.length() == 1){
            fixMonth = "0"+month;
        }else {
            fixMonth = month;
        }
        if(day.length() == 1){
            fixDay = "0"+day;
        }else {
            fixDay = day;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.GET,
                "http://project-one.sakura.ne.jp/e-net_api/SelectWorkList.php?company_code="+companyCode+"&date="+year+fixMonth+fixDay,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.i("aaa",response.toString());
                        ArrayList<String> dataList = new ArrayList<>();
                        ArrayList<WorkContents> workContentsList = new ArrayList<>();
                        JsonParser jsonParser = new JsonParser();
                        //取得データをパースしてリストのビューを組み立てる
                        for (int i=0;i<jsonParser.getJsonArrayLength(response);i++){
                            dataList = jsonParser.getWorkListContent(response,i);
                            int image;
                            if(dataList.get(5).equals("0")){
                                image = R.drawable.checkbox;
                            }else {
                                image = R.drawable.checked;
                            }
                            workContentsList.add(new WorkContents(dataList.get(0),dataList.get(1),dataList.get(2),
                                    dataList.get(3),dataList.get(4),dataList.get(5),image));
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

    /**
     * workl_istデータベースのcheckboxステータスを変更する
     * @param id レコードのid
     * @param status 変更後のcheckboxカラムのステータス
     */
    private void changeCheckboxStatus(final String id, final String status, final ArrayList<WorkContents> list){
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.GET,
                "http://project-one.sakura.ne.jp/e-net_api/updateWorklistCheckboxStatus.php?id="+id+"&status="+status,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("aaa",response.toString());
                        JsonParser jsonParser = new JsonParser();
                        String id = jsonParser.parseObject(response,"id").get(0);

                        refleshListView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 通信失敗
                        new AlertDialog.Builder(mContext)
                                .setTitle("情報の更新に失敗しました")
                                .setMessage("ネットワークを確認してください。")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        changeCheckboxStatus(id,status, list);
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(mContext, "更新できませんでした", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                    }
                }
        );
        requestQueue.add(jsonReq);
    }
}

class WorkContents{
    private String id;
    private String date;
    private String name;
    private String field;
    private String contents;
    private String checkbox;
    private int image;

    public WorkContents(String id,String date, String name, String field, String contents, String checkbox, int image){
        this.id = id;
        this.date = date;
        this.name = name;
        this.field = field;
        this.contents = contents;
        this.checkbox = checkbox;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public String getContents() {
        return contents;
    }

    public String getCheckbox() {
        return checkbox;
    }

    public int getImage() {
        return image;
    }
}

class WorkContentListAdapter extends ArrayAdapter<WorkContents> {
    private LayoutInflater inflater;
    private int resourceId;
    private List<WorkContents> item;

    public WorkContentListAdapter(Context context, int resourceId, List<WorkContents> item) {
        super(context, resourceId, item);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resourceId = resourceId;
        this.item = item;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        TextView name = (TextView) view.findViewById(R.id.work_content_person_name);
        TextView field = (TextView) view.findViewById(R.id.work_content_field);
        TextView contents = (TextView) view.findViewById(R.id.work_content_person_contents);
        ImageView image = (ImageView) view.findViewById(R.id.work_content_checkbox);

        WorkContents item = this.item.get(position);
        name.setText(item.getName());
        field.setText(item.getField());
        contents.setText(item.getContents());
        image.setImageResource(item.getImage());

        return view;
    }
}

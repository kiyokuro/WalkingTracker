package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.JsonParser;
import jp.gr.java_conf.kzstudio.walkingtracker.util.UserPreference;

/**
 * GPSTrackの新規登録と既存のもを閲覧を選択するホーム画面
 * Created by kiyokazu on 16/08/03.
 */
public class FunctionHomeActivity extends AppCompatActivity {

    private ListView mRecordList;
    private Button mRegistButton;

    private WalkRecordListAdapter mWalkRecordListAdapter;
    private ArrayList<WalkRecord> mWalkRecords;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_home);
        setTitle("見回り記録");

        mRegistButton = (Button)findViewById(R.id.regist_button);
        mRegistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goGpsTrack();
            }
        });
        mWalkRecords = new ArrayList<>();
        mContext = this;
        getWalkRecordList();
        //実験用コード-------------------------------------------------------------------------------------------------
        /*for(int i=0; i<10; i++) {
            mWalkRecords.add(new WalkRecord("動作確認用データ"+i, "2016/8/"+String.valueOf(i+1), String.valueOf(i)));
        }
        showRecordList();*/
        //-------------------------------------------------------------------------------------------------
    }

    private void getWalkRecordList(){
        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        final String userId = userPreference.loadUserPreference("USER_ID");
        String url = "http://project-one.sakura.ne.jp/e-net_api/SelectGpsData.php?UserId="+userId;
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        JsonParser jsonParser = new JsonParser();
                        ArrayList<String> dateList= jsonParser.parseObject(response, "date");//下3つの名前はAPIに合わせて適宜変更
                        ArrayList<String> titleList= jsonParser.parseObject(response, "title");
                        ArrayList<String> idList= jsonParser.parseObject(response, "id");
                        for(int i=0; i<dateList.size(); i++) {
                            mWalkRecords.add(new WalkRecord(titleList.get(i), dateList.get(i), idList.get(i)));
                        }
                        showRecordList();
                    }
                }
                ,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("error",error.toString());
                // 通信失敗
                new AlertDialog.Builder(mContext)
                        .setTitle("リトライ")
                        .setMessage("情報の取得に失敗しました。情報を再取得しますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getWalkRecordList();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                findViewById(R.id.loadview).setVisibility(View.GONE);
                                Toast.makeText(mContext,"情報を取得できませんでした。",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
        );
        requestQueue.add(jsonObjectRequest);

    }

    private void showRecordList(){
        mRecordList = (ListView)findViewById(R.id.record_list);

        WalkRecordListAdapter adapter = new WalkRecordListAdapter(this,R.layout.item_record_listview,mWalkRecords);
        mRecordList.setAdapter(adapter);

        mRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = (int) parent.getItemIdAtPosition(position);
                String recordId = mWalkRecords.get(pos).getRecordId();
                changeActivity(recordId);
            }
        });
    }

    private void goGpsTrack(){
        Intent intent = new Intent(this, GpsTrackActivity.class);
        startActivity(intent);
        finish();
    }

    private void changeActivity(String recordId){
        Intent intent = new Intent(this, DetailTrackDataActivity.class);
        intent.putExtra("recordId", recordId);
        startActivity(intent);
    }
}

class WalkRecord{
    private String title;
    private String date;
    private String recordId;

    public WalkRecord(String title, String date, String recordId){
        this.title = title;
        this.date = date;
        this.recordId = recordId;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getRecordId() {
        return recordId;
    }
}

class WalkRecordListAdapter extends ArrayAdapter<WalkRecord>{
    private LayoutInflater inflater;
    private int resourceId;
    private List<WalkRecord> item;

    public WalkRecordListAdapter(Context context, int resource, List<WalkRecord> item) {
        super(context, resource, item);
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resourceId = resource;
        this.item = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        TextView date = (TextView)view.findViewById(R.id.record_date);
        TextView title = (TextView)view.findViewById(R.id.record_title);

        WalkRecord item = this.item.get(position);
        date.setText(item.getDate());
        title.setText(item.getTitle());

        return view;
    }

}

package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.GpsPoint;
import jp.gr.java_conf.kzstudio.walkingtracker.util.JsonParser;

/**
 * Created by kiyokazu on 16/04/11.
 */
public class DetailTrackDataActivity extends FragmentActivity implements OnMapReadyCallback, OnClickListener{
    private GoogleMap mMap;
    private Context mContext;

    private ArrayList<GpsPoint> mPoints;
    private ArrayList<Marker> mMarkerList;
    private GPSPointListAdapter mGpsPointListAdapter;

    private ListView mComments;
    private Button mDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_track_data);

        mComments = (ListView)findViewById(R.id.comment_list);
        mDeleteButton = (Button)findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);

        Intent intent = getIntent();
        String recordId = intent.getStringExtra("recordId");
        mContext = this;
        mPoints = new ArrayList<GpsPoint>();
        mMarkerList = new ArrayList<Marker>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //mPoints = (ArrayList<GpsPoint>)intent.getSerializableExtra("pointsList");
        //サーバからバスコースの情報を取得する。
        getGPSPointList(recordId);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //drawPolyline(mMap, mPositions);
        drawMarker();
        if(mPoints.size()<1){
            return;
        }
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(mPoints.get(0).getLan()), Double.parseDouble(mPoints.get(0).getLon())))
                .zoom(16f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    /*void drawPolyline(GoogleMap googleMap, List<LatLng> positions) {
        int fillColor = ContextCompat.getColor(this, R.color.polylineFillColor);

        PolylineOptions fillOptions = new PolylineOptions()
                .width(20)
                .color(fillColor)
                .addAll(positions);

        googleMap.addPolyline(fillOptions);
    }*/

    void drawMarker(){
        for(int i = 0; i < mPoints.size(); i++) {
            Marker marker;
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(mPoints.get(i).getLan()), Double.parseDouble(mPoints.get(i).getLon())))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(String.valueOf(i))
                    .snippet(mPoints.get(i).getComment()));
            mMarkerList.add(marker);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_button:
                //削除処理
                finish();
                break;
        }
    }


    /**
     * GPSの座標リストを取得する
     */
    private void getGPSPointList(final String recordId) {
        String url = "APIのURLを記入";
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonObjReq =new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    //通信成功
                    @Override
                    public void onResponse(JSONObject response) {
                        JsonParser jsonParser = new JsonParser();
                        ArrayList<String> lanList= jsonParser.parseObject(response, "lan");//下4つの名前はAPIに合わせて適宜変更
                        ArrayList<String> lonList= jsonParser.parseObject(response, "lon");
                        ArrayList<String> markerExistList = jsonParser.parseObject(response, "makerExist");
                        ArrayList<String> commentList= jsonParser.parseObject(response, "comment");
                        for(int i=0; i<lanList.size(); i++) {
                            //マーカーがついていた座標点だけのリストを作成
                            if(markerExistList.get(i).equals("true")) {
                                mPoints.add(new GpsPoint(String.valueOf(i), lanList.get(i), lonList.get(i), true, "",
                                        commentList.get(i), String.valueOf(i)));
                            }
                        }
                        showCommentList();
                    }
                }
                ,new Response.ErrorListener() {
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
                                getGPSPointList(recordId);
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
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("recordId", recordId);//パラメータ追加
                return params;
            }
        };
        requestQueue.add(jsonObjReq);

    }
    private void showCommentList(){
        mComments = (ListView)findViewById(R.id.comment_list);

        GPSPointListAdapter adapter = new GPSPointListAdapter(this,R.layout.item_record_listview,mPoints);
        mComments.setAdapter(adapter);

        mComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = (int) parent.getItemIdAtPosition(position);
                String recordId = mPoints.get(pos).getOrder();
                //コメントをタップした時に対応するマーカーの色を変えたりできるといい
            }
        });
    }
}

class GPSPointListAdapter extends ArrayAdapter<GpsPoint> {
    private LayoutInflater inflater;
    private int resourceId;
    private List<GpsPoint> item;
    private Map<Integer, View> positionView;

    public GPSPointListAdapter(Context context, int resourceId, List<GpsPoint> item){
        super(context, resourceId, item);
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resourceId = resourceId;
        this.item = item;
        positionView = new HashMap<Integer, View>();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        TextView order = (TextView)view.findViewById(R.id.comment_order);
        TextView comment = (TextView)view.findViewById(R.id.comment);
        TextView lan = (TextView)view.findViewById(R.id.lan);
        TextView lon = (TextView)view.findViewById(R.id.lon);

        GpsPoint item = this.item.get(position);
        order.setText(item.getOrder());
        comment.setText(item.getComment());
        lan.setText("緯度："+item.getLan());
        lon.setText("軽度：" + item.getLon());

        positionView.put(position, view);

        return view;
    }

    public View getPositionView(int targetPosition){
        return positionView.get(targetPosition);
    }

    @Override
    public boolean isEnabled(int position){
        return true;
    }
}
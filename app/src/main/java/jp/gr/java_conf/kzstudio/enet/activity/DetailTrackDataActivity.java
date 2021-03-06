package jp.gr.java_conf.kzstudio.enet.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.util.GpsPoint;
import jp.gr.java_conf.kzstudio.enet.util.JsonParser;

/**
 * Created by kiyokazu on 16/04/11.
 */
public class DetailTrackDataActivity extends FragmentActivity implements OnMapReadyCallback, OnClickListener{

    private static final float POLYLINE_WIDTH_IN_PIXELS = 6;

    private GoogleMap mMap;
    private Context mContext;

    private ArrayList<GpsPoint> mCheckPointPosition;
    private ArrayList<Marker> mMarkerList;
    private GPSPointListAdapter mGpsPointListAdapter;
    private ArrayList<LatLng> mLatLngs;

    private ListView mComments;
    private Button mDeleteButton;

    private String recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_track_data);

        mComments = (ListView)findViewById(R.id.comment_list);
        mDeleteButton = (Button)findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);

        Intent intent = getIntent();
        recordId = intent.getStringExtra("recordId");
        mContext = this;
        mCheckPointPosition = new ArrayList<GpsPoint>();
        mMarkerList = new ArrayList<Marker>();
        mLatLngs = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //mCheckPointPosition = (ArrayList<GpsPoint>)intent.getSerializableExtra("pointsList");
        //サーバからバスコースの情報を取得する。
        getGPSPointList(recordId);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //drawPolyline(mMap, mPositions);
        /*drawMarker();
        if(mCheckPointPosition.size()<1){
            return;
        }
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(new LatLng(Double.parseDouble(mCheckPointPosition.get(0).getLan()), Double.parseDouble(mCheckPointPosition.get(0).getLon())))
                .zoom(16f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));
        */
    }

    void drawPolyline(GoogleMap googleMap, List<LatLng> positions) {
        int fillColor = ContextCompat.getColor(this, R.color.polylineFillColor);

        PolylineOptions fillOptions = new PolylineOptions()
                .width(POLYLINE_WIDTH_IN_PIXELS)
                .color(fillColor)
                .addAll(positions);

        googleMap.addPolyline(fillOptions);
    }

    /*void drawMarker(){
        for(int i = 0; i < mCheckPointPosition.size(); i++) {
            Marker marker;
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(mCheckPointPosition.get(i).getLan()), Double.parseDouble(mCheckPointPosition.get(i).getLon())))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(String.valueOf(i))
                    .snippet(mCheckPointPosition.get(i).getComment()));

        }
    }*/

    private void createMarker(String title, String comment, String lan, String lon){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(lan), Double.parseDouble(lon)))
                .title(comment)
                        //.snippet(comment)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMarkerList.add(marker);
    }

    private void moveCamera(double lan, double lon){
        LatLng nowArea = new LatLng(lan, lon);

        CameraPosition camerapos = new CameraPosition.Builder()
                .target(nowArea).zoom(18f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    private void showMakerWindow(int markerListIndex){
        mMarkerList.get(markerListIndex).showInfoWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_button:
                //削除処理
                new AlertDialog.Builder(mContext)
                        .setTitle("この記録を削除しますか？")
                        .setMessage("削除すると記録は復元できません。削除しますか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRecord(recordId);
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


    /**
     * GPSの座標リストを取得する
     */
    private void getGPSPointList(final String recordId) {
        String url = "http://project-one.sakura.ne.jp/e-net_api/SelectGpsDataDetail.php";
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest =new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    //通信成功
                    @Override
                    public void onResponse(String response) {
                        Log.i("response",response.toString());
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonParser jsonParser = new JsonParser();
                        String trackData = jsonParser.getTrackData(jsonObject, "track_data");
                        String[] datas = trackData.split(",", 0);
                        for(int i=0; i<datas.length; i++){
                            String[] pointData = datas[i].split("@",-1);
                            //記録されている位置情報のうち、チェックポイントに登録されているものだけ取得したリストを作成
                            if(pointData[3].equals("true")){
                                if(pointData.length > 7 && pointData[7]!=null){
                                    if(pointData[7].equals("true")) {
                                        mCheckPointPosition.add(new GpsPoint(pointData[0], pointData[1], pointData[2], true, pointData[4], pointData[5], pointData[6], true));
                                    }
                                }else {
                                    mCheckPointPosition.add(new GpsPoint(pointData[0], pointData[1], pointData[2], true, pointData[4], pointData[5], pointData[6], false));
                                }
                                createMarker(pointData[4],pointData[5],pointData[1],pointData[2]);
                            }
                            //記録されている位置情報をからリストを作成。移動経路をラインとして見せるために利用
                            mLatLngs.add(new LatLng(Double.parseDouble(pointData[1]),Double.parseDouble(pointData[2])));
                        }

                        if(mCheckPointPosition.size()<1){
                            mCheckPointPosition.add(new GpsPoint("0", "0", "0", false, "データなし", "データなし", "0", false));
                        }else {
                            //位置情報を全てつないだラインを地図上に描画する
                            drawPolyline(mMap, mLatLngs);
                            //カメラを最後のチェックポイント追加地点に移動させる。
                            moveCamera(Double.parseDouble(mCheckPointPosition.get(mCheckPointPosition.size()-1).getLan()), Double.parseDouble(mCheckPointPosition.get(mCheckPointPosition.size()-1).getLon()));
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
                params.put("RecordId", recordId);//パラメータ追加
                return params;
            }
        };
        requestQueue.add(stringRequest);

    }
    private void showCommentList(){
        mComments = (ListView)findViewById(R.id.comment_list);

        GPSPointListAdapter adapter = new GPSPointListAdapter(this,R.layout.item_comment_pos, mCheckPointPosition);
        mComments.setAdapter(adapter);

        mComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = (int) parent.getItemIdAtPosition(position);
                String recordId = mCheckPointPosition.get(pos).getOrder();
                double lan = Double.parseDouble(mCheckPointPosition.get(pos).getLan());
                double lon = Double.parseDouble(mCheckPointPosition.get(pos).getLon());
                moveCamera(lan, lon);
                showMakerWindow(pos);
            }
        });
    }

    private void deleteRecord(final String recordId) {
        String url = "http://project-one.sakura.ne.jp/e-net_api/DeleteGpsData.php";
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest =new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    //通信成功
                    @Override
                    public void onResponse(String response) {
                        Log.i("response",response.toString());
                        if(response.equals("OK")){
                            goFunctionHome();
                        }else {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("削除できませんでした")
                                    .setMessage("削除できませんでした")
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
                ,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 通信失敗
                new AlertDialog.Builder(mContext)
                        .setTitle("リトライ")
                        .setMessage("もう一度実行しますか？")
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
                                //findViewById(R.id.loadview).setVisibility(View.GONE);
                                Toast.makeText(mContext,"削除できませんでした。",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("RecordId", recordId);//パラメータ追加
                return params;
            }
        };
        requestQueue.add(stringRequest);

    }

    private void goFunctionHome(){
        Intent intent = new Intent(this, FunctionHomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            goFunctionHome();
            return true;
        }
        return false;
    }
}

class GPSPointListAdapter extends ArrayAdapter<GpsPoint> {
    private LayoutInflater inflater;
    private int resourceId;
    private List<GpsPoint> item;
    RequestQueue queue;

    public GPSPointListAdapter(Context context, int resourceId, List<GpsPoint> item){
        super(context, resourceId, item);
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resourceId = resourceId;
        this.item = item;
        queue = Volley.newRequestQueue(getContext());
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
        NetworkImageView image = (NetworkImageView)view.findViewById(R.id.checkpoint_image);

        GpsPoint item = this.item.get(position);
        order.setText(item.getCheckPointNum());
        comment.setText(item.getComment());
        lan.setText("緯度："+item.getLan());
        lon.setText("軽度：" + item.getLon());
        if(item.isPhotoExist()){
            String str = item.getTime();

            String url = "http://www.project-one.sakura.ne.jp/e-net_api/photo/"+str;//写真のURLは時間にしてある
            Log.i("aaaa","photoName"+url);
            image.setImageUrl(url, new ImageLoader(queue, new ImageLoader.ImageCache() {
                @Override
                public Bitmap getBitmap(String url) {

                    return null;
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {

                }
            }));
            //Bitmap bm = deformationPhoto(url);
            //image.setImageBitmap(bm);

        }

        return view;
    }

    @Override
    public boolean isEnabled(int position){
        return true;
    }

    private Bitmap deformationPhoto(String filePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        int inSampleSize = 4;//画像サイズを1/4する

        // inSampleSize を計算
        options.inSampleSize = inSampleSize;

        // inSampleSize をセットしてデコード
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}

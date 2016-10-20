package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.util.GpsPoint;
import jp.gr.java_conf.kzstudio.walkingtracker.util.UserPreference;

public class GpsTrackActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private final int _REQUEST_PERMISSION_GPS = 10;
    private static final float POLYLINE_WIDTH_IN_PIXELS = 6;

    private GoogleMap mMap;
    private Marker mMaker;
    private Context mContext;

    private LocationManager mLocationManager;
    private double mLat = 35.681382;
    private double mLon = 139.766084;
    //DBに登録するために記録する座標などの情報
    private List<GpsPoint> mCheckPointPosition;
    //線を引くために取得する座標
    private List<LatLng> mPositions;
    private int mCount = 0;
    private int mCheckPointNum = 1;
    private boolean isStart = false;//位置情報取得をスタートしているか
    private boolean isMarkerExist = false;//現在の座標にマーカがあるか

    private View mProgressView;
    private View mSwitchExplainView;
    private ToggleButton mGpsSwitch;
    private Button mAddMarkerButton;
    private Button mRegistRouteButton;
    private Button mResetRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_track);

        mContext = this;
        mCheckPointPosition = new ArrayList<GpsPoint>();
        mPositions = new ArrayList<LatLng>();
        mProgressView = findViewById(R.id.progress_view);
        mProgressView.setVisibility(View.GONE);
        mSwitchExplainView = findViewById(R.id.switch_explain_view);
        mSwitchExplainView.setVisibility(View.VISIBLE);
        mGpsSwitch = (ToggleButton) findViewById(R.id.gps_track_switch);
        mGpsSwitch.setOnClickListener(this);
        mAddMarkerButton = (Button) findViewById(R.id.add_marker_button);
        mAddMarkerButton.setOnClickListener(this);
        mRegistRouteButton = (Button) findViewById(R.id.regist_route_button);
        mRegistRouteButton.setOnClickListener(this);
        mRegistRouteButton.setVisibility(View.GONE);
        mResetRouteButton = (Button) findViewById(R.id.reset_button);
        mResetRouteButton.setOnClickListener(this);
        mResetRouteButton.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * 位置情報の権限が許可されているか確認する。許可されてなければ許可を求める。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(GpsTrackActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, _REQUEST_PERMISSION_GPS);

            } else {
                Toast toast = Toast.makeText(this,
                        "あなたの位置情報を利用するため、GPSの使用を許可してください", Toast.LENGTH_LONG);
                toast.show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, _REQUEST_PERMISSION_GPS);
            }
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * checkPermissionで権限を求めた結果を受け取る。
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == _REQUEST_PERMISSION_GPS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else {
                Toast toast = Toast.makeText(this, "見回り記録は実行できません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /**
     * 現在地の取得を有効にする
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void getLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //GPSがONかチェック
        String gpsStatus = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        //GPSがOFFならダイアログを表示
        if (gpsStatus.indexOf("gps", 0) < 0) {
            new AlertDialog.Builder(this)
                    .setTitle("位置情報がONになっていません")
                    .setMessage("位置情報がONに設定されていません。位置情報をONにしてください。")
                    .setPositiveButton("GPS設定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //GPSの設定画面を開く
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("閉じる", null)
                    .show();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            //現在地取得開始
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        //GPSの取得間隔の設定 3000ms && 2m
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2, this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!isMarkerExist) {
            LatLng nowArea = new LatLng(mLat, mLon);
            mMaker = mMap.addMarker(new MarkerOptions().position(nowArea).title("現在地"));
            //isMarkerExist = true;
        }
    }

    /**
     * 現在地にマーカをセットする
     */
    private void showNowLocation() {
        //mMaker.remove();
        LatLng nowArea = new LatLng(mLat, mLon);

        CameraPosition camerapos = new CameraPosition.Builder()
                .target(nowArea).zoom(18f).build();
        mMaker.setPosition(nowArea);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));
    }

    /**
     * 線の頂点になる点のリストを渡すとそれをつないで線を引く
     * @param googleMap
     * @param positions 線を引く時の頂点になる点のリスト
     */
    void drawPolyline(GoogleMap googleMap, List<LatLng> positions) {
        int fillColor = ContextCompat.getColor(this, R.color.polylineFillColor);

        PolylineOptions fillOptions = new PolylineOptions()
                .width(POLYLINE_WIDTH_IN_PIXELS)
                .color(fillColor)
                .addAll(positions);

        googleMap.addPolyline(fillOptions);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLocationChanged(Location location) {
        mProgressView.setVisibility(View.GONE);

        mLat = location.getLatitude();
        mLon = location.getLongitude();
        //Log.v("現在地", "Lat=" + mLat + "Lon=" + mLon);

        //mLocationManager.removeUpdates(mLocationListener);
        mPositions.add(new LatLng(mLat,mLon));
        mCount++;
        isMarkerExist = false;
        isStart = true;

        //初めての座標取得ならその地点をスタート地点とする
        if(mCheckPointPosition.size()<1){
            mCheckPointPosition.add(new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), true, " ", "スタート", " ", false));
        }else {
            mCheckPointPosition.add(new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), false, " ", " ", " ", false));
        }
        showNowLocation();
        drawPolyline(mMap, mPositions);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gps_track_switch:
                if (mGpsSwitch.isChecked()) {
                    mSwitchExplainView.setVisibility(View.GONE);
                    mProgressView.setVisibility(View.VISIBLE);
                    mRegistRouteButton.setVisibility(View.GONE);
                    mResetRouteButton.setVisibility(View.GONE);
                    getLocation();
                    isStart = true;
                } else {
                    isStart = false;
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    mLocationManager.removeUpdates(this);//現在地の取得を終了する
                    mRegistRouteButton.setVisibility(View.VISIBLE);
                    mResetRouteButton.setVisibility(View.VISIBLE);
                    mProgressView.setVisibility(View.GONE);
                }
                break;
            case R.id.add_marker_button:
                final String[] checkPointTitle = {""};
                if(!isStart || isMarkerExist){
                    return;
                }
                /*final EditText editView = new EditText(this);
                new AlertDialog.Builder(GpsTrackActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("このポイントの記録事項を記入してください。")
                        .setCancelable(false)
                        .setView(editView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //checkPointTitle[0] = editView.getText().toString();
                                createMarker(editView.getText().toString(), editView.getText().toString());
                                mCheckPointPosition.add(mCount, new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), true, " ", editView.getText().toString(), String.valueOf(mCheckPointNum)));
                                mCheckPointNum++;
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        })
                        .show();*/

                Intent intent = new Intent(this, MakeCheckpointActivity.class);
                String str = String.valueOf(mLat+mLon).replaceAll(".","_");
                intent.putExtra("latlng",str);
                startActivityForResult(intent, 1);

                isMarkerExist = true;
                break;
            case R.id.regist_route_button:
                final EditText titleEdit = new EditText(this);
                final String[] recordTitle = {""};
                new AlertDialog.Builder(GpsTrackActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("この記録にタイトルをつけてください。タイトルをつけない場合は自動で日付になります。")
                        .setCancelable(false)
                        .setView(titleEdit)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //ゴール地点を作成のマーカを作る
                                mCheckPointPosition.add(new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), true, " ", "エンド", " ", false));

                                TimeZone timeZone = TimeZone.getTimeZone("Asia/Tokyo");
                                Calendar calendar = Calendar.getInstance(timeZone);
                                final String date = calendar.get(Calendar.YEAR)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
                                recordTitle[0] = titleEdit.getText().toString();
                                //タイトルが空の場合に、タイトルを日付で自動生成する
                                if(recordTitle[0].equals("")){
                                    recordTitle[0] = date+"の記録";
                                }
                                registData(recordTitle[0], date);
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        })
                        .show();

                break;
            case R.id.reset_button:
                mMap.clear();
                mCount = 0;
                mCheckPointNum = 1;
                isMarkerExist = false;
                mCheckPointPosition.clear();
                mPositions.clear();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();

        if (resultCode == RESULT_OK){
            createMarker(bundle.getString("title"), bundle.getString("comment"));
            mCheckPointPosition.add(mCount, new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), true, " ", bundle.getString("title"), String.valueOf(mCheckPointNum), true));
            mCheckPointNum++;
        }
    }

    private void createMarker(String title, String comment){
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLat, mLon))
                .title(title)
                        //.snippet(comment)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    //DB記録ようのデータを送信できるようにString型に加工する
    private String makeStringData(List<GpsPoint> list){
        String stringData = "";
        for(int i = 0; i< mCheckPointPosition.size(); i++){
            stringData += mCheckPointPosition.get(i).getOrder()+"@"+ mCheckPointPosition.get(i).getLan()+"@"+ mCheckPointPosition.get(i).getLon()+"@"+
                    String.valueOf(mCheckPointPosition.get(i).isMarkerExist())+"@"+ mCheckPointPosition.get(i).getTitle()+"@"+
                    mCheckPointPosition.get(i).getComment() + "@" + mCheckPointPosition.get(i).getCheckPointNum()+"@"+mCheckPointPosition.get(i).isPhotoExist()+",";
        }
        return stringData;
    }

    private void registData(final String recordTitle, final String date){
        UserPreference userPreference = new UserPreference(mContext, "UserPref");
        final String userId = userPreference.loadUserPreference("USER_ID");

        //サーバに登録する処理を書く。登録日時も取得して送信
        final String stringData = makeStringData(mCheckPointPosition);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringReq = new StringRequest(
                Request.Method.POST,
                "http://project-one.sakura.ne.jp/e-net_api/InsertGpsData.php",
                new Response.Listener<String>() {
                    //通信成功
                    @Override
                    public void onResponse(String response) {
                        //Log.i("response",response);
                        if(response.equals("OK")){
                            goFunctionHome();
                        }else {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("保存できませんでした")
                                    .setMessage("もう一度保存してください")
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
                        .setTitle("情報を登録できませんでした")
                        .setMessage("インターネット接続を確認してください。再度登録するには「OK」を押してください。")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                registData(recordTitle, date);
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, "通信に失敗しました。", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("RecordTitle", recordTitle);
                params.put("Date", date);
                params.put("GpsPointData", stringData);
                params.put("UserId", userId);
                return params;
            }
        };
        requestQueue.add(stringReq);
    }

    private void goFunctionHome(){
        Intent intent = new Intent(this, FunctionHomeActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(this)
                    .setTitle("記録を中止しますか？")
                    .setMessage("保存する前に画面を移動すると記録が失われます")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO 自動生成されたメソッド・スタブ
                            goFunctionHome();
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




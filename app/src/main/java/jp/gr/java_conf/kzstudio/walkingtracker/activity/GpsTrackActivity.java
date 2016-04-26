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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.kzstudio.walkingtracker.R;

public class GpsTrackActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private final int _REQUEST_PERMISSION_GPS = 10;
    private static final float POLYLINE_WIDTH_IN_PIXELS = 8;

    private GoogleMap mMap;
    private Marker mMaker;
    private Context mContex;

    private LocationManager mLocationManager;
    private double mLat = 35.681382;
    private double mLon = 139.766084;
    private List<GpsPoint> mPoints;
    private List<LatLng> mPositions;
    private int mCount = 0;
    private int mCheckPointNum = 1;
    private boolean isStart = false;//位置情報取得をスタートしているか
    private boolean isMarkerExist = false;
    private boolean isPutMakerHere = false;

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

        mContex = this;
        mPoints = new ArrayList<GpsPoint>();
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
                        "現在地の天気を検索するために、GPSの使用を許可してください", Toast.LENGTH_LONG);
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
                Toast toast = Toast.makeText(this, "アプリは実行できません", Toast.LENGTH_SHORT);
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

        //現在地取得開始
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
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
    }

    /**
     * 現在地にマーカをセットする
     */
    private void showNowLocation() {
        //mMaker.remove();
        LatLng nowArea = new LatLng(mLat, mLon);
        if (!isMarkerExist) {
            mMaker = mMap.addMarker(new MarkerOptions().position(nowArea).title("現在地"));
            isMarkerExist = true;
        }
        CameraPosition camerapos = new CameraPosition.Builder()
                .target(nowArea).zoom(17f).build();
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
                .width(POLYLINE_WIDTH_IN_PIXELS * 2)
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
        isPutMakerHere = false;
        isStart = true;
        mPoints.add(new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), false, "", "", ""));
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
                } else {
                    isStart = false;
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
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
                final EditText editView = new EditText(this);
                new AlertDialog.Builder(GpsTrackActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("チェックポイント名を入力してください")
                        .setCancelable(false)
                        .setView(editView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                checkPointTitle[0] = editView.getText().toString();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        })
                        .show();

                if(isStart && !isPutMakerHere) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLat, mLon))
                            .title(checkPointTitle[0])
                            .snippet("コメント"));
                    mPoints.add(mCount, new GpsPoint(String.valueOf(mCount), String.valueOf(mLat), String.valueOf(mLon), true, "", "", String.valueOf(mCheckPointNum)));
                    mCheckPointNum++;
                    isPutMakerHere = true;
                }
                break;
            case R.id.regist_route_button:
                //registBusStopList(mBusCourseCode, mPoints);
                changeActivity();
                break;
            case R.id.reset_button:
                mMap.clear();
                mCount = 0;
                mCheckPointNum = 1;
                isMarkerExist = false;
                mPoints.clear();
                mPositions.clear();
        }
    }


    private void changeActivity(){
        //Intent intent = new Intent(this, GpsTrackDataFixActivity.class);
        //intent.putExtra("busCourseCode", mBusCourseCode);
        //startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                finish();
            }
        }
    }
}

class GpsPoint implements Serializable {
    String order;
    String lan;
    String lon;
    boolean markerExist;
    String title = "";
    String comment = "";
    String busStopNum;

    public GpsPoint(String order, String lan, String lon, boolean markerExist, String title, String comment, String busStopNum){
        this.order = order;
        this.lan = lan;
        this.lon = lon;
        this.markerExist = markerExist;
        this.title = title;
        this.comment = comment;
        this.busStopNum = busStopNum;
    }

    public String getOrder() {
        return order;
    }

    public String getLan() {
        return lan;
    }

    public String getLon() {
        return lon;
    }

    public boolean isMarkerExist() {
        return markerExist;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getBusStopNum() {
        return busStopNum;
    }
}

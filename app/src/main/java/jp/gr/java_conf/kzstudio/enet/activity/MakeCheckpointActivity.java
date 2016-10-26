package jp.gr.java_conf.kzstudio.enet.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.kzstudio.enet.R;
import jp.gr.java_conf.kzstudio.enet.fragment.ProgressDialogFragment;
import jp.gr.java_conf.kzstudio.enet.util.ItemDialogUtility;
import jp.gr.java_conf.kzstudio.enet.util.MultipartRequest;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class MakeCheckpointActivity extends FragmentActivity implements View.OnClickListener, ItemDialogUtility.Listener{

    private static final ItemDialogUtility.ListItem[] items = {
            new ItemDialogUtility.ListItem("カメラで撮影", R.drawable.ic_dialog_camera_48),
            new ItemDialogUtility.ListItem("ギャラリーの選択", R.drawable.ic_dialog_gallery_48),
            new ItemDialogUtility.ListItem("キャンセル", 0),
    };
    static final int REQUEST_CODE_CAMERA = 1; /* カメラを判定するコード */
    static final int REQUEST_CODE_GALLERY = 2; /* ギャラリーを判定するコード */
    private final int _REQUEST_PERMISSION_CAMERA = 0x01;
    private final int _REQUEST_PERMISSION_STORAGE = 0x02;

    private ImageView imageView;
    private Button left_turn;
    private Button right_turn;
    private TextView text;
    private EditText commentText;
    private Button upload;
    private FrameLayout frameLayout;

    private Uri mImageUri;

    private String[] postMessege = new String[3];
    private boolean isTakePhoto = false;
    private String title;
    private long currentTime;
    private RequestQueue mQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_checkpoint);
        setTitle("チェックポイント作成");

        imageView = (ImageView)findViewById(R.id.image);
        imageView.setOnClickListener(this);
        left_turn = (Button)findViewById(R.id.left_turn);
        left_turn.setOnClickListener(this);
        left_turn.setVisibility(View.INVISIBLE);
        right_turn = (Button)findViewById(R.id.right_turn);
        right_turn.setOnClickListener(this);
        right_turn.setVisibility(View.INVISIBLE);
        text = (TextView)findViewById(R.id.text);
        text.setText("タップで写真を選択してください");
        commentText = (EditText)findViewById(R.id.comment_text);
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(this);
        frameLayout = (FrameLayout) findViewById(R.id.frame);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        currentTime = intent.getLongExtra("cuttentTime",0);
        mQueue = Volley.newRequestQueue(this);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
    }

    /**
     * 位置情報の権限が許可されているか確認する。許可されてなければ許可を求める。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MakeCheckpointActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, _REQUEST_PERMISSION_CAMERA);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, _REQUEST_PERMISSION_CAMERA);
            }
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
        if (requestCode == _REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast toast = Toast.makeText(this, "見回り記録は利用できません", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float minScale = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)frameLayout.getLayoutParams();
        layoutParams.width = (int)minScale-60;
        layoutParams.height = (int)minScale-60;
        frameLayout.setLayoutParams(layoutParams);
    }
    @Override
    public void onClickItem(int item) {
        switch (item){
            case 0:
                wakeupCamera(); // カメラ起動
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.image:
                wakeupCamera();
                break;
            case R.id.upload:
                if(commentText.getText().toString().equals("")){
                    new AlertDialog.Builder(MakeCheckpointActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("コメントを記入してください")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            })
                            .show();
                }else {
                    upload.setEnabled(false);
                    if (isTakePhoto) {

                        uploadPhoto();
                    } else {
                        closeActivity();
                    }
                }
                break;
            case R.id.right_turn:
                break;
            case R.id.left_turn:
                break;
        }
    }

    protected void wakeupCamera(){
        mImageUri = getPhotoUri(title, currentTime);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void rotatePhoto(int angle){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){

            switch(requestCode){
                case 1: // カメラの場合
                    try {
                        Bitmap bm = BitmapFactory.decodeFile(getPath(this,mImageUri));
                        imageView.setImageBitmap(bm);
                        //imageView.setImageURI(mImageUri);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
            //right_turn.setVisibility(View.VISIBLE);
            //left_turn.setVisibility(View.VISIBLE);
            text.setText("");
            imageView.setEnabled(false);
            isTakePhoto = true;
        }
    }

    /**
     * 画像のディレクトリパスを取得する
     * @return
     */
    private String getDirPath() {
        String dirPath = "";
        File photoDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        if (extStorageDir.canWrite()) {
            photoDir = new File(extStorageDir.getPath() + "/" + getPackageName());
        }
        if (photoDir != null) {
            if (!photoDir.exists()) {
                photoDir.mkdirs();
            }
            if (photoDir.canWrite()) {
                dirPath = photoDir.getPath();
            }
        }
        return dirPath;
    }

    /**
     * 画像のUriを取得する
     * @return
     */
    private Uri getPhotoUri(String title, long currentTimeMillis) {


        String dirPath = getDirPath();
        String fileName = title+ ".jpg";
        String path = dirPath + "/" + fileName;
        File file = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, path);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    private void uploadPhoto(){
        ProgressDialogFragment dialog = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Title","お待ち下さい");
        bundle.putString("Message","登録中");
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"PROGRESS");

        Map<String, String> binaryParams = new HashMap<>();
        Map<String,String> strMap = new HashMap<String, String>();
        Log.i("aaaa",getPath(this, mImageUri));
        strMap.put("name",String.valueOf(currentTime));
        binaryParams.put("uploadFiles", getPath(this, mImageUri));

        MultipartRequest multipartRequest = new MultipartRequest(
                "http://www.project-one.sakura.ne.jp/e-net_api/TanboCameraServer.php",//insertPhoto.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Upload成功
                        Log.i("aaaa","uploadSuccess"+response);
                        ProgressDialogFragment dialog = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag("PROGRESS");
                        if(dialog != null){
                            dialog.onDismiss(dialog.getDialog());
                        }
                        closeActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Upload失敗
                        Log.i("aaaa","uploadFail"+ error.getMessage());
                        ProgressDialogFragment dialog = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag("PROGRESS");
                        if(dialog != null){
                            dialog.onDismiss(dialog.getDialog());
                        }
                        isTakePhoto = false;
                    }
                });
        multipartRequest.setBinaryParams(binaryParams);
        multipartRequest.setTextParams(strMap);
        mQueue.add(multipartRequest);
    }

    private void closeActivity(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("comment", commentText.getText().toString());
        bundle.putString("time", String.valueOf(currentTime));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
    /**
     * UriからPathへの変換処理
     * @param uri
     * @return String
     */
    public static String getPath(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] columns = { MediaStore.Images.Media.DATA };
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();
        return path;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

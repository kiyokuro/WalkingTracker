package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import com.google.android.gms.maps.SupportMapFragment;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.fragment.ProgressDialogFragment;
import jp.gr.java_conf.kzstudio.walkingtracker.util.ItemDialogUtility;
import jp.gr.java_conf.kzstudio.walkingtracker.util.PictureUtil;
import jp.gr.java_conf.kzstudio.walkingtracker.util.UploadAsyncTask;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class MakeCheckpointActivity extends FragmentActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<String>, ItemDialogUtility.Listener{

    private static final ItemDialogUtility.ListItem[] items = {
            new ItemDialogUtility.ListItem("カメラで撮影", R.drawable.ic_dialog_camera_48),
            new ItemDialogUtility.ListItem("ギャラリーの選択", R.drawable.ic_dialog_gallery_48),
            new ItemDialogUtility.ListItem("キャンセル", 0),
    };
    static final int REQUEST_CODE_CAMERA = 1; /* カメラを判定するコード */
    static final int REQUEST_CODE_GALLERY = 2; /* ギャラリーを判定するコード */
    private final int _REQUEST_PERMISSION_CAMERA = 0x01;

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

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
    }

    /**
     * 位置情報の権限が許可されているか確認する。許可されてなければ許可を求める。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MakeCheckpointActivity.this,
                        new String[]{Manifest.permission.CAMERA}, _REQUEST_PERMISSION_CAMERA);

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,}, _REQUEST_PERMISSION_CAMERA);
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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast toast = Toast.makeText(this, "カメラは利用できません", Toast.LENGTH_SHORT);
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
                        //非同期処理の実行
                        Bundle args = new Bundle();
                        args.putStringArray("postMassege", postMessege);
                        getSupportLoaderManager().initLoader(0, args, this);//onCreateLoderを実行する
                    } else {
                        closeActivity();
                    }
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){

            switch(requestCode){
                case 1: // カメラの場合
                    try {
                        imageView.setImageURI(mImageUri);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
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

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        final File outputDir = getDir("eNet",Context.MODE_PRIVATE);
        //プログレスダイアログの設定
        ProgressDialogFragment dialog = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Title","お待ち下さい");
        bundle.putString("Message","登録中");
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"PROGRESS");

        // サーバにアップロード
        UploadAsyncTask uploadAsyncTask = new UploadAsyncTask(this, dialog, outputDir, mImageUri);
        uploadAsyncTask.forceLoad();
        return uploadAsyncTask;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        //プログレスダイアログの終了処理
        ProgressDialogFragment dialog = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag("PROGRESS");
        if(dialog != null){
            dialog.onDismiss(dialog.getDialog());
        }
        closeActivity();
    }

    private void closeActivity(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("title", commentText.getText().toString());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<String> loader) {

    }
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

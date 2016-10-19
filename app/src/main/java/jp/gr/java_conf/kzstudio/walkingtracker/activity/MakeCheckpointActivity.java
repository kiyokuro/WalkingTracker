package jp.gr.java_conf.kzstudio.walkingtracker.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import jp.gr.java_conf.kzstudio.walkingtracker.R;
import jp.gr.java_conf.kzstudio.walkingtracker.fragment.ProgressDialogFragment;
import jp.gr.java_conf.kzstudio.walkingtracker.util.GpsPoint;
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

    private ImageView imageView;
    private Button button1;
    private Button left_turn;
    private Button right_turn;
    private TextView text;
    private EditText commentText;
    private Button upload;
    private FrameLayout frameLayout;

    private Uri bitmapUri;
    private Bitmap bm;
    private Bitmap bm2;
    private String selectedDate;

    private String[] postMessege = new String[3];
    private File outputDir;
    private boolean isTakePhoto = false;
    int loatation = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_checkpoint);
        setTitle("チェックポイント作成");

        imageView = (ImageView)findViewById(R.id.image);
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);
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

        outputDir = getDir("eNet", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        postMessege[0] = intent.getStringExtra("latlng");


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
            case 1:
                wakeupGallery(); // ギャラリー起動
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button1:
                ItemDialogUtility.show(this, this, "写真を選択", items);
                break;
            case R.id.upload:
                upload.setEnabled(false);
                if(isTakePhoto) {
                    Matrix matrixl1 = new Matrix();
                    matrixl1.postRotate((float) loatation % 360);
                    PictureUtil.bmPhotoReal = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixl1, false);
                    PictureUtil.bmPhoto = bm2;

                    //非同期処理の実行
                    Bundle args = new Bundle();
                    args.putStringArray("postMassege", postMessege);
                    getSupportLoaderManager().initLoader(0, args, this);//onCreateLoderを実行する
                }else {
                    closeActivity();
                }
                break;
            case R.id.left_turn:
                if(bm2 == null){
                    break;
                }
                Matrix matrixl = new Matrix();
                matrixl.postRotate(-90.0f);
                loatation -= 90;
                //Bitmap回転させる
                Bitmap fixedBm2L = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrixl, false);
                bm2 = fixedBm2L;
                imageView.setImageBitmap(fixedBm2L);
                break;
            case R.id.right_turn:
                if(bm2 == null){
                    break;
                }
                Matrix matrixr = new Matrix();
                matrixr.postRotate(90.0f);
                loatation += 90;
                //Bitmap回転させる
                bm2 = Bitmap.createBitmap(bm2, 0, 0, bm2.getWidth(), bm2.getHeight(), matrixr, false);
                imageView.setImageBitmap(bm2);
                break;
        }
    }

    protected void wakeupCamera(){
        File out = new File(Environment.getExternalStorageDirectory()+"/tmpPhoto.jpg");
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
        startActivityForResult(i, REQUEST_CODE_CAMERA);
    }

    protected void wakeupGallery(){
        Intent i = new Intent();
        i.setType("image/*"); // 画像のみが表示されるようにフィルターをかける
        i.setAction(Intent.ACTION_GET_CONTENT); // 出0他を取得するアプリをすべて開く
        startActivityForResult(i, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            if (bm != null)
                bm.recycle(); // 直前のBitmapが読み込まれていたら開放する

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 元の1/4サイズでbitmap取得
            options.inDither = false;

            switch(requestCode){
                case 1: // カメラの場合
                    try {
                        //FileInputStream fileInputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory()+"/tmpPhoto.jpg"));
                        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                        imageOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;
                        bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/tmpPhoto.jpg", imageOptions);
                        //bm = (Bitmap) data.getExtras().get("data");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    // 撮影した画像をギャラリーのインデックスに追加されるようにスキャンする。
                    // これをやらないと、アプリ起動中に撮った写真が反映されない
                    /*String[] paths = {bitmapUri.getPath()};
                    String[] mimeTypes = {"image/*"};
                    MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, new MediaScannerConnection.OnScanCompletedListener(){
                        @Override
                        public void onScanCompleted(String path, Uri uri){
                        }
                    });
                    */
                    break;
                case 2: // ギャラリーの場合
                    try{
                        //ContentResolver cr = getContentResolver();
                        //String[] columns = { MediaStore.Images.Media.DATA };
                        //Cursor c = cr.query(data.getData(), columns, null, null, null);
                        //c.moveToFirst();
                        //bitmapUri = Uri.fromFile(new File(c.getString(0)));
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        bm = BitmapFactory.decodeStream(is, null, options);
                        is.close();

                        //InputStream in = getContentResolver().openInputStream(data.getData());
                        //bm = BitmapFactory.decodeStream(in);
                        //in.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
            bm2 = trimPhoto(bm);
            imageView.setImageBitmap(bm2); // imgView（イメージビュー）を準備しておく
            text.setText("");
            left_turn.setVisibility(View.VISIBLE);
            right_turn.setVisibility(View.VISIBLE);
            isTakePhoto = true;
        }
    }

    /**
     * ビットマップの画像を中央で正方形にトリミングする
     * @return トリミングされた画像
     */
    public Bitmap trimPhoto(Bitmap bm){
        int w = bm.getWidth();
        int h = bm.getHeight();
        float scale = Math.max((float) 500 / w, (float) 500 / h);
        int size = Math.min(w, h);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bm, (w - size) / 2, (h - size) / 2, size, size, matrix, true);
    }

    /**
     * サムネイル用に小さい画像にトリミングする
     * @return トリミングされた画像
     */
    public Bitmap trimPhotoSmoll(Bitmap bm){
        int w = bm.getWidth();
        int h = bm.getHeight();
        float scale = Math.max((float)50/w, (float) 50/h);
        int size = Math.min(w, h);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bm, (w - size) / 2, (h - size) / 2, size, size, matrix, true);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
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
        }

        final File outputDir = getDir("eNet",Context.MODE_PRIVATE);
        //プログレスダイアログの設定
        ProgressDialogFragment dialog = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Title","お待ち下さい");
        bundle.putString("Message","登録中");
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(),"PROGRESS");

        // サーバにアップロード
        UploadAsyncTask uploadAsyncTask = new UploadAsyncTask(this, dialog, outputDir, postMessege[0]);
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

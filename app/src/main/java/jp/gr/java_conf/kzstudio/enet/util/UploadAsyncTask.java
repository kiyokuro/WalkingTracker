package jp.gr.java_conf.kzstudio.enet.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

import jp.gr.java_conf.kzstudio.enet.fragment.ProgressDialogFragment;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class UploadAsyncTask extends AsyncTaskLoader<String> {

    public Context context;
    private String ReceiveStr;
    private File outputDir;
    String[] param;
    ProgressDialogFragment dialog;
    private Uri imageUri;

    public UploadAsyncTask(Context context, ProgressDialogFragment dialog, File outputDir, Uri imageUri) {
        super(context);
        this.dialog = dialog;
        this.outputDir = outputDir;
        this.imageUri = imageUri;
    }

    @Override
    public String loadInBackground() {
        try {
            // パラメータ作成 Activityを継承していないとPriferenceから読み込めないので引数でもらう
            String url = "http://www.project-one.sakura.ne.jp/e-net_api/TanboCameraServer.php";
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("ENCTYPE","multipart/form-data");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            //PictureUtil.outputToFile(PictureUtil.bmPhotoReal,fileName,outputDir);
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            //File file = new File(outputDir + "/"+fileName+".jpeg");
            File file = new File(imageUri.getPath());

            //画像を添付
            multipartEntity.addPart("photo", new FileBody(file));
            //文字を添付
            //multipartEntity.addPart("filename", new StringBody(fileName));//文字列送りたい時に使う

            httpPost.setEntity(multipartEntity);

            ReceiveStr = httpClient.execute(httpPost, responseHandler);

            file.delete();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReceiveStr;
    }
}

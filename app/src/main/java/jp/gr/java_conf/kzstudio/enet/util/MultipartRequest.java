package jp.gr.java_conf.kzstudio.enet.util;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kiyokazu on 2016/10/24.
 */

public class MultipartRequest extends StringRequest {
    private static final String CRLF = "\r\n";

    private final String boundary = "----boundary" + System.currentTimeMillis();

    // テキストパラメータ nameとvalue
    private Map<String, String> textParams = new HashMap<>();
    // バイナリパラメータ nameとファイルパス
    private Map<String, String> binaryParams = new HashMap<>();


    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();

    public MultipartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener/*,
                            Map<String, String> textParams, Map<String, File> fileParams*/) {
        super(Method.POST, url, listener, errorListener);

        //テスト用----------------------------
        /*mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setBoundary("___________________" + Long.toString(System.currentTimeMillis()));
        mBuilder.setCharset(Consts.UTF_8);

        for (Map.Entry<String, String> entry : textParams.entrySet()) {
            mBuilder.addTextBody(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, ?> entry : fileParams.entrySet()) {
            ContentType imageContentType = ContentType.create("image/jpeg");

            if (entry.getValue() instanceof byte[]) {
                Log.i("aaaa","1");
                //Log.d(TAG, "entry.getValue() => byte[]");
                mBuilder.addBinaryBody("uploadFiles", (byte[]) entry.getValue(), imageContentType, entry.getKey());
            } else if (entry.getValue() instanceof File) {
                Log.i("aaaa","2");
                //Log.d(TAG, "entry.getValue() => File");
                mBuilder.addBinaryBody("uploadFiles", (File) entry.getValue(), imageContentType, entry.getKey());
            } else if (entry.getValue() instanceof InputStream) {
                Log.i("aaaa", "3");
                //Log.d(TAG, "entry.getValue() => InputStream");
                //Log.d(TAG, "key: " + entry.getKey());
                mBuilder.addBinaryBody("uploadFiles[]", (InputStream) entry.getValue(), imageContentType, entry.getKey());
            }else {
                Log.i("aaaa","4");
            }
        }
*/
        //テスト用----------------------------
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
        //return mBuilder.build().getContentType().getValue();
    }

    public HttpEntity getEntity() {

        return mBuilder.build();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            writeTextPart(dos);
            writeBinaryPart(dos);
            dos.writeBytes("--" + boundary + "--" + CRLF);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("aaaa",bos.toString());
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        byte[] responseData = response.data;
        try {
            String str = new String(responseData, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }

    private void writeTextPart(DataOutputStream dos) throws IOException {
        for (Map.Entry<String, String> e: textParams.entrySet()) {
            dos.write(encodeToByteArray("Content-Disposition: form-data; name=\"" + e.getKey() + "\"" + CRLF));
            dos.writeBytes(CRLF);
            dos.write(encodeToByteArray(e.getValue() + CRLF));
            dos.writeBytes("--" + boundary + CRLF);
        }
    }

    private void writeBinaryPart(DataOutputStream dos) throws IOException {
        for (Map.Entry<String, String> e: binaryParams.entrySet()) {
            UploadFile uploadFile = new UploadFile(e.getValue());
            dos.write(encodeToByteArray("Content-Disposition: form-data; name=\"" + e.getKey() + "\"; filename=\"" + uploadFile.getName() + "\"" + CRLF));
            dos.writeBytes("Content-Type: " + uploadFile.getMimeType() + CRLF);
            dos.writeBytes(CRLF);
            dos.write(uploadFile.getByteArray()); dos.writeBytes(CRLF);
            dos.writeBytes(CRLF);
            dos.writeBytes("--" + boundary + CRLF);
        }
    }

    public void setTextParams(Map<String, String> textParams) {
        this.textParams = textParams;
    }

    public void setBinaryParams(Map<String, String> binaryParams) {
        this.binaryParams = binaryParams;
    }

    protected byte[] encodeToByteArray(String str) {
        return str.getBytes();
    }
}

class UploadFile {

    private final String filePath;

    UploadFile(String filePath) {
        this.filePath = filePath;
    }

    String getName() {
        Log.i("aaaa",PathUtil.getFileName(filePath));
        return PathUtil.getFileName(filePath);
    }

    String getMimeType() {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = PathUtil.getExtension(filePath);
        return extension != null ? mime.getMimeTypeFromExtension(extension) : null;
    }

    byte[] getByteArray() throws IOException {
        Log.i("aaaa",FileUtil.readAsByteArray(new File(filePath)).toString());
        return FileUtil.readAsByteArray(new File(filePath));
    }
}

class FileUtil {

    public static byte[] readAsByteArray(File file) throws IOException {
        long length = file.length();
        byte[] bytes = new byte[(int)length];

        InputStream is = new FileInputStream(file);
        int offset = 0;
        int numRead;
        while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
}

class PathUtil {

    public static String getFileName(String path) {
        return new File(path).getName();
    }

    public static String getExtension(String path) {
        String extension = null;
        int index = path.lastIndexOf(".");
        if (index > 0) {
            extension = path.substring(index + 1);
        }
        return extension;
    }
}
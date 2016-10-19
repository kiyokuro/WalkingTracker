package jp.gr.java_conf.kzstudio.walkingtracker.util;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class PictureUtil {
    public static Bitmap bmPhoto;
    public static Bitmap bmPhotoReal;
    public static File fileData;//端末のディレクトリのファイルをセットする
    public static String fileName;
    public static String fileNameMealFileUp;

    public static void outputToFile(Bitmap picture, String fileName, File outputDir) {

        File file = new File(outputDir +"/"+ fileName+".jpeg");

        try {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }catch (Exception e){}
    }
}

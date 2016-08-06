package jp.gr.java_conf.kzstudio.walkingtracker.util;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by kiyokazu on 16/08/05.
 */
public class JsonMaker {

    public String makeGpsPointJson(List<GpsPoint> list){
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}

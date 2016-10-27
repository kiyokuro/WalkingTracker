package jp.gr.java_conf.kzstudio.enet.util;

import android.annotation.TargetApi;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonParser {
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ArrayList<String> parseObject(JSONObject json, String element){

        ArrayList<String> data = new ArrayList<>();
        try{
            JSONArray jsonArray = json.getJSONArray("data");
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                data.add(i,jsonObject.getString(element));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ArrayList<String> parseObject(JSONObject json, String jarrayName, String element){

        ArrayList<String> data = new ArrayList<>();
        try{
            JSONArray jsonArray = json.getJSONArray("data").getJSONObject(0).getJSONArray(jarrayName);
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                data.add(i,jsonObject.getString(element));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getTrackData(JSONObject json, String element){

        String trackData = "";
        try{
            trackData = json.getJSONArray("data").getJSONObject(0).getString("track_data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trackData;
    }
}

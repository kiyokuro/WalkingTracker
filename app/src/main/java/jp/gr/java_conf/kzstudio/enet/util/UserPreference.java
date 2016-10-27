package jp.gr.java_conf.kzstudio.enet.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kiyokazu on 16/08/05.
 */
public class UserPreference {

    private Context mContext;
    private String mPrefName;

    public UserPreference(Context context, String prefName){
        this.mContext = context;
        this.mPrefName = prefName;
    }

    public void saveUserPreference(String[] keys, String[] values){
        SharedPreferences preferences = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for(int i=0; i<keys.length; i++){
            editor.putString(keys[i],values[i]);
        }
        editor.apply();
    }

    public String loadUserPreference(String key){
        SharedPreferences preferences = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }
}

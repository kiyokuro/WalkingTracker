package jp.gr.java_conf.kzstudio.enet.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.gr.java_conf.kzstudio.enet.R;


/**
 * Created by kiyokazu on 2016/11/08.
 */

public class WorkContentsCheckList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_contents_check_list);
        setTitle("業務リスト");
    }
}

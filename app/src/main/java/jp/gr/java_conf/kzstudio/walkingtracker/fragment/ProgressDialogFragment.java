package jp.gr.java_conf.kzstudio.walkingtracker.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class ProgressDialogFragment extends DialogFragment {
    ProgressDialog dialog;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getArguments().getString("Title"));
        dialog.setMessage(getArguments().getString("Message"));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMax(100);
        dialog.setProgress(0);
        return dialog;
    }
    public void updateProgress(int value){
        if(dialog != null){
            dialog.setProgress(value);
        }
    }
}

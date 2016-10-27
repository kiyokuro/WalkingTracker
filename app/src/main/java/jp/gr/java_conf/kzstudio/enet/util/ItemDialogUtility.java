package jp.gr.java_conf.kzstudio.enet.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by kiyokazu on 2016/10/19.
 */

public class ItemDialogUtility {
    public interface Listener {
        public void onClickItem(int item);
    }

    public static class ListItem {
        public final String text;
        public final int icon;

        public ListItem(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * アイコン付きダイアログ
     *
     * @param context
     * @param listener
     * @param title
     * @param items
     */
    public static void show(Context context, final Listener listener,
                            String title, ListItem[] items) {

        ListAdapter adapter = new ListAdapter(context, items);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                // ダイアログ閉じる
                dialog.dismiss();
                // リスナーのメソッドを実行
                listener.onClickItem(item);
            }
        });

        dialog.show();
    }

    /**
     * アイコン付き用リストのアダプター
     */
    public static class ListAdapter extends ArrayAdapter<ListItem> {

        private Context mContext = null;

        public ListAdapter(Context context, ListItem[] itmes) {
            super(context, android.R.layout.select_dialog_item,
                    android.R.id.text1, itmes);
            mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ListItem item = this.getItem(position);

            //User super class to create the View
            View v = super.getView(position, convertView, parent);
            TextView tv = (TextView) v.findViewById(android.R.id.text1);

            //Put the image on the TextView
            tv.setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0);

            //Add margin between image and text (support various screen densities)
            int dp5 = (int) (5 *
                    mContext.getResources().getDisplayMetrics().density + 0.5f);
            tv.setCompoundDrawablePadding(dp5);

            return v;

        }
    }
}

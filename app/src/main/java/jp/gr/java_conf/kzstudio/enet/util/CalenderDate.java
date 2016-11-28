package jp.gr.java_conf.kzstudio.enet.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kiyokazu on 16/08/25.
 */
public class CalenderDate {
    Calendar calendar = Calendar.getInstance();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public CalenderDate(){
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    public String[] getTodayDate() {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        String day = String.valueOf(calendar.get(Calendar.DATE));
        return new String[]{year,month,day};
    }
    public String[] getTomorrow(String year, String month, String day) throws ParseException {
        Date dt = df.parse(year+"-"+month+"-"+day);
        calendar.setTime(dt);
        calendar.add(Calendar.DATE,1);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        day = String.valueOf(calendar.get(Calendar.DATE));
        return new String[]{year,month,day};
    }
    public String[] getYesterday(String year, String month, String day) throws ParseException {
        Date dt = df.parse(year + "-" + month + "-" + day);
        calendar.setTime(dt);
        calendar.add(Calendar.DATE,-1);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        day = String.valueOf(calendar.get(Calendar.DATE));
        return new String[]{year,month,day};
    }
    public String[] getNowTime(){
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String miute = String.valueOf(calendar.get(Calendar.MINUTE));
        return new String[]{hour,miute};
    }
}

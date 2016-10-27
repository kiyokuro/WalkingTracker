package jp.gr.java_conf.kzstudio.enet.util;

import java.io.Serializable;

/**
 * Created by kiyokazu on 16/08/03.
 */
public class GpsPoint implements Serializable {
    String order;
    String lan;
    String lon;
    boolean markerExist;
    String time = "";
    String comment = "";
    String checkPointNum;
    boolean photoExist;

    public GpsPoint(String order, String lan, String lon, boolean markerExist, String time, String comment, String checkPointNum, boolean photoExist){
        this.order = order;
        this.lan = lan;
        this.lon = lon;
        this.markerExist = markerExist;
        this.time = time;
        this.comment = comment;
        this.checkPointNum = checkPointNum;
        this.photoExist = photoExist;
    }

    public String getOrder() {
        return order;
    }

    public String getLan() {
        return lan;
    }

    public String getLon() {
        return lon;
    }

    public boolean isMarkerExist() {
        return markerExist;
    }

    public String getTime() {
        return time;
    }

    public String getComment() {
        return comment;
    }

    public String getCheckPointNum() {
        return checkPointNum;
    }

    public boolean isPhotoExist() {
        return photoExist;
    }
}

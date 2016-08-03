package jp.gr.java_conf.kzstudio.walkingtracker.util;

import java.io.Serializable;

/**
 * Created by kiyokazu on 16/08/03.
 */
public class GpsPoint implements Serializable {
    String order;
    String lan;
    String lon;
    boolean markerExist;
    String title = "";
    String comment = "";
    String checkPointNum;

    public GpsPoint(String order, String lan, String lon, boolean markerExist, String title, String comment, String checkPointNum){
        this.order = order;
        this.lan = lan;
        this.lon = lon;
        this.markerExist = markerExist;
        this.title = title;
        this.comment = comment;
        this.checkPointNum = checkPointNum;
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

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getCheckPointNum() {
        return checkPointNum;
    }
}

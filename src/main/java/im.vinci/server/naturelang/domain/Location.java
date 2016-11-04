package im.vinci.server.naturelang.domain;

/**
 * Created by mlc on 2016/7/26.
 */
public class Location {
    private double latitude; //维度
    private double longitude;//经度

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

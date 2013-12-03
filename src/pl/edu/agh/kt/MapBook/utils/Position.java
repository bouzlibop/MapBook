package pl.edu.agh.kt.MapBook.utils;

/**
 * Created with IntelliJ IDEA.
 * User: adba
 * Date: 03.12.13
 */
public class Position {
    private float screenX;
    private float screenY;
    private String latitude;
    private String longitude;

    public Position(float screenX, float screenY, String latitude, String longitude) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getScreenX() {
        return screenX;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public float getScreenY() {
        return screenY;
    }

    public void setScreenY(float screenY) {
        this.screenY = screenY;
    }

    public void setScreenX(float screenX) {
        this.screenX = screenX;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

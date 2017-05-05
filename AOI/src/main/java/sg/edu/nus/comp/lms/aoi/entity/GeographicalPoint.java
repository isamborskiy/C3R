package sg.edu.nus.comp.lms.aoi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.StringTokenizer;

import static java.lang.Math.*;

public class GeographicalPoint {

    public final double lat;
    public final double lng;
    public final double r;

    public GeographicalPoint(double lat, double lng) {
        this(lat, lng, 1);
    }

    public GeographicalPoint(double lat, double lng, double r) {
        this.lat = lat;
        this.lng = lng;
        this.r = r;
    }

    /**
     * Where {@code coordinates} is string the next format "(lat, lng)".
     */
    public GeographicalPoint(String coordinates) {
        StringTokenizer tokenizer = new StringTokenizer(coordinates, ")(, ");
        this.lat = Double.parseDouble(tokenizer.nextToken());
        this.lng = Double.parseDouble(tokenizer.nextToken());
        this.r = 1;
    }

    public static CartesianPoint toCartesianPoint(GeographicalPoint point) {
        double lat = toRadians(point.lat);
        double lng = toRadians(point.lng);

        double x = point.r * sin(lat) * cos(lng);
        double y = point.r * cos(lat) * sin(lng);
        double z = point.r * cos(lat);
        return new CartesianPoint(x, y, z);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @JsonIgnore
    public double getR() {
        return r;
    }

    public CartesianPoint toCartesianPoint() {
        return toCartesianPoint(this);
    }
}

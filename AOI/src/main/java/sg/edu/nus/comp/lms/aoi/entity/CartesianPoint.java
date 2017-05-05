package sg.edu.nus.comp.lms.aoi.entity;

import static java.lang.Math.*;

public class CartesianPoint implements Comparable<CartesianPoint> {

    public final double x;
    public final double y;
    public final double z;

    public CartesianPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static GeographicalPoint toGeographicalPoint(CartesianPoint point) {
        double lat = toDegrees(atan(sqrt(pow(point.x, 2) + pow(point.y, 2)) / point.z));
        double lng = toDegrees(atan(point.y / point.x));
        double r = sqrt(pow(point.x, 2) + pow(point.y, 2) + pow(point.z, 2));
        return new GeographicalPoint(lat, lng, r);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public int compareTo(CartesianPoint point) {
        if (x == point.x) {
            return Double.compare(y, point.y);
        } else {
            return Double.compare(x, point.x);
        }
    }

    public double distance(CartesianPoint point) {
        return sqrt(pow(x - point.x, 2) + pow(y - point.y, 2) + pow(z - point.z, 2));
    }

    public GeographicalPoint toGeographicalPoint() {
        return toGeographicalPoint(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CartesianPoint that = (CartesianPoint) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        return Double.compare(that.z, z) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

package sg.edu.nus.comp.lms.aoi.entity;

import java.util.ArrayList;

public class Polygon extends ArrayList<CartesianPoint> {

    public Polygon() {
        super();
    }

    public boolean contains(CartesianPoint point) {
        if (isEmpty()) {
            return false;
        } else if (size() <= 2) {
            return point.equals(get(0)) || point.equals(get(1));
        } else {
            for (int i = 0; i < size(); i++) {
                CartesianPoint first = get(i);
                CartesianPoint second = get((i + 1) % size());
                if (isLeftTurn(first, second, point) > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean contains(GeographicalPoint point) {
        return contains(point.toCartesianPoint());
    }

    private int isLeftTurn(CartesianPoint a, CartesianPoint b, CartesianPoint c) {
        if (a.equals(c) || b.equals(c)) {
            return 0;
        } else {
            double v = (c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x);
            return v > 0 ? 1 : (v < 0 ? -1 : 0);
        }
    }
}

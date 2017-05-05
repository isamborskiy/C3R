package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.CartesianPoint;
import sg.edu.nus.comp.lms.aoi.entity.UserAOI;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Radius implements AOIFeature {

    @Override
    public String getValue(UserAOI userAOI) {
        double[] radii = new double[userAOI.getPoints().size()];
        for (int i = 0; i < radii.length; i++) {
            List<CartesianPoint> points = userAOI.getPoints().get(i)
                    .stream().map(point -> point.toCartesianPoint())
                    .collect(Collectors.toList());
            double x = 0;
            double y = 0;
            double z = 0;
            for (CartesianPoint point : points) {
                x += point.x;
                y += point.y;
                z += point.z;
            }
            CartesianPoint center = new CartesianPoint(x / points.size(), y / points.size(), z / points.size());
            radii[i] = center.distance(points.get(0));
        }
        return String.format("%.3f", ArraysUtils.getMedian(radii));
    }

    @Override
    public String getName() {
        return "radius";
    }
}

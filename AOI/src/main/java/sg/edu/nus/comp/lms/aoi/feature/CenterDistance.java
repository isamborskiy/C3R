package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.CartesianPoint;
import sg.edu.nus.comp.lms.aoi.entity.UserAOI;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CenterDistance implements AOIFeature {

    @Override
    public String getValue(UserAOI userAOI) {
        List<CartesianPoint> centers = new ArrayList<>(userAOI.getPoints().size());
        for (int i = 0; i < userAOI.getPoints().size(); i++) {
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
            centers.add(new CartesianPoint(x / points.size(), y / points.size(), z / points.size()));
        }

        double[] distances = new double[centers.size() * (centers.size() - 1)];
        int k = 0;
        for (int i = 0; i < centers.size(); i++) {
            CartesianPoint first = centers.get(i);
            for (int j = i + 1; j < centers.size(); j++) {
                distances[k++] = first.distance(centers.get(j));
            }
        }
        return String.format("%.3f", ArraysUtils.getMedian(distances));
    }

    @Override
    public String getName() {
        return "center_distance";
    }
}

package sg.edu.nus.comp.lms.aoi.util;

import sg.edu.nus.comp.lms.domain.weka.distance.EuclideanDistanceNormalized;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DBScanUtils {

    private static final double EPS = Math.ulp(1);

    private DBScanUtils() {
    }

    public static double findEpsilon(Instances instances, int k) {
        if (instances.size() <= 3) {
            return 0;
        }
        List<Double> distanceDistribution = new ArrayList<>();
        for (Instance instance : instances) {
            EuclideanDistanceNormalized distance = new EuclideanDistanceNormalized();
            distance.initialize(instances);
            List<Double> distances = instances.stream()
                    .map(inst -> distance.distance(instance, inst))
                    .collect(Collectors.toList());
            Collections.sort(distances);
            distanceDistribution.addAll(distances.subList(1, k + 1));
        }
        Collections.sort(distanceDistribution);
        return distanceDistribution.get(distanceDistribution.size() - 1) + EPS;
    }
}

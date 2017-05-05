package sg.edu.nus.comp.lms.domain.weka.distance;

import weka.core.Attribute;
import weka.core.Instance;

import java.util.stream.IntStream;

public class EuclideanDistanceNormalized extends NormalizedDistance {

    @Override
    public double distance(Instance first, Instance second) {
        if (first == null || second == null) {
            return 0;
        }
        return Math.sqrt(IntStream.range(0, first.numAttributes())
                .mapToDouble(i -> oneDimensionValue(first, second, first.attribute(i)))
                .sum());
    }

    private double oneDimensionValue(Instance first, Instance second, Attribute attribute) {
        return Math.pow((first.value(attribute) - second.value(attribute)) * attribute.weight(), 2);
    }
}

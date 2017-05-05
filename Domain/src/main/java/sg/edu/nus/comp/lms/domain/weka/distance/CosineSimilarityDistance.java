package sg.edu.nus.comp.lms.domain.weka.distance;

import weka.core.Instance;
import weka.core.Instances;

import java.util.stream.IntStream;

public class CosineSimilarityDistance extends Distance {

    @Override
    public void initialize(Instances instances) {
    }

    @Override
    public double distance(Instance first, Instance second) {
        if (first == null || second == null) {
            return 0;
        }
        double normFirst = norm(first);
        double normSecond = norm(second);
        return 1 - IntStream.range(0, first.numAttributes())
                .mapToDouble(i -> value(first, i) * value(second, i))
                .sum() / (normFirst * normSecond);
    }

    private double norm(Instance instance) {
        return Math.sqrt(IntStream.range(0, instance.numAttributes())
                .mapToDouble(i -> Math.pow(value(instance, i), 2)).sum());
    }
}

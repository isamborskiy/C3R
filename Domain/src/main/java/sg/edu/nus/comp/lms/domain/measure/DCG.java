package sg.edu.nus.comp.lms.domain.measure;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.log;
import static java.lang.Math.pow;

public class DCG implements Measure {

    protected final Map<Integer, Double> classToWeight;
    protected final int k;

    public DCG(double[] userVector, int k) {
        this.classToWeight = IntStream.range(0, userVector.length)
                .mapToObj(i -> i)
                .collect(Collectors.toMap(
                        i -> i,
                        i -> userVector[i]
                ));
        this.k = k;
    }

    @Override
    public double get(int[] recommendation) {
        return IntStream.rangeClosed(1, k)
                .mapToDouble(i -> {
                    try {
                        return getValue(i, classToWeight.get(recommendation[i - 1]));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        return 0.;
                    }
                })
                .sum();
    }

    protected double getValue(int i, double relevance) {
        return (pow(2, relevance) - 1) / (log(i + 1) / log(2));
    }
}

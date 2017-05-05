package sg.edu.nus.comp.lms.domain.measure;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AveragePrecision implements Measure {

    private final double[] userVector;
    private final Set<Integer> relevantDocuments;

    private int k;

    public AveragePrecision(double[] userVector) {
        this.userVector = userVector;
        this.relevantDocuments = IntStream.range(0, userVector.length)
                .mapToObj(i -> i)
                .filter(i -> userVector[i] > 0)
                .collect(Collectors.toSet());
        this.k = userVector.length;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public double get(int[] recommendation) {
        double sum = IntStream.range(1, k)
                .mapToDouble(i -> new Precision(userVector, i).get(recommendation) * (relevantDocuments.contains(i) ? 1 : 0))
                .sum();
        return sum / relevantDocuments.size();
    }
}

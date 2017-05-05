package sg.edu.nus.comp.lms.domain.measure;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Precision implements Measure {

    private final int k;
    private final Set<Integer> relevantDocuments;

    public Precision(double[] userVector, int k) {
        this.k = k;
        this.relevantDocuments = IntStream.range(0, userVector.length)
                .mapToObj(i -> i)
                .sorted((o1, o2) -> Double.compare(userVector[o1], userVector[o2]))
                .limit(k)
                .collect(Collectors.toSet());
    }

    @Override
    public double get(int[] recommendation) {
        Set<Integer> retrievedDocuments = Arrays.stream(recommendation)
                .mapToObj(i -> i)
                .limit(k)
                .filter(relevantDocuments::contains)
                .collect(Collectors.toSet());
        return ((double) retrievedDocuments.size()) / k;
    }
}

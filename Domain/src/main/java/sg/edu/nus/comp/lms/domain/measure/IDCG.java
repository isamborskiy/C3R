package sg.edu.nus.comp.lms.domain.measure;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IDCG extends DCG {

    private final List<Double> values;

    public IDCG(double[] userVector, int k) {
        super(userVector, k);
        this.values = classToWeight.values().stream()
                .sorted((o1, o2) -> -Double.compare(o1, o2))
                .collect(Collectors.toList());
    }

    @Override
    public double get(int[] recommendation) {
        return IntStream.rangeClosed(1, k)
                .mapToDouble(i -> getValue(i, values.get(i - 1)))
                .sum();
    }
}

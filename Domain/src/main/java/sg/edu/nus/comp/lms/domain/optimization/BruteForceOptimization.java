package sg.edu.nus.comp.lms.domain.optimization;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

public class BruteForceOptimization extends AbstractOptimization {

    private final int[] maxParamsMul;

    public BruteForceOptimization(double[] minParams, double[] maxParams, double[] step) {
        super(minParams, maxParams, step);
        this.maxParamsMul = IntStream.range(0, minParams.length)
                .map(i -> (int) ((maxParams[i] - minParams[i]) / step[i]))
                .toArray();
    }

    @Override
    public double[] findParams(Function<double[], Double> model, boolean log) {
        int[] vector = new int[step.length];
        int[] bestVector = Arrays.copyOf(vector, vector.length);
        double bestResult = model.apply(generateParams(vector));

        while (hasNextVector(vector)) {
            nextVector(vector);
            log(log, "Current vector is: " + Arrays.toString(generateParams(vector)));
            double result = model.apply(generateParams(vector));
            if (result > bestResult) {
                bestResult = result;
                bestVector = Arrays.copyOf(vector, vector.length);
                log(log, "Find new maximum: " + bestResult + " (when params " + Arrays.toString(generateParams(bestVector)) + ")");
            }
        }

        log(log, "In conclusion, the best params are: " + Arrays.toString(generateParams(bestVector)));
        return generateParams(bestVector);
    }

    private double[] generateParams(int[] vector) {
        return IntStream.range(0, vector.length)
                .mapToDouble(i -> minParams[i] + vector[i] * step[i])
                .toArray();
    }

    private boolean hasNextVector(int[] vector) {
        return IntStream.range(0, vector.length)
                .filter(i -> vector[i] != maxParamsMul[i])
                .findFirst().isPresent();
    }

    private void nextVector(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            vector[i]++;
            if (vector[i] > maxParamsMul[i]) {
                vector[i] = 0;
            } else {
                break;
            }
        }
    }
}

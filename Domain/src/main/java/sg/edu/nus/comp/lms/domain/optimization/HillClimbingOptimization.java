package sg.edu.nus.comp.lms.domain.optimization;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

public class HillClimbingOptimization extends AbstractOptimization {

    private static final Random RANDOM = new Random();

    private int n = 30;
    private int k = 30;

    public HillClimbingOptimization(double[] minParams, double[] maxParams, double[] step) {
        super(minParams, maxParams, step);
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public double[] findParams(Function<double[], Double> model, boolean log) {
        double[] bestParam = null;
        double bestResult = -1;

        for (int i = 0; i < n; i++) {
            double[] initParams = randomVector();
            double initValue = model.apply(initParams);
            double[] maxParams = initParams;
            double maxValue = initValue;

            for (int j = 0; j < k; j++) {
                for (int l = 0; l < 2 * initParams.length; l++) {
                    double[] params = nextVector(initParams, l);
                    if (params != null) {
                        double value = model.apply(params);
                        if (maxValue <= value) {
                            maxValue = value;
                            maxParams = params;
                            log(log, "Find local maximum: " + value + " (when params " + Arrays.toString(params) + ")");
                        }
                    }
                }
                if (initValue == maxValue) {
                    break;
                }
                initParams = maxParams;
                initValue = maxValue;
                log(log, "Finish iteration: k = " + j + "/" + k);
            }
            if (bestResult < maxValue) {
                bestParam = maxParams;
                bestResult = maxValue;
                log(log, "Iteration maximum: " + bestResult + " (when params " + Arrays.toString(bestParam) + ")");
            }
            log(log, "Finish iteration: n = " + i + "/" + n);
        }

        log(log, "In conclusion, the best params are: " + Arrays.toString(bestParam));
        return bestParam;
    }

    private double[] nextVector(double[] params, int direction) {
        int i = direction / 2;
        if (params[i] == maxParams[i] || params[i] == minParams[i]) {
            return null;
        }

        double[] copiedVector = Arrays.copyOf(params, params.length);
        if (direction % 2 == 0) {
            copiedVector[i] = Math.min(maxParams[i], copiedVector[i] + step[i]);
        } else {
            copiedVector[i] = Math.max(minParams[i], copiedVector[i] - step[i]);
        }
        return copiedVector;
    }

    private double[] randomVector() {
        return IntStream.range(0, minParams.length)
                .mapToDouble(i -> minParams[i] + (RANDOM.nextDouble() * (maxParams[i] - minParams[i])))
                .toArray();
    }
}

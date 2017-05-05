package sg.edu.nus.comp.lms.domain.util;

import org.junit.Test;
import sg.edu.nus.comp.lms.domain.optimization.BruteForceOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Optimization {

    private static double[] constArray(double value, int length) {
        double[] generatedArray = new double[length];
        Arrays.fill(generatedArray, value);
        return generatedArray;
    }

    @Test
    public void hillClimbingTest() {
        double step = 0.01;
        HillClimbingOptimization optimization = new HillClimbingOptimization(
                constArray(-2, 1), constArray(2, 1), constArray(step, 1));
        optimization.setN(30);
        optimization.setK(100);
        double[] bestParams = optimization.findParams(v -> -v[0] * v[0], true);
        assertThat(bestParams[0], allOf(greaterThan(0 - step), lessThan(0 + step)));
    }

    @Test
    public void bruteForceTest() {
        double step = 0.01;
        BruteForceOptimization optimization = new BruteForceOptimization(
                constArray(-2, 1), constArray(2, 1), constArray(step, 1));
        double[] bestParams = optimization.findParams(v -> -v[0] * v[0], true);
        assertThat(bestParams[0], allOf(greaterThan(0 - step), lessThan(0 + step)));
    }
}

package sg.edu.nus.comp.lms.domain.util;

import org.junit.Test;
import sg.edu.nus.comp.lms.domain.measure.DCG;
import sg.edu.nus.comp.lms.domain.measure.IDCG;
import sg.edu.nus.comp.lms.domain.measure.Measure;
import sg.edu.nus.comp.lms.domain.measure.NDCG;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class MeasureTest {

    private static final int ITERATION_NUMBER = 100000;
    private static final int MAX_ARRAY_SIZE = 1000;
    private static final Random RANDOM = new Random();

    @Test
    public void testDCG() {
        double[] userVector = new double[]{1., 0., 2., 2., 3., 5.};
        int[] recommendation = new int[]{5, 4, 3, 2, 0, 1};

        Measure measure = new DCG(userVector, 6);
        Measure idcg = new IDCG(userVector, 6);
        assertThat(measure.get(recommendation), equalTo(idcg.get(recommendation)));
    }

    @Test
    public void testNDCG() {
        double[] userVector = new double[]{1., 0., 2., 2., 3., 5.};
        int[] recommendation = new int[]{5, 4, 3, 2, 0, 1};

        Measure ndcg = new NDCG(userVector, 6);
        assertThat(ndcg.get(recommendation), equalTo(ndcg.get(recommendation)));
    }

    @Test
    public void testRandom() {
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            int length = RANDOM.nextInt(MAX_ARRAY_SIZE) + 1;
            int k = Math.max(RANDOM.nextInt(length), 1);

            double[] userVector = generateDoubleArray(length);
            int[] recommendation = generateRecommendation(length);

            double dcgValue = new DCG(userVector, k).get(recommendation);
            double idcgValue = new IDCG(userVector, k).get(recommendation);

            assertThat(dcgValue, lessThanOrEqualTo(idcgValue));
        }
    }

    private int[] generateRecommendation(int length) {
        List<Integer> recommendation = IntStream.range(0, length).mapToObj(i -> i).collect(Collectors.toList());
        Collections.shuffle(recommendation);
        return recommendation.stream().mapToInt(i -> i).toArray();
    }

    private double[] generateDoubleArray(int length) {
        double[] testArray = new double[length];
        for (int i = 0; i < length; i++) {
            testArray[i] = RANDOM.nextDouble();
        }
        return testArray;
    }
}

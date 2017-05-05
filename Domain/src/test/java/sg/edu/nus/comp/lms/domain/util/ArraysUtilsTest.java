package sg.edu.nus.comp.lms.domain.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArraysUtilsTest {

    private static final int ITERATION_NUMBER = 1000;
    private static final int MAX_ARRAY_SIZE = 1000;
    private static final Random RANDOM = new Random();

    @Test
    public void randomTest() {
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            double[] testArray = generateArray(1 + RANDOM.nextInt(MAX_ARRAY_SIZE));
            int k = RANDOM.nextInt(testArray.length);
            double kthElement = ArraysUtils.kthElement(testArray, k);

            Arrays.sort(testArray);
            assertThat(kthElement, equalTo(testArray[k]));
        }
    }

    @Test(expected = RuntimeException.class)
    public void errorTest() {
        double[] testArray = generateArray(RANDOM.nextInt(MAX_ARRAY_SIZE));
        int k = testArray.length;
        double kthElement = ArraysUtils.kthElement(testArray, k);

        Arrays.sort(testArray);
        assertThat(kthElement, equalTo(testArray[k]));
    }

    @Test(timeout = 2000)
    public void uniformTest() {
        double[] testArray = new double[RANDOM.nextInt(MAX_ARRAY_SIZE)];
        Arrays.fill(testArray, RANDOM.nextDouble());
        int k = RANDOM.nextInt(testArray.length);
        double kthElement = ArraysUtils.kthElement(testArray, k);

        Arrays.sort(testArray);
        assertThat(kthElement, equalTo(testArray[k]));
    }

    private double[] generateArray(int length) {
        double[] testArray = new double[length];
        for (int i = 0; i < length; i++) {
            testArray[i] = RANDOM.nextDouble();
        }
        return testArray;
    }
}

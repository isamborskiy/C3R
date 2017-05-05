package sg.edu.nus.comp.lms.domain.util;

import java.util.Arrays;
import java.util.Random;

public class ArraysUtils {

    private static final Random RANDOM = new Random();

    private ArraysUtils() {
    }

    public static double kthElement(double[] array, int k) {
        return kthElementInPlace(Arrays.copyOf(array, array.length), k);
    }

    public static double kthElementInPlace(double[] array, int k) {
        if (k >= array.length) {
            throw new RuntimeException("Can not find " + k + "-th element of array, where length is " + array.length);
        }

        int left = 0;
        int right = array.length - 1;

        while (true) {
            int middle = partition(array, left, right);
            if (middle < k) {
                left = middle + 1;
            } else if (middle > k) {
                right = middle - 1;
            } else {
                return array[k];
            }
        }
    }

    private static int partition(double[] array, int left, int right) {
        if (left != right) {
            swap(array, left + RANDOM.nextInt(right - left), right);
        }
        double x = array[right];
        int i = left - 1;
        for (int j = left; j <= right; j++) {
            if (array[j] <= x) {
                swap(array, ++i, j);
            }
        }
        return i;
    }

    public static void swap(double[] array, int i, int j) {
        double tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static <T> int indexOf(T[] array, T element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static double getMedian(double[] array) {
        return getMedianInPlace(Arrays.copyOf(array, array.length));
    }

    public static double getMedianInPlace(double[] array) {
        if (array.length == 0) {
            return 0;
        }
        Arrays.sort(array);
        if (array.length % 2 == 0) {
            return (array[array.length / 2] + array[array.length / 2 - 1]) / 2;
        } else {
            return array[array.length / 2];
        }
    }

    public static double[] normalize(double[] array) {
        double min = Arrays.stream(array).min().getAsDouble();
        double range = Arrays.stream(array).max().getAsDouble() - min;
        if (range != 0) {
            return Arrays.stream(array).map(value -> (value - min) / range).toArray();
        } else {
            return Arrays.copyOf(array, array.length);
        }
    }
}

package sg.edu.nus.comp.lms.model;

import java.util.Map;
import java.util.stream.IntStream;

public abstract class Recommender {

    public abstract void train(Map<String, double[]> usersVectors);

    public abstract boolean canRecommend(String id);

    public abstract int[] recommend(String id);

    protected void add(double[] first, double[] second) {
        add(first, second, 1);
    }

    protected void add(double[] first, double[] second, double alpha) {
        for (int i = 0; i < first.length; i++) {
            first[i] += alpha * second[i];
        }
    }

    protected int[] sortDistribution(double[] distribution) {
        return IntStream.range(0, distribution.length)
                .mapToObj(i -> i)
                .sorted((o1, o2) -> -Double.compare(distribution[o1], distribution[o2]))
                .mapToInt(i -> i)
                .toArray();
    }

    protected int[] sortDistribution(double[] userDistribution, double[] distribution) {
        return IntStream.range(0, distribution.length)
                .mapToObj(i -> i)
                .sorted((o1, o2) -> {
                    if (distribution[o1] == distribution[o2] && distribution[o1] != 0) {
                        return -Double.compare(userDistribution[o1], userDistribution[o2]);
                    } else {
                        return -Double.compare(distribution[o1], distribution[o2]);
                    }
                })
                .mapToInt(i -> i)
                .toArray();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

package sg.edu.nus.comp.lms;

import com.opencsv.CSVWriter;
import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.measure.Measure;
import sg.edu.nus.comp.lms.domain.measure.NDCG;
import sg.edu.nus.comp.lms.model.multi.c3r.LCapC3R;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CombinationOfLayers {

    private static final int K = 20;
    private static final DoubleMatrix MATRIX = new DoubleMatrix(5, 5,
            0.000000, 0.846154, 0.980411, 0.718462, 0.836102,
            0.846154, 0.000000, 1.006731, 0.809419, 0.961554,
            0.980411, 1.006731, 0.000000, 0.987747, 0.897505,
            0.718462, 0.809419, 0.987747, 0.000000, 0.863983,
            0.836102, 0.961554, 0.897505, 0.863983, 0.000000);

    public static void main(String[] args) throws IOException {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        Map<String, double[]> testData = CommonMethods.getTest2();
        Map<String, double[]> trainData = CommonMethods.getTrain3(testData.keySet());

        Vector[] vectors = new Vector[]{
                new Vector(1, 0, 1, 1, 1),
                new Vector(0, 1, 1, 1, 1),
                new Vector(1, 1, 1, 1, 1)
        };

        Map<String, List<Double>> ndcgResults = new HashMap<>();
        Map<String, List<Double>> pResults = new HashMap<>();

        for (Vector vector : vectors) {
            int[] layers = vector.layers();

            LCapC3R recommender;
            if (layers.length == 5) {
                recommender = new LCapC3R(sources);
            } else {
                List<Instances> copiedSources = Arrays.stream(layers)
                        .mapToObj(i -> new Instances(sources.get(i)))
                        .collect(Collectors.toList());
                recommender = new LCapC3R(copiedSources);
            }
            recommender.setWeights(getCoefMatrix(vector));
            recommender.setClusterNumber(10);
            recommender.setAlpha(0.13990390031709746);
            recommender.setBeta(0.7758022350152981);
            recommender.setGamma(0.021026137984037532);
            recommender.setK(31);
            recommender.train(trainData);

            String networks = vector.toSN();
            System.out.println(networks + " finish recommending...");

            Map<String, int[]> recommendations = new HashMap<>();
            for (String userId : testData.keySet()) {
                if (recommender.canRecommend(userId)) {
                    recommendations.put(userId, recommender.recommend(userId));
                }
            }

            // NDCG
            {
                List<Double> ndcgResult = new ArrayList<>();
                for (int k = 1; k < K; k++) {
                    double ndcg = 0;
                    for (String userId : recommendations.keySet()) {
                        Measure measure = new NDCG(testData.get(userId), k);
                        ndcg += measure.get(recommendations.get(userId));
                    }
                    ndcgResult.add(ndcg / recommendations.size());
                }
                ndcgResults.put(networks, ndcgResult);
                System.out.println(networks + " finish ndcg...");
            }

            // P@K
            {
                List<Double> pResult = new ArrayList<>();
                for (int k = 1; k < K; k++) {
                    double p = 0.;
                    for (String userId : recommendations.keySet()) {
                        p += averagePrecision(recommendations.get(userId), testData.get(userId), k);
                    }
                    pResult.add(p / recommendations.size());
                }
                pResults.put(networks, pResult);
                System.out.println(networks + " finish P...");
            }
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(Settings.CITY_NAME + "_compinations_ndcg.csv"))) {
            String[] headers = Arrays.stream(vectors).map(Vector::toSN).toArray(String[]::new);
            writer.writeNext(headers);
            int size = ndcgResults.get(headers[0]).size();

            for (int i = 0; i < size; i++) {
                final int index = i;
                String[] values = Arrays.stream(headers)
                        .map(networks -> ndcgResults.get(networks).get(index))
                        .map(String::valueOf)
                        .toArray(String[]::new);
                writer.writeNext(values);
            }
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(Settings.CITY_NAME + "_compinations_p.csv"))) {
            String[] headers = Arrays.stream(vectors).map(Vector::toSN).toArray(String[]::new);
            writer.writeNext(headers);
            int size = pResults.get(headers[0]).size();

            for (int i = 0; i < size; i++) {
                final int index = i;
                String[] values = Arrays.stream(headers)
                        .map(networks -> pResults.get(networks).get(index))
                        .map(String::valueOf)
                        .toArray(String[]::new);
                writer.writeNext(values);
            }
        }
    }

    private static double averagePrecision(int[] recommended, double[] realValues, int k) {
        int[] real = sortedIndeces(realValues);
        double ap = 0.;
        int count = 0;
        for (int i = 0; i < k; i++) {
            if (recommended[i] == real[i]) {
                count++;
                ap += (((double) count) / (i + 1));
            }
        }
        return count == 0 ? 0 : ap / count;
    }

    private static int[] sortedIndeces(double[] values) {
        return IntStream.range(0, values.length)
                .boxed()
                .sorted((o1, o2) -> -Double.compare(values[o1], values[o2]))
                .mapToInt(i -> i)
                .toArray();
    }

    private static DoubleMatrix getCoefMatrix(Vector vector) {
        int size = vector.layers().length;
        double[] data = new double[size * size];
        int i = 0;
        for (int x = 0; x < vector.data.length; x++) {
            if (vector.data[x] == 1) {
                for (int y = 0; y < vector.data.length; y++) {
                    if (vector.data[y] == 1) {
                        data[i++] = MATRIX.get(x, y);
                    }
                }
            }
        }
        return new DoubleMatrix(size, size, data);
    }

    private static class Vector {

        public int[] data;

        public Vector(int n) {
            this.data = new int[n];
        }

        public Vector(int... data) {
            this.data = data;
        }

        public int[] next() {
            for (int i = data.length - 1; i >= 0; i--) {
                data[i] = (data[i] + 1) % 2;
                if (data[i] == 1) {
                    break;
                }
            }
            return data;
        }

        public int[] layers() {
            return IntStream.range(0, data.length).filter(i -> data[i] == 1).toArray();
        }

        public boolean hasNext() {
            return Arrays.stream(data).filter(i -> i == 0).count() != 0;
        }

        public String toSN() {
            return Arrays.stream(layers())
                    .mapToObj(i -> Settings.MODAL_SHORT_NAME[i])
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining(" + "));
        }

        @Override
        public String toString() {
            return Arrays.stream(data).mapToObj(String::valueOf).collect(Collectors.joining(" "));
        }
    }
}
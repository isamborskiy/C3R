package sg.edu.nus.comp.lms;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.measure.Measure;
import sg.edu.nus.comp.lms.domain.measure.NDCG;
import sg.edu.nus.comp.lms.domain.weka.operation.MergeInstances;
import sg.edu.nus.comp.lms.model.multi.SimpleClusteringRecommender;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleClusteringRecommenderTest {

    private static final String CLUSTERS_FILE = Settings.CITY_NAME + "_hierarchical.csv";

    public static void main(String[] args) throws IOException {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();
        Instances mergedInstances = sources.get(0);
        for (int i = 1; i < sources.size(); i++) {
            mergedInstances = new MergeInstances(mergedInstances, sources.get(i)).eval();
        }

        Map<String, double[]> testData = CommonMethods.getTest2();
        Map<String, double[]> trainData = CommonMethods.getTrain3(testData.keySet());

        int size = trainData.values().stream().findFirst().get().length;

        Map<String, Integer> idToCluster;
        try (CSVReader reader = new CSVReader(new FileReader(CLUSTERS_FILE))) {
            reader.readNext(); //skip header
            idToCluster = reader.readAll().stream()
                    .collect(Collectors.toMap(
                            arr -> arr[0],
                            arr -> {
                                if (arr[1] != null && !arr[1].isEmpty()) {
                                    return Integer.parseInt(arr[1]);
                                } else {
                                    return -1;
                                }
                            }
                    ));
        }

        SimpleClusteringRecommender recommender = new SimpleClusteringRecommender(mergedInstances, idToCluster);
        recommender.setBeta(1);
        recommender.setGamma(0.011507798699552036);
        recommender.train(trainData);

        Map<String, int[]> recommendations = new HashMap<>();
        for (String userId : testData.keySet()) {
            if (recommender.canRecommend(userId)) {
                recommendations.put(userId, recommender.recommend(userId));
            }
        }

        // NDCG
        List<Double> ndcgResult = new ArrayList<>();
        for (int k = 1; k < size; k++) {
            double ndcg = 0;
            for (String userId : recommendations.keySet()) {
                Measure measure = new NDCG(testData.get(userId), k);
                ndcg += measure.get(recommendations.get(userId));
            }
            ndcgResult.add(ndcg / recommendations.size());
        }
        try (PrintWriter writer = new PrintWriter(Settings.CITY_NAME + "_" + recommender.toString())) {
            ndcgResult.forEach(writer::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish NDCG for " + recommender.toString());

        // P@K
        List<Double> pResults = new ArrayList<>();
        for (int k = 1; k < size; k++) {
            double p = 0.;
            for (String userId : recommendations.keySet()) {
                p += averagePrecision(recommendations.get(userId), testData.get(userId), k);
            }
            pResults.add(p / recommendations.size());
        }
        try (PrintWriter writer = new PrintWriter(Settings.CITY_NAME + "_" + recommender.toString() + "_precision")) {
            pResults.forEach(writer::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish Precision for " + recommender.toString());

        // HitRatio@K
        List<Double> hrResults = new ArrayList<>();
        for (int k = 1; k < size; k++) {
            hrResults.add(hitRatio(recommendations, testData, k));
        }
        try (PrintWriter writer = new PrintWriter(Settings.CITY_NAME + "_" + recommender.toString() + "_hr")) {
            hrResults.forEach(writer::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish Hit Ratio for " + recommender.toString());
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

    private static double hitRatio(Map<String, int[]> recommended, Map<String, double[]> realValues, int k) {
        Map<String, Set<Integer>> real = realValues.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> Arrays.stream(sortedIndeces(realValues.get(id)))
                                .limit(k)
                                .boxed()
                                .collect(Collectors.toSet())
                ));
        double totalHR = 0.;
        for (int i = 0; i < k; i++) {
            double hr = 0.;
            for (String userId : recommended.keySet()) {
                hr += real.get(userId).contains(recommended.get(userId)[i]) ? 1 : 0;
            }
            totalHR += (hr == 0 ? 0 : hr / recommended.size());
        }
        return totalHR == 0 ? 0 : totalHR / k;
    }

    private static int[] sortedIndeces(double[] values) {
        return IntStream.range(0, values.length)
                .boxed()
                .sorted((o1, o2) -> -Double.compare(values[o1], values[o2]))
                .mapToInt(i -> i)
                .toArray();
    }
}

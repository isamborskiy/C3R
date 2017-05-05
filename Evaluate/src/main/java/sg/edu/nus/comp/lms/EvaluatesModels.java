package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.measure.Measure;
import sg.edu.nus.comp.lms.domain.measure.NDCG;
import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EvaluatesModels {

    public static void main(String[] args) {
//        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        Map<String, double[]> testData = CommonMethods.getTest2();
        Map<String, double[]> trainData = CommonMethods.getTrain3(testData.keySet());

        int size = trainData.values().stream().findFirst().get().length;

        List<Recommender> recommenders = new ArrayList<>();
//        recommenders.add(new FM());
        recommenders.add(EvaluateMethods.getSVD());
//        recommenders.add(EvaluateMethods.getPopular(copy(sources), 1));
//        recommenders.add(EvaluateMethods.getPopularHistory(copy(sources), 1));
//        recommenders.add(EvaluateMethods.getCFPlusPopular(copy(sources), 1));
//        recommenders.add(EvaluateMethods.getCFSingle(copy(sources), 1));
//        recommenders.add(EvaluateMethods.getCFEarlyFusion(copy(sources)));
//        recommenders.add(EvaluateMethods.getCFLateFusion(copy(sources)));
//        recommenders.add(EvaluateMethods.getC3RWithoutRegularizationModel(copy(sources)));
//        recommenders.add(EvaluateMethods.getC3RModel(copy(sources)));
//        recommenders.add(EvaluateMethods.getLCapC3RModel(copy(sources)));

        recommenders.stream()
                .forEach(recommender -> {
                    long trainTime = System.currentTimeMillis();
                    recommender.train(trainData);
                    trainTime = System.currentTimeMillis() - trainTime;
                    System.out.println("Finish training for " + recommender.toString());

                    long totalTime = 0;
                    Map<String, int[]> recommendations = new HashMap<>();
                    for (String userId : testData.keySet()) {
                        if (recommender.canRecommend(userId)) {
                            long time = System.nanoTime();
                            int[] rec = recommender.recommend(userId);
                            time = System.nanoTime() - time;
                            totalTime += time;
                            recommendations.put(userId, rec);
                            System.out.format("%d/%d\n", recommendations.size(), testData.size());
                        }
                    }
                    long recommendTime = totalTime / recommendations.size();
                    System.out.println("Finish recommendation for " + recommender.toString());

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

                    // MAP
                    /*double map = 0.;
                    for (String userId : recommendations.keySet()) {
                        map += averagePrecision(recommendations.get(userId), testData.get(userId), size);
                    }
                    map /= recommendations.size();
                    System.out.println("Finish MAP for " + recommender.toString());
                    */
                    try (PrintWriter writer = new PrintWriter(Settings.CITY_NAME + "_" + recommender.toString() + "_meta")) {
//                        writer.format("map = %f\n", map);
                        writer.format("train time = %dms\n", trainTime);
                        writer.format("recommend time = %dns\n", recommendTime);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
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

    private static List<Instances> copy(List<Instances> sources) {
        return sources.stream()
                .map(Instances::new)
                .collect(Collectors.toList());
    }
}

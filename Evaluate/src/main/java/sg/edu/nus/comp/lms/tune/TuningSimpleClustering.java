package sg.edu.nus.comp.lms.tune;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;
import sg.edu.nus.comp.lms.domain.weka.operation.MergeInstances;
import sg.edu.nus.comp.lms.model.multi.SimpleClusteringRecommender;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TuningSimpleClustering {

    private static final String CLUSTERS_FILE = Settings.CITY_NAME + "_xmeans.csv";

    public static void main(String[] args) throws IOException {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());
        List<Instances> sources = CommonMethods.readAllIntersectedSources();
        Instances mergedInstances = sources.get(0);
        for (int i = 1; i < sources.size(); i++) {
            mergedInstances = new MergeInstances(mergedInstances, sources.get(i)).eval();
        }
        Map<String, Integer> idToCluster;
        try (CSVReader reader = new CSVReader(new FileReader(CLUSTERS_FILE))) {
            reader.readNext(); //skip header
            idToCluster = reader.readAll().stream()
                    .collect(Collectors.toMap(
                            arr -> arr[0],
                            arr -> Integer.parseInt(arr[1])
                    ));
        }

        AbstractOptimization optimization = new HillClimbingOptimization(
                new double[]{0.},
                new double[]{0.2},
                new double[]{0.01}
        );
        ((HillClimbingOptimization) optimization).setN(30);
        ((HillClimbingOptimization) optimization).setK(5);

        SimpleClusteringRecommender model = new SimpleClusteringRecommender(mergedInstances, idToCluster);
        Function<double[], Double> function = params -> {
            model.setBeta(1);
            model.setGamma(params[0]);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

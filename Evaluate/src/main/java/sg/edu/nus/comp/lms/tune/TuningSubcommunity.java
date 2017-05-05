package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.algorithm.multi.SpectralMultiLayerClustering;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;
import sg.edu.nus.comp.lms.model.multi.SubcommunityRecommender;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TuningSubcommunity {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        AbstractOptimization optimization = new HillClimbingOptimization(
                // multi clusters, alpha, single clusters
                new double[]{1, 0., 1},
                new double[]{31, 1., 31},
                new double[]{3, 0.05, 3}
        );
        ((HillClimbingOptimization) optimization).setN(30);
        ((HillClimbingOptimization) optimization).setK(30);

        SubcommunityRecommender model = new SubcommunityRecommender(sources, 1);
        Function<double[], Double> function = params -> {
            SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory multiFactory =
                    new SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory(params[1]);
            model.setMultiClusterNumber((int) params[0]);
            model.setSingleClusterNumber((int) params[2]);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;
import sg.edu.nus.comp.lms.model.multi.c3r.C3R;
import sg.edu.nus.comp.lms.model.multi.c3r.C3RRecommender;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TuningC3R {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        AbstractOptimization optimization = new HillClimbingOptimization(
                // clusters, alpha, beta, gamma, kNN
                new double[]{1, 0., 0.},
                new double[]{31, 1., 1.},
                new double[]{3, 0.05, 0.05}
        );
        ((HillClimbingOptimization) optimization).setN(30);
        ((HillClimbingOptimization) optimization).setK(30);

        C3RRecommender model = new C3R(sources);
        Function<double[], Double> function = params -> {
            model.setClusterNumber((int) params[0]);
            model.setAlpha(0);
            model.setBeta(params[1]);
            model.setGamma(params[2]);
            model.setK(30);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

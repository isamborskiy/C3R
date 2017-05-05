package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;
import sg.edu.nus.comp.lms.model.single.SVD;
import sg.edu.nus.comp.lms.util.EvaluateMethods;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class TuningSVD {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());

        AbstractOptimization optimization = new HillClimbingOptimization(
                new double[]{0, 0, 0, 0, 1, 1},
                new double[]{1, 1, 1, 1, 500, 50},
                new double[]{0.01, 0.01, 0.01, 0.01, 5, 1}
        );
        ((HillClimbingOptimization) optimization).setN(30);
        ((HillClimbingOptimization) optimization).setK(30);

        SVD model = new SVD();
        Function<double[], Double> function = params -> {
            model.setLearningRate(params[0]);
            model.setPreventOverfitting(params[1]);
            model.setRandomNoise(params[2]);
            model.setLearningRateDecay(params[3]);
            model.setIterationNumber((int) params[4]);
            model.setFeaturesNumber((int) params[5]);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

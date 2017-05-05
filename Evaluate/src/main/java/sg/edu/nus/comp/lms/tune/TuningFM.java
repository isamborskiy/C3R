package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.BruteForceOptimization;
import sg.edu.nus.comp.lms.model.single.FM;
import sg.edu.nus.comp.lms.util.EvaluateMethods;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class TuningFM {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());

        AbstractOptimization optimization = new BruteForceOptimization(
                new double[]{25},
                new double[]{250},
                new double[]{25}
        );

        Function<double[], Double> function = params -> {
            FM model = new FM();
            model.setIterationNumber((int) params[0]);
            model.train(trainData);
            double res = EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
            System.out.println("res = " + res);
            return res;
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

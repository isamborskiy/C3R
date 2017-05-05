package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.BruteForceOptimization;
import sg.edu.nus.comp.lms.model.single.CFPlusPopular;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class TuningCFPlusPopular {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());
        Instances source = CommonMethods.readAllIntersectedSources().get(1);

        AbstractOptimization optimization = new BruteForceOptimization(
                // beta
                new double[]{0.},
                new double[]{1.},
                new double[]{0.05}
        );

        CFPlusPopular model = new CFPlusPopular(source);
        Function<double[], Double> function = params -> {
            model.setAlpha(1);
            model.setBeta(params[0]);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

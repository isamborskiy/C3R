package sg.edu.nus.comp.lms.tune;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.optimization.AbstractOptimization;
import sg.edu.nus.comp.lms.domain.optimization.HillClimbingOptimization;
import sg.edu.nus.comp.lms.model.multi.LateFusion;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TuningLateFusion {

    public static void main(String[] args) {
        Map<String, double[]> testData = CommonMethods.getTest1();
        Map<String, double[]> trainData = CommonMethods.getTrain2(testData.keySet());
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        AbstractOptimization optimization = new HillClimbingOptimization(
                new double[]{0., 0., 0., 0., 0., 1},
                new double[]{1., 1., 1., 1., 1., 31},
                new double[]{0.05, 0.05, 0.05, 0.05, 0.05, 1}
        );
        ((HillClimbingOptimization) optimization).setN(30);
        ((HillClimbingOptimization) optimization).setK(30);

        LateFusion model = new LateFusion(sources);
        Function<double[], Double> function = params -> {
            model.setWeights(Arrays.copyOf(params, params.length - 1));
            model.setK((int) params[params.length - 1]);
            model.train(trainData);
            return EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);
        };

        double[] params = optimization.findParams(function, true);
        System.out.println(Arrays.toString(params));
    }
}

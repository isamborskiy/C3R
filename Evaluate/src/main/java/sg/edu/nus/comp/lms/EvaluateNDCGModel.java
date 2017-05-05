package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class EvaluateNDCGModel {

    public static void main(String[] args) throws Exception {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        Map<String, double[]> testData = CommonMethods.getTest2();
        Map<String, double[]> trainData = CommonMethods.getTrain3(testData.keySet());

        Recommender model = EvaluateMethods.getC3RWithoutRegularizationModel(sources);
        model.train(trainData);

        int upperBound = testData.values().stream().findFirst().orElse(new double[0]).length;
        try (PrintWriter writer = new PrintWriter(model.toString() + "_result_ndcg")) {
            for (int ndcgParam = 1; ndcgParam < upperBound; ndcgParam++) {
                writer.println(EvaluateMethods.evaluateModelNDCG(model, testData, ndcgParam));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

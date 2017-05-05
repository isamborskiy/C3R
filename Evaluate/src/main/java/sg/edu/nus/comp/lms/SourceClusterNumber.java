package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.model.multi.c3r.C3RSingle;
import sg.edu.nus.comp.lms.util.EvaluateMethods;
import weka.core.Instances;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SourceClusterNumber {

    public static void main(String[] args) {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        Map<String, double[]> testData = CommonMethods.getTest2();
        Map<String, double[]> trainData = CommonMethods.getTrain3(testData.keySet());

        IntStream.range(0, sources.size())
                .parallel()
                .forEach(layerIndex -> {
                    int upperBound = (int) Math.sqrt(sources.get(layerIndex).size());
                    double maxMeasure = 0;
                    double maxK = 1;
                    for (int k = 1; k < upperBound; k++) {
                        C3RSingle model = new C3RSingle(sources, layerIndex);
                        model.setClusterNumber(k);
                        model.setAlpha(0.575957787680769);
                        model.setBeta(0.5107696219874466);
                        model.setGamma(0.008417318465459525);
                        model.setK(31);
                        model.setLaplacianFunction(SpectralClusteringUtils.getLaplacianFunction());
                        model.setEigenvectorFunction(SpectralClusteringUtils.getEigenvectorFunction(k));
                        model.train(trainData);

                        double measure = EvaluateMethods.evaluateModelNDCG(model, testData, Settings.DEFAULT_NDCG_K);

                        System.out.println("For layer " + Settings.MODAL_NAME[layerIndex] + " and K = " + k + " nDCG = " + measure);
                        if (measure > maxMeasure) {
                            maxMeasure = measure;
                            maxK = k;
                        }
                    }
                    System.out.println("In conclusion for layer " + Settings.MODAL_NAME[layerIndex] + " and K = " + maxK + " max nDCG = " + maxMeasure);
                });
    }
}

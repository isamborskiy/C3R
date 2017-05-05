package sg.edu.nus.comp.lms.model.multi;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.model.MultiSourceRecommenderFactory;
import sg.edu.nus.comp.lms.model.single.CFSingleLayer;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LateFusion extends MultiSourceRecommender {

    private int k = Settings.DEFAULT_K;
    private double[] weights;

    private List<CFSingleLayer> singleModels;
    private Map<String, int[]> idToRecommendation;

    public LateFusion(List<Instances> sources) {
        super(sources);
        this.weights = new double[sources.size()];
        Arrays.fill(weights, 1);
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        if (singleModels == null) {
            singleModels = sources.stream()
                    .map(instances -> {
                        CFSingleLayer model = new CFSingleLayer(instances);
                        model.setK(k);
                        model.train(usersVectors);
                        return model;
                    }).collect(Collectors.toList());
        }

        idToRecommendation = usersVectors.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> {
                            double[] distribution = new double[usersVectors.get(id).length];
                            for (int i = 0; i < singleModels.size(); i++) {
                                double weight = weights[i];
                                int[] recommendation = singleModels.get(i).recommend(id);
                                for (int j = 0; j < recommendation.length; j++) {
                                    distribution[recommendation[j]] += (recommendation.length - j) * weight;
                                }
                            }
                            return sortDistribution(distribution);
                        }
                ));
    }

    @Override
    public boolean canRecommend(String id) {
        return ids.contains(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }

    public static class LateFusionFactory implements MultiSourceRecommenderFactory {

        private int k = Settings.DEFAULT_K;
        private double[] weights;

        public void setK(int k) {
            this.k = k;
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        @Override
        public LateFusion create(List<Instances> sources) {
            LateFusion model = new LateFusion(sources);
            model.setK(k);
            if (weights != null) {
                model.setWeights(weights);
            }
            return model;
        }
    }
}

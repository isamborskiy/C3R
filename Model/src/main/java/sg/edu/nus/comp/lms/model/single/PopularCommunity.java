package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.distance.Distance;
import sg.edu.nus.comp.lms.domain.weka.distance.EuclideanDistance;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PopularCommunity extends SingleSourceRecommender {

    private int k = Settings.DEFAULT_K;
    private double beta = 1;
    private double gamma = 0;

    private Map<String, int[]> idToRecommendation;

    public PopularCommunity(Instances instances) {
        super(instances);
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        idToRecommendation = usersVectors.keySet().stream()
                .filter(ids::contains)
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> {
                            Instance instance = InstancesUtils.findInstance(instances, Settings.ID_ATTR, id);
                            Distance distance = new EuclideanDistance();
                            distance.initialize(instances);
                            Collections.sort(instances, (o1, o2) -> Double.compare(distance.distance(instance, o1),
                                    distance.distance(instance, o2)));

                            double[] userDistribution = usersVectors.get(id);
                            double[] communityDistribution = getCommunityDistribution(usersVectors, id, instances);

                            return sortDistribution(userDistribution, communityDistribution);
                        }
                ));
    }

    private double[] getCommunityDistribution(Map<String, double[]> usersVectors, String id, Instances instances) {
        Attribute idAttribute = instances.attribute(Settings.ID_ATTR);
        Set<String> nearestIds = instances.stream().limit(k)
                .map(i -> i.stringValue(idAttribute))
                .collect(Collectors.toSet());

        double[] userVector = usersVectors.get(id);
        double[] distribution = new double[userVector.length];
        add(distribution, userVector, beta);
        usersVectors.keySet().stream()
                .filter(nearestIds::contains)
                .filter(userId -> !userId.equals(id))
                .map(usersVectors::get)
                .forEach(vector -> add(distribution, vector, gamma));
        return distribution;
    }

    @Override
    public boolean canRecommend(String id) {
        return idToRecommendation.containsKey(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }

    public static class PopularCommunityFactory implements RecommenderFactory {

        private int k = Settings.DEFAULT_K;
        private double beta = 1;
        private double gamma = 0;

        public void setK(int k) {
            this.k = k;
        }

        public void setBeta(double beta) {
            this.beta = beta;
        }

        public void setGamma(double gamma) {
            this.gamma = gamma;
        }

        @Override
        public PopularCommunity create(Instances instances) {
            PopularCommunity model = new PopularCommunity(instances);
            model.setK(k);
            model.setBeta(beta);
            model.setGamma(gamma);
            return model;
        }
    }
}

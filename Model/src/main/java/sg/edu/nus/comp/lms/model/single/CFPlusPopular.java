package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.domain.weka.distance.CosineSimilarityDistance;
import sg.edu.nus.comp.lms.domain.weka.distance.Distance;
import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CFPlusPopular extends CFRecommender {

    private double alpha = 1.;
    private double beta = 0.;

    public CFPlusPopular(Instances instances) {
        super(instances);
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        Distance distance = new CosineSimilarityDistance();
        distance.initialize(instances);
        idToRecommendation = usersVectors.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> {
                            Instance instance = idToInstance.get(id);
                            Map<String, Instance> nearestUsers = idToInstance.keySet().stream()
                                    .filter(usersVectors::containsKey)
                                    .sorted((o1, o2) -> Double.compare(distance.distance(instance, idToInstance.get(o1)),
                                            distance.distance(instance, idToInstance.get(o2))))
                                    .limit(k + 1) // cause [0] is {@code id} user
                                    .collect(Collectors.toMap(
                                            Function.identity(),
                                            idToInstance::get
                                    ));
                            nearestUsers.remove(id);

                            double[] distribution = new double[usersVectors.get(id).length];
                            add(distribution, usersVectors.get(id), alpha);
                            for (String userId : nearestUsers.keySet()) {
                                add(distribution, usersVectors.get(userId), beta);
                            }

                            return sortDistribution(distribution);
                        }
                ));
    }

    public static class CFPlusPopularFactory implements RecommenderFactory {

        private double alpha = 0.;
        private double beta = 0.;

        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        public void setBeta(double beta) {
            this.beta = beta;
        }

        @Override
        public CFPlusPopular create(Instances instances) {
            CFPlusPopular model = new CFPlusPopular(instances);
            model.setAlpha(alpha);
            model.setBeta(beta);
            return model;
        }
    }
}

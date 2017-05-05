package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Instances;

import java.util.Map;

public class Popular extends Recommender {

    private int[] recommendation;

    public Popular(Instances instances) {
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        String someUserId = usersVectors.keySet().stream().findFirst().get();
        double[] distribution = new double[usersVectors.get(someUserId).length];
        usersVectors.values().stream().forEach(vec -> add(distribution, vec));
        recommendation = sortDistribution(distribution);
    }

    @Override
    public boolean canRecommend(String id) {
        return true;
    }

    @Override
    public int[] recommend(String id) {
        return recommendation;
    }

    public static class PopularFactory implements RecommenderFactory {

        @Override
        public Popular create(Instances instances) {
            return new Popular(instances);
        }
    }
}

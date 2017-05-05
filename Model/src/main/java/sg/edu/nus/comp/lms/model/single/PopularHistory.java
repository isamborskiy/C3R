package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Instances;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PopularHistory extends SingleSourceRecommender {

    private Map<String, int[]> idToRecommendation;

    public PopularHistory(Instances instances) {
        super(instances);
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        idToRecommendation = usersVectors.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> sortDistribution(usersVectors.get(id))
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

    public static class PopularHistoryFactory implements RecommenderFactory {

        @Override
        public PopularHistory create(Instances instances) {
            return new PopularHistory(instances);
        }
    }
}

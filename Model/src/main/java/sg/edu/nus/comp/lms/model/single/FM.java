package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.model.single.fm.FMRecommender;
import sg.edu.nus.comp.lms.model.single.fm.Transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FM extends Recommender {

    private final Map<String, int[]> recommendations = new HashMap<>();

    private int nFactors = 110;
    private int iterationNumber = 250;
    private double negative = 0;

    @Override
    public void train(Map<String, double[]> usersVectors) {
        try {
            Transformation transformation = new Transformation(usersVectors, negative);
            FMRecommender model = new FMRecommender(transformation, nFactors, iterationNumber);
            for (int userId : transformation.getUserIds()) {
                System.out.print("\rGenerating the recommendation list for user=" + userId + "...");
                String user = transformation.convertUser(userId);
                int[] recommendation = model.getRecommendationList(user);
                recommendations.put(user, recommendation);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setnFactors(int nFactors) {
        this.nFactors = nFactors;
    }

    public void setIterationNumber(int iterationNumber) {
        this.iterationNumber = iterationNumber;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }

    @Override
    public boolean canRecommend(String id) {
        return recommendations.containsKey(id);
    }

    @Override
    public int[] recommend(String id) {
        return recommendations.get(id);
    }

    @Override
    public String toString() {
        return "FM";
    }
}

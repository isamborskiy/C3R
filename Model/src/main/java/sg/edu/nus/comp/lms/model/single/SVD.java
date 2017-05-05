package sg.edu.nus.comp.lms.model.single;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import sg.edu.nus.comp.lms.model.Recommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SVD extends Recommender {

    private Map<String, Long> userToId;
    private Map<Integer, Long> itemToId;
    private Map<Long, Integer> idToItem;

    private int recommendationSize;

    private SVDRecommender recommender;

    private int featuresNumber = 40;
    private int iterationNumber = 81;
    private double learningRate = 0.11770667159580162;
    private double preventOverfitting = 0.2177380839385118;
    private double randomNoise = 0.05072227049413537;
    private double learningRateDecay = 0.1705382959554046;

    @Override
    public void train(Map<String, double[]> usersVectors) {
        mapIds(usersVectors);

        FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<>();
        for (String user : usersVectors.keySet()) {
            List<Preference> userPreferences = new ArrayList<>();
            long userId = userToId.get(user);
            double[] values = usersVectors.get(user);
            for (int item = 0; item < recommendationSize; item++) {
                if (values[item] != 0.) {
                    userPreferences.add(new GenericPreference(userId, itemToId.get(item), (float) values[item]));
                }
            }
            GenericUserPreferenceArray userArray = new GenericUserPreferenceArray(userPreferences);
            preferenceMap.put(userId, userArray);
        }
        DataModel model = new GenericDataModel(preferenceMap);

        try {
            Factorizer factorizer = new SVDPlusPlusFactorizer(model, featuresNumber, learningRate,
                    preventOverfitting, randomNoise, iterationNumber, learningRateDecay);
            this.recommender = new SVDRecommender(model, factorizer);
        } catch (TasteException e) {
            throw new RuntimeException(e);
        }
    }

    private void mapIds(Map<String, double[]> usersVectors) {
        userToId = new HashMap<>();
        itemToId = new HashMap<>();
        idToItem = new HashMap<>();

        long id = 0;
        for (String user : usersVectors.keySet()) {
            userToId.put(user, id);
            id++;
        }
        double[] values = usersVectors.values().stream().findFirst().get();
        recommendationSize = values.length;
        for (int item = 0; item < recommendationSize; item++) {
            itemToId.put(item, id);
            idToItem.put(id, item);
            id++;
        }
    }

    public void setRecommendationSize(int recommendationSize) {
        this.recommendationSize = recommendationSize;
    }

    public void setFeaturesNumber(int featuresNumber) {
        this.featuresNumber = featuresNumber;
    }

    public void setIterationNumber(int iterationNumber) {
        this.iterationNumber = iterationNumber;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setPreventOverfitting(double preventOverfitting) {
        this.preventOverfitting = preventOverfitting;
    }

    public void setRandomNoise(double randomNoise) {
        this.randomNoise = randomNoise;
    }

    public void setLearningRateDecay(double learningRateDecay) {
        this.learningRateDecay = learningRateDecay;
    }

    @Override
    public boolean canRecommend(String id) {
        return userToId.containsKey(id);
    }

    @Override
    public int[] recommend(String user) {
        long id = userToId.get(user);
        List<Float> itemEstimations = idToItem.keySet().stream()
                .sorted(Long::compare)
                .map(itemId -> {
                    try {
                        return recommender.estimatePreference(id, itemId);
                    } catch (TasteException e) {
                        return 0f;
                    }
                })
                .collect(Collectors.toList());
        return IntStream.range(0, itemEstimations.size())
                .boxed()
                .sorted((o1, o2) -> -Double.compare(itemEstimations.get(o1), itemEstimations.get(o2)))
                .mapToInt(i -> i)
                .toArray();
//        try {
//            List<Integer> recommendation = recommender.recommend(userToId.get(id), recommendationSize).stream()
//                    .map(value -> idToItem.get(value.getItemID()))
//                    .collect(Collectors.toList());
//            IntStream.range(0, recommendationSize)
//                    .filter(i -> !recommendation.contains(i))
//                    .forEach(recommendation::add);
//            return recommendation.stream().mapToInt(i -> i).toArray();
//        } catch (TasteException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public String toString() {
        return "SVD";
    }
}

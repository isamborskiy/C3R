package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.distance.CosineSimilarityDistance;
import sg.edu.nus.comp.lms.domain.weka.distance.Distance;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CFRecommender extends SingleSourceRecommender {

    protected Map<String, Instance> idToInstance;
    protected Map<String, int[]> idToRecommendation;

    protected int k = Settings.DEFAULT_K;

    public CFRecommender(Instances instances) {
        super(instances);
        this.idToInstance = InstancesUtils.extractStringAttrToInstance(instances, Settings.ID_ATTR);
    }

    public void setK(int k) {
        this.k = k;
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
                            for (String userId : nearestUsers.keySet()) {
                                double[] userVector = usersVectors.get(userId);
                                add(distribution, userVector, distance.distance(instance, nearestUsers.get(userId)));
                            }

                            return sortDistribution(distribution);
                        }
                ));
    }

    @Override
    public boolean canRecommend(String id) {
        return idToInstance.containsKey(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }
}

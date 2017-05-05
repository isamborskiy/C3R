package sg.edu.nus.comp.lms.model.multi;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.single.PopularCommunity;
import sg.edu.nus.comp.lms.model.single.SingleSourceRecommender;
import weka.core.Instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleClusteringRecommender extends SingleSourceRecommender {

    protected int k = Settings.DEFAULT_K;
    protected double beta = 1;
    protected double gamma = 0;

    protected Map<Integer, Set<String>> clusters;
    protected Map<String, Integer> idToCluster;

    protected Map<String, int[]> idToRecommendation;

    public SimpleClusteringRecommender(Instances instances, Map<String, Integer> idToCluster) {
        super(instances);
        this.idToCluster = idToCluster;
        this.clusters = new HashMap<>();
        for (String id : idToCluster.keySet()) {
            int clusterIndex = idToCluster.get(id);
            Set<String> ids = clusters.getOrDefault(clusterIndex, new HashSet<>());
            ids.add(id);
            clusters.put(clusterIndex, ids);
        }
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
        idToRecommendation = new HashMap<>();
        clusters.values().stream()
                .forEach(cluster -> {
                    Instances inst = InstancesUtils.filterInstances(instances, cluster, Settings.ID_ATTR);
                    PopularCommunity model = new PopularCommunity(inst);
                    model.setK(k);
                    model.setBeta(beta);
                    model.setGamma(gamma);
                    model.train(usersVectors);
                    cluster.stream().filter(model::canRecommend).forEach(id -> idToRecommendation.put(id, model.recommend(id)));
                });
    }

    @Override
    public boolean canRecommend(String id) {
        return idToRecommendation.containsKey(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }
}

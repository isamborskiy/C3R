package sg.edu.nus.comp.lms.model.multi.c3r;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.multi.MultiSourceRecommender;
import sg.edu.nus.comp.lms.model.single.PopularCommunity;
import weka.core.Instances;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class C3RRecommender extends MultiSourceRecommender {

    protected int k = Settings.DEFAULT_K;
    protected double alpha = .5;
    protected double beta = 1;
    protected double gamma = 0;
    protected int clusterNumber = 50;
    protected ClusteringAlgorithmFactory lastStageClusteringFactory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;

    protected Function<Integer, DoubleMatrix> laplacianFunction;
    protected BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction;

    protected Map<Integer, Set<String>> clusters;
    protected Map<String, Integer> idToCluster;
    protected Instances instances;

    protected Map<String, int[]> idToRecommendation;

    public C3RRecommender(List<Instances> sources) {
        super(sources);
    }

    public void setLaplacianFunction(Function<Integer, DoubleMatrix> laplacianFunction) {
        this.laplacianFunction = laplacianFunction;
    }

    public void setEigenvectorFunction(BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction) {
        this.eigenvectorFunction = eigenvectorFunction;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setAlpha(double alpha) {
        if (alpha != this.alpha) {
            this.alpha = alpha;
            this.clusters = null;
        }
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setClusterNumber(int clusterNumber) {
        if (clusterNumber != this.clusterNumber) {
            this.clusterNumber = clusterNumber;
            this.clusters = null;
        }
    }

    public void setLastStageClusteringFactory(ClusteringAlgorithmFactory lastStageClusteringFactory) {
        this.lastStageClusteringFactory = lastStageClusteringFactory;
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        if (clusters == null || idToCluster == null || instances == null) {
            try {
                ids.removeIf(userId -> !usersVectors.containsKey(userId));
                ClusteringAlgorithm clusteringAlgorithm = getBuiltClustering();
                convertClusters(clusteringAlgorithm::getClusterIndex);
                instances = clusteringAlgorithm.getInstances();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't train clustering", e);
            }
        }

        idToRecommendation = new HashMap<>();
        clusters.values().stream()
                .forEach(cluster -> {
                    Instances inst = InstancesUtils.filterInstances(instances, cluster, Settings.ID_ATTR);
                    PopularCommunity model = new PopularCommunity(inst);
                    model.setK(k);
                    model.setBeta(beta);
                    model.setGamma(gamma);
                    model.train(usersVectors);
                    cluster.stream().forEach(id -> idToRecommendation.put(id, model.recommend(id)));
                });
    }

    private void convertClusters(Function<String, Integer> mapping) {
        clusters = new HashMap<>();
        idToCluster = new HashMap<>();
        for (String id : ids) {
            int clusterIndex = mapping.apply(id);
            Set<String> cluster = clusters.getOrDefault(clusterIndex, new HashSet<>());
            cluster.add(id);
            clusters.put(clusterIndex, cluster);
            idToCluster.put(id, clusterIndex);
        }
    }

    protected abstract ClusteringAlgorithm getBuiltClustering() throws Exception;

    @Override
    public boolean canRecommend(String id) {
        return ids.contains(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }
}

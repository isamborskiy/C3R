package sg.edu.nus.comp.lms.model.multi;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.MultiLayerClusteringFactory;
import sg.edu.nus.comp.lms.algorithm.multi.MultiLayerClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.multi.SpectralMultiLayerClustering;
import sg.edu.nus.comp.lms.algorithm.single.AbstractClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.single.XMeans;
import sg.edu.nus.comp.lms.algorithm.util.ClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.MultiSourceRecommenderFactory;
import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.model.RecommenderFactory;
import sg.edu.nus.comp.lms.model.single.PopularHistory;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubcommunityRecommender extends MultiSourceRecommender {

    private final int sourceIndex;

    private Map<String, int[]> idToRecommendation;

    private int multiClusterNumber = 25;
    private int singleClusterNumber = 25;

    private MultiLayerClusteringFactory multiFactory =
            new SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory(0);
    private ClusteringAlgorithmFactory singleFactory = new XMeans.XMeansFactory();
    private RecommenderFactory recommenderFactory = new PopularHistory.PopularHistoryFactory();

    public SubcommunityRecommender(List<Instances> sources, int sourceIndex) {
        super(sources);
        this.sourceIndex = sourceIndex;
    }

    public void setMultiFactory(MultiLayerClusteringFactory multiFactory) {
        this.multiFactory = multiFactory;
    }

    public void setSingleFactory(ClusteringAlgorithmFactory singleFactory) {
        this.singleFactory = singleFactory;
    }

    public void setMultiClusterNumber(int multiClusterNumber) {
        this.multiClusterNumber = multiClusterNumber;
    }

    public void setSingleClusterNumber(int singleClusterNumber) {
        this.singleClusterNumber = singleClusterNumber;
    }

    public void setRecommenderFactory(RecommenderFactory recommenderFactory) {
        this.recommenderFactory = recommenderFactory;
    }

    @Override
    public void train(Map<String, double[]> usersVectors) {
        try {
            Instances source = new Instances(sources.get(sourceIndex));
            // find big communities
            MultiLayerClusteringAlgorithm multiClustering = multiFactory.create(sources, multiClusterNumber);
            multiClustering.cluster();
            Map<Integer, List<String>> communities = ClusteringUtils.extractClusters(multiClustering, ids);

            // train subcommunities for every community
            List<List<String>> subcommunities = new ArrayList<>();
            communities.values().stream()
                    .map(ids -> InstancesUtils.filterInstances(source, ids, Settings.ID_ATTR))
                    .forEach(instances -> {
                        try {
                            AbstractClusteringAlgorithm clustering = singleFactory.create(instances, singleClusterNumber);
                            clustering.cluster();
                            subcommunities.addAll(ClusteringUtils.extractClusters(clustering, instances).values());
                        } catch (Exception e) {
                            throw new RuntimeException("Couldn't train single clustering", e);
                        }
                    });

            // for every user from usersVectors.keySet() find community and then his/her subcommunity
            idToRecommendation = new HashMap<>();
            subcommunities.stream()
                    .forEach(subcommunity -> {
                                Instances instances = InstancesUtils.filterInstances(source, subcommunity, Settings.ID_ATTR);
                                Recommender model = recommenderFactory.create(instances);
                                model.train(usersVectors);

                                usersVectors.keySet().stream()
                                        .filter(model::canRecommend)
                                        .forEach(id -> idToRecommendation.put(id, model.recommend(id)));
                            }
                    );
        } catch (Exception e) {
            throw new RuntimeException("Couldn't train subcommunity model", e);
        }
    }

    @Override
    public boolean canRecommend(String id) {
        return idToRecommendation.containsKey(id);
    }

    @Override
    public int[] recommend(String id) {
        return idToRecommendation.get(id);
    }

    public static class SubcommunityRecommenderFactory implements MultiSourceRecommenderFactory {

        private final int sourceIndex;

        private int multiClusterNumber = 25;
        private int singleClusterNumber = 25;

        private MultiLayerClusteringFactory multiFactory =
                new SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory(0);
        private ClusteringAlgorithmFactory singleFactory = new XMeans.XMeansFactory();
        private RecommenderFactory recommenderFactory = new PopularHistory.PopularHistoryFactory();

        public SubcommunityRecommenderFactory(int sourceIndex) {
            this.sourceIndex = sourceIndex;
        }

        public void setMultiClusterNumber(int multiClusterNumber) {
            this.multiClusterNumber = multiClusterNumber;
        }

        public void setSingleClusterNumber(int singleClusterNumber) {
            this.singleClusterNumber = singleClusterNumber;
        }

        public void setMultiFactory(MultiLayerClusteringFactory multiFactory) {
            this.multiFactory = multiFactory;
        }

        public void setSingleFactory(ClusteringAlgorithmFactory singleFactory) {
            this.singleFactory = singleFactory;
        }

        public void setRecommenderFactory(RecommenderFactory recommenderFactory) {
            this.recommenderFactory = recommenderFactory;
        }

        @Override
        public SubcommunityRecommender create(List<Instances> sources) {
            SubcommunityRecommender recommender = new SubcommunityRecommender(sources, sourceIndex);
            recommender.setMultiFactory(multiFactory);
            recommender.setSingleFactory(singleFactory);
            recommender.setRecommenderFactory(recommenderFactory);
            recommender.setMultiClusterNumber(multiClusterNumber);
            recommender.setSingleClusterNumber(singleClusterNumber);
            return recommender;
        }
    }
}

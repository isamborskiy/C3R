package sg.edu.nus.comp.lms.algorithm.single;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.domain.Settings;
import weka.core.Attribute;
import weka.core.Instances;

public class XMeans extends AbstractClusteringAlgorithm {

    private weka.clusterers.XMeans xMeans;

    private int seed = Settings.DEFAULT_SEED;
    private int minNumClusters = 2;
    private int maxNumClusters = 100;

    public XMeans(Instances instances, int clusterNumber) {
        super(instances, clusterNumber);
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setMinNumClusters(int minNumClusters) {
        this.minNumClusters = minNumClusters;
    }

    public void setMaxNumClusters(int maxNumClusters) {
        this.maxNumClusters = maxNumClusters;
    }

    @Override
    public void cluster() throws Exception {
        xMeans = new weka.clusterers.XMeans();

        xMeans.setSeed(seed);
        xMeans.setMinNumClusters(minNumClusters);
        xMeans.setMaxNumClusters(maxNumClusters);

        Instances instances = new Instances(this.instances);
        instances.deleteStringAttributes();
        instances.deleteAttributeType(Attribute.NOMINAL);
        xMeans.buildClusterer(instances);

        Attribute idAttribute = this.instances.attribute(Settings.ID_ATTR);
        for (int i = 0; i < instances.size(); i++) {
            clusteredInstances.put(this.instances.get(i).stringValue(idAttribute),
                    xMeans.clusterInstance(instances.get(i)));
        }
    }

    @Override
    public int clusterNumber() {
        if (xMeans != null) {
            return xMeans.numberOfClusters();
        }
        return clusterNumber;
    }

    public static class XMeansFactory implements ClusteringAlgorithmFactory {

        private int seed = Settings.DEFAULT_SEED;
        private int minNumClusters = 2;
        private int maxNumClusters = 100;

        public void setSeed(int seed) {
            this.seed = seed;
        }

        public void setMinNumClusters(int minNumClusters) {
            this.minNumClusters = minNumClusters;
        }

        public void setMaxNumClusters(int maxNumClusters) {
            this.maxNumClusters = maxNumClusters;
        }

        @Override
        public XMeans create(Instances instances, int clusterNumber) {
            XMeans xMeans = new XMeans(instances, clusterNumber);
            xMeans.setSeed(seed);
            xMeans.setMinNumClusters(minNumClusters);
            xMeans.setMaxNumClusters(maxNumClusters);
            return xMeans;
        }
    }
}

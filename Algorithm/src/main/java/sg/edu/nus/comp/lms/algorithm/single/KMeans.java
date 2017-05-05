package sg.edu.nus.comp.lms.algorithm.single;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.domain.Settings;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instances;

public class KMeans extends AbstractClusteringAlgorithm {

    private int seed = Settings.DEFAULT_SEED;
    private boolean preserveInstancesOrder = true;

    public KMeans(Instances instances, int clustersNumber) {
        super(instances, clustersNumber);
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setPreserveInstancesOrder(boolean preserveInstancesOrder) {
        this.preserveInstancesOrder = preserveInstancesOrder;
    }

    @Override
    public void cluster() throws Exception {
        SimpleKMeans kMeans = new SimpleKMeans();

        kMeans.setSeed(seed);
        kMeans.setPreserveInstancesOrder(preserveInstancesOrder);
        kMeans.setNumClusters(clusterNumber);

        Instances instances = new Instances(this.instances);
        instances.deleteStringAttributes();
        kMeans.buildClusterer(instances);

        int[] assignments = kMeans.getAssignments();
        Attribute idAttribute = this.instances.attribute(Settings.ID_ATTR);
        for (int i = 0; i < assignments.length; i++) {
            clusteredInstances.put(this.instances.get(i).stringValue(idAttribute), assignments[i]);
        }
    }

    public static class KMeansFactory implements ClusteringAlgorithmFactory {

        private int seed = Settings.DEFAULT_SEED;
        private boolean preserveInstancesOrder = true;

        public void setSeed(int seed) {
            this.seed = seed;
        }

        public void setPreserveInstancesOrder(boolean preserveInstancesOrder) {
            this.preserveInstancesOrder = preserveInstancesOrder;
        }

        @Override
        public KMeans create(Instances instances, int clusterNumber) {
            KMeans kMeans = new KMeans(instances, clusterNumber);
            kMeans.setSeed(seed);
            kMeans.setPreserveInstancesOrder(preserveInstancesOrder);
            return kMeans;
        }
    }
}

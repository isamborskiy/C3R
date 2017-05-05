package sg.edu.nus.comp.lms.algorithm.single;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

public class SpectralClustering extends AbstractClusteringAlgorithm {

    private ClusteringAlgorithmFactory lastStageClusteringFactory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;

    private DoubleMatrix laplacian;
    private DoubleMatrix eigenvectors;

    private boolean isNormalize = true;

    private ClusteringAlgorithm clusteringAlgorithm;

    public SpectralClustering(Instances instances, int clusterNumber) {
        super(instances, clusterNumber);
    }

    public void setLaplacian(DoubleMatrix laplacian) {
        this.laplacian = laplacian;
    }

    public void setEigenvectors(DoubleMatrix eigenvectors) {
        this.eigenvectors = eigenvectors;
    }

    public void setLastStageClusteringFactory(ClusteringAlgorithmFactory lastStageClusteringFactory) {
        this.lastStageClusteringFactory = lastStageClusteringFactory;
    }

    public void setNormalize(boolean normalize) {
        isNormalize = normalize;
    }

    @Override
    public int clusterNumber() {
        return clusteringAlgorithm != null ? clusteringAlgorithm.clusterNumber() : clusterNumber;
    }

    @Override
    public void cluster() throws Exception {
        if (eigenvectors == null) {
            if (laplacian == null) {
                List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
                laplacian = isNormalize ? SpectralClusteringUtils.buildLaplacianNormalize(instances, ids)
                        : SpectralClusteringUtils.buildLaplacian(instances, ids);
            }
            eigenvectors = SpectralClusteringUtils.buildEigenvectors(laplacian, clusterNumber);
        }
        clusterEigenvectors(eigenvectors);
    }

    private void clusterEigenvectors(DoubleMatrix eigenvectors) throws Exception {
        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
        Instances instances = InstancesUtils.generateInstances(eigenvectors, ids);

        clusteringAlgorithm = lastStageClusteringFactory.create(instances, clusterNumber());
        clusteringAlgorithm.cluster();
        Attribute attr = this.instances.attribute(Settings.ID_ATTR);
        for (Instance instance : this.instances) {
            String key = instance.stringValue(attr);
            int index = clusteringAlgorithm.getClusterIndex(key);
            clusteredInstances.put(key, index);
        }
    }

    public static class SpectralClusteringFactory implements ClusteringAlgorithmFactory {

        private ClusteringAlgorithmFactory lastStageClusteringFactory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;

        private DoubleMatrix laplacian;
        private DoubleMatrix eigenvectors;

        private boolean isNormalize = true;

        public void setLastStageClusteringFactory(ClusteringAlgorithmFactory lastStageClusteringFactory) {
            this.lastStageClusteringFactory = lastStageClusteringFactory;
        }

        public void setLaplacian(DoubleMatrix laplacian) {
            this.laplacian = laplacian;
        }

        public void setEigenvectors(DoubleMatrix eigenvectors) {
            this.eigenvectors = eigenvectors;
        }

        public void setNormalize(boolean normalize) {
            isNormalize = normalize;
        }

        @Override
        public SpectralClustering create(Instances instances, int clusterNumber) {
            SpectralClustering spectralClustering = new SpectralClustering(instances, clusterNumber);
            spectralClustering.setEigenvectors(eigenvectors);
            spectralClustering.setLaplacian(laplacian);
            spectralClustering.setNormalize(isNormalize);
            spectralClustering.setLastStageClusteringFactory(lastStageClusteringFactory);
            return spectralClustering;
        }
    }
}

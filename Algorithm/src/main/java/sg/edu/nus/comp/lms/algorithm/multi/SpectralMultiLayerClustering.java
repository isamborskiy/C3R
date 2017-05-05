package sg.edu.nus.comp.lms.algorithm.multi;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.MultiLayerClusteringFactory;
import sg.edu.nus.comp.lms.algorithm.single.AbstractClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instances;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SpectralMultiLayerClustering extends MultiLayerClusteringAlgorithm {

    protected final double alpha;

    protected Function<Integer, DoubleMatrix> laplacianFunction;
    protected BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction;

    private ClusteringAlgorithmFactory lastStageClusteringFactory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;
    private AbstractClusteringAlgorithm clustering;

    public SpectralMultiLayerClustering(List<Instances> layers, int clustersNumber, double alpha) {
        super(layers, clustersNumber);
        this.alpha = alpha;

        List<String> ids = InstancesUtils.extractStringAttr(this.layers.get(0), Settings.ID_ATTR);
        this.laplacianFunction = i -> SpectralClusteringUtils.buildLaplacian(this.layers.get(i), ids);
        this.eigenvectorFunction = (laplacian, i) -> SpectralClusteringUtils.buildEigenvectors(laplacian, clustersNumber);

    }

    public void setLastStageClusteringFactory(ClusteringAlgorithmFactory lastStageClusteringFactory) {
        this.lastStageClusteringFactory = lastStageClusteringFactory;
    }

    public void setLaplacianFunction(Function<Integer, DoubleMatrix> laplacianFunction) {
        this.laplacianFunction = laplacianFunction;
    }

    public void setEigenvectorFunction(BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction) {
        this.eigenvectorFunction = eigenvectorFunction;
    }

    @Override
    public void cluster() throws Exception {
        List<String> ids = InstancesUtils.extractStringAttr(layers.get(0), Settings.ID_ATTR);

        DoubleMatrix laplacianMod = getLaplacianMod(ids.size());
        DoubleMatrix eigenvectors = SpectralClusteringUtils.buildEigenvectors(laplacianMod, clusterNumber());

        Instances instances = InstancesUtils.generateInstances(eigenvectors, ids);
        clustering = lastStageClusteringFactory.create(instances, clusterNumber());
        clustering.cluster();
    }

    @Override
    public int getClusterIndex(String key) {
        return clustering.getClusterIndex(key);
    }

    @Override
    public Instances getInstances() {
        return clustering.getInstances();
    }

    private DoubleMatrix getLaplacianMod(int n) {
        DoubleMatrix laplacianModLeft = new DoubleMatrix(n, n);
        DoubleMatrix laplacianModRight = new DoubleMatrix(n, n);
        for (int i = 0; i < layers.size(); i++) {
            DoubleMatrix laplacian = getLaplacian(i);
            laplacianModLeft.addi(laplacian);
            DoubleMatrix eigenvalues = getEigenvectors(laplacian, i);
            laplacianModRight.addi(eigenvalues.mmul(eigenvalues.transpose()));
        }
        return laplacianModLeft.sub(laplacianModRight.mul(alpha));
    }

    protected DoubleMatrix getLaplacian(int i) {
        return laplacianFunction.apply(i);
    }

    protected DoubleMatrix getEigenvectors(DoubleMatrix laplacian, int i) {
        return eigenvectorFunction.apply(laplacian, i);
    }

    public static class SpectralMultiLayerClusteringFactory implements MultiLayerClusteringFactory {

        protected final double alpha;

        protected Function<Integer, DoubleMatrix> laplacianFunction;
        protected BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction;

        protected ClusteringAlgorithmFactory lastStageClusteringFactory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;

        public SpectralMultiLayerClusteringFactory(double alpha) {
            this.alpha = alpha;
        }

        public void setLaplacianFunction(Function<Integer, DoubleMatrix> laplacianFunction) {
            this.laplacianFunction = laplacianFunction;
        }

        public void setEigenvectorFunction(BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction) {
            this.eigenvectorFunction = eigenvectorFunction;
        }

        public void setLastStageClusteringFactory(ClusteringAlgorithmFactory lastStageClusteringFactory) {
            this.lastStageClusteringFactory = lastStageClusteringFactory;
        }

        @Override
        public SpectralMultiLayerClustering create(List<Instances> layers, int clusterNumber) {
            SpectralMultiLayerClustering clustering = new SpectralMultiLayerClustering(layers, clusterNumber, alpha);
            if (laplacianFunction != null) {
                clustering.setLaplacianFunction(laplacianFunction);
            }
            if (eigenvectorFunction != null) {
                clustering.setEigenvectorFunction(eigenvectorFunction);
            }
            clustering.setLastStageClusteringFactory(lastStageClusteringFactory);
            return clustering;
        }
    }
}

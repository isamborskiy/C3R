package sg.edu.nus.comp.lms.model.multi.c3r;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.multi.SpectralMultiLayerClustering;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.model.MultiSourceRecommenderFactory;
import weka.core.Instances;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class C3R extends C3RRecommender {

    public C3R(List<Instances> sources) {
        super(sources);
    }

    @Override
    protected ClusteringAlgorithm getBuiltClustering() throws Exception {
        SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory multiFactory
                = new SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory(alpha);
        multiFactory.setLastStageClusteringFactory(lastStageClusteringFactory);
        multiFactory.setEigenvectorFunction(eigenvectorFunction);
        multiFactory.setLaplacianFunction(laplacianFunction);

        SpectralMultiLayerClustering clustering = multiFactory.create(sources, clusterNumber);
        clustering.cluster();
        return clustering;
    }

    public static class C3RFactory implements MultiSourceRecommenderFactory {

        protected int k = Settings.DEFAULT_K;
        protected double alpha = .5;
        protected double beta = 1;
        protected double gamma = 0;
        protected int clusterNumber = 50;
        protected ClusteringAlgorithmFactory factory = SpectralClusteringUtils.DEFAULT_CLUSTERING_FACTORY;

        protected Function<Integer, DoubleMatrix> laplacianFunction;
        protected BiFunction<DoubleMatrix, Integer, DoubleMatrix> eigenvectorFunction;

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
            this.alpha = alpha;
        }

        public void setBeta(double beta) {
            this.beta = beta;
        }

        public void setGamma(double gamma) {
            this.gamma = gamma;
        }

        public void setClusterNumber(int clusterNumber) {
            this.clusterNumber = clusterNumber;
        }

        public void setFactory(ClusteringAlgorithmFactory factory) {
            this.factory = factory;
        }

        @Override
        public C3R create(List<Instances> sources) {
            C3R c3r = new C3R(sources);
            c3r.setK(k);
            c3r.setAlpha(alpha);
            c3r.setBeta(beta);
            c3r.setGamma(gamma);
            c3r.setClusterNumber(clusterNumber);
            c3r.setLaplacianFunction(laplacianFunction);
            c3r.setEigenvectorFunction(eigenvectorFunction);
            return c3r;
        }
    }
}

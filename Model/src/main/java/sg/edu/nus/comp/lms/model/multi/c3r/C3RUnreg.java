package sg.edu.nus.comp.lms.model.multi.c3r;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.model.MultiSourceRecommenderFactory;
import weka.core.Instances;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class C3RUnreg extends C3R {

    protected double alpha = 0.;

    public C3RUnreg(List<Instances> sources) {
        super(sources);
    }

    @Override
    public String toString() {
        return "C3RUnreg";
    }

    public static class C3RUnregFactory implements MultiSourceRecommenderFactory {

        protected int k = Settings.DEFAULT_K;
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
        public C3RUnreg create(List<Instances> sources) {
            C3RUnreg c3r = new C3RUnreg(sources);
            c3r.setK(k);
            c3r.setBeta(beta);
            c3r.setGamma(gamma);
            c3r.setClusterNumber(clusterNumber);
            c3r.setLaplacianFunction(laplacianFunction);
            c3r.setEigenvectorFunction(eigenvectorFunction);
            return c3r;
        }
    }
}

package sg.edu.nus.comp.lms.model.multi.c3r;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.multi.GraphRegularizedSpectralMultiLayerClustering;
import weka.core.Instances;

import java.util.List;

public class LCapC3R extends C3R {

    private DoubleMatrix weights;

    public LCapC3R(List<Instances> sources) {
        super(sources);
    }

    public void setWeights(DoubleMatrix weights) {
        if (weights != this.weights) {
            this.weights = weights;
            this.clusters = null;
        }
    }

    @Override
    protected ClusteringAlgorithm getBuiltClustering() throws Exception {
        GraphRegularizedSpectralMultiLayerClustering.GraphRegularizedSpectralMultiLayerClusteringFactory multiFactory
                = new GraphRegularizedSpectralMultiLayerClustering.GraphRegularizedSpectralMultiLayerClusteringFactory(alpha);
        multiFactory.setLastStageClusteringFactory(lastStageClusteringFactory);
        multiFactory.setEigenvectorFunction(eigenvectorFunction);
        multiFactory.setLaplacianFunction(laplacianFunction);
        multiFactory.setWeight(weights);

        GraphRegularizedSpectralMultiLayerClustering clustering = multiFactory.create(sources, clusterNumber);
        clustering.cluster();
        return clustering;
    }

    public static class LCapC3RFactory extends C3R.C3RFactory {

        private DoubleMatrix weights;

        public void setWeights(DoubleMatrix weights) {
            this.weights = weights;
        }

        @Override
        public LCapC3R create(List<Instances> sources) {
            LCapC3R lCapC3R = new LCapC3R(sources);
            lCapC3R.setK(k);
            lCapC3R.setAlpha(alpha);
            lCapC3R.setBeta(beta);
            lCapC3R.setGamma(gamma);
            lCapC3R.setClusterNumber(clusterNumber);
            lCapC3R.setLaplacianFunction(laplacianFunction);
            lCapC3R.setEigenvectorFunction(eigenvectorFunction);
            lCapC3R.setWeights(weights);
            return lCapC3R;
        }
    }
}

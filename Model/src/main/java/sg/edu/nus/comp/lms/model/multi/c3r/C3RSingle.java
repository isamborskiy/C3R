package sg.edu.nus.comp.lms.model.multi.c3r;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.single.SpectralClustering;
import weka.core.Instances;

import java.util.List;

public class C3RSingle extends C3R {

    private final int layerIndex;

    public C3RSingle(List<Instances> sources, int layerIndex) {
        super(sources);
        this.layerIndex = layerIndex;
    }

    @Override
    protected ClusteringAlgorithm getBuiltClustering() throws Exception {
        SpectralClustering.SpectralClusteringFactory singleFactory = new SpectralClustering.SpectralClusteringFactory();
        singleFactory.setLastStageClusteringFactory(lastStageClusteringFactory);
        if (laplacianFunction != null) {
            singleFactory.setLaplacian(laplacianFunction.apply(layerIndex));
        }
        if (eigenvectorFunction != null) {
            singleFactory.setEigenvectors(eigenvectorFunction.apply(null, layerIndex));
        }

        SpectralClustering clustering = singleFactory.create(sources.get(layerIndex), clusterNumber);
        clustering.cluster();
        return clustering;
    }

    public static class C3RSingleFactory extends C3RFactory {

        protected final int layerIndex;

        public C3RSingleFactory(int layerIndex) {
            this.layerIndex = layerIndex;
        }

        @Override
        public C3RSingle create(List<Instances> sources) {
            C3RSingle c3RSingle = new C3RSingle(sources, layerIndex);
            c3RSingle.setK(k);
            c3RSingle.setAlpha(alpha);
            c3RSingle.setBeta(beta);
            c3RSingle.setGamma(gamma);
            c3RSingle.setClusterNumber(clusterNumber);
            c3RSingle.setLaplacianFunction(laplacianFunction);
            c3RSingle.setEigenvectorFunction(eigenvectorFunction);
            return c3RSingle;
        }
    }
}

package sg.edu.nus.comp.lms.algorithm.multi;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import weka.core.Instances;

import java.util.List;

public class GraphRegularizedSpectralMultiLayerClustering extends SpectralMultiLayerClustering {

    private final DoubleMatrix[] us;

    private DoubleMatrix weight;

    public GraphRegularizedSpectralMultiLayerClustering(List<Instances> layers, int clustersNumber, double alpha) {
        super(layers, clustersNumber, alpha);
        this.us = new DoubleMatrix[layers.size()];
    }

    public void setWeight(DoubleMatrix weight) {
        this.weight = weight;
    }

    @Override
    protected DoubleMatrix getLaplacian(int i) {
        DoubleMatrix laplacian = laplacianFunction.apply(i);
        DoubleMatrix rightPart = new DoubleMatrix(laplacian.getRows(), laplacian.getColumns());
        for (int j = 0; j < layers.size(); j++) {
            if (j != i) {
                if (us[j] == null) {
                    us[j] = eigenvectorFunction.apply(laplacian, j);
                }
                DoubleMatrix u = us[j];
                rightPart.add(u.mmul(u.transpose()).mul(getWeight(i, j)));
            }
        }
        return super.getLaplacian(i);
    }

    private double getWeight(int i, int j) {
        if (weight != null) {
            return weight.get(i, j);
        } else {
            return 0;
        }
    }

    @Override
    protected DoubleMatrix getEigenvectors(DoubleMatrix laplacian, int i) {
        return SpectralClusteringUtils.buildEigenvectors(laplacian, clusterNumber);
    }

    public static class GraphRegularizedSpectralMultiLayerClusteringFactory extends SpectralMultiLayerClusteringFactory {

        private DoubleMatrix weight;

        public GraphRegularizedSpectralMultiLayerClusteringFactory(double alpha) {
            super(alpha);
        }

        public void setWeight(DoubleMatrix weight) {
            this.weight = weight;
        }

        @Override
        public GraphRegularizedSpectralMultiLayerClustering create(List<Instances> layers, int clusterNumber) {
            GraphRegularizedSpectralMultiLayerClustering clustering = new GraphRegularizedSpectralMultiLayerClustering(layers, clusterNumber, alpha);
            if (laplacianFunction != null) {
                clustering.setLaplacianFunction(laplacianFunction);
            }
            if (eigenvectorFunction != null) {
                clustering.setEigenvectorFunction(eigenvectorFunction);
            }
            clustering.setLastStageClusteringFactory(lastStageClusteringFactory);
            clustering.setWeight(weight);
            return clustering;
        }
    }
}

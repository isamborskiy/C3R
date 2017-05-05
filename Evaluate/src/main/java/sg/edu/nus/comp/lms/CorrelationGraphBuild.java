package sg.edu.nus.comp.lms;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.algorithm.single.SpectralClustering;
import sg.edu.nus.comp.lms.algorithm.util.LayersRelationGraph;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.util.DoubleMatrixUtils;
import weka.core.Instances;

import java.util.List;
import java.util.function.BiFunction;

public class CorrelationGraphBuild {

    public static void main(String[] args) throws Exception {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

//        DoubleMatrix[] laplacians = new DoubleMatrix[sources.size()];
//        DoubleMatrix[][] eigenmatrix = new DoubleMatrix[sources.size()][2];
//        for (int i = 0; i < sources.size(); i++) {
//            laplacians[i] = SpectralClusteringUtils.getLaplacian(i);
//            eigenmatrix[i] = SpectralClusteringUtils.getEigenmatrix(i);
//        }

        LayersRelationGraph layersRelationGraph = new LayersRelationGraph(getClusteringFunction(sources/*, laplacians, eigenmatrix*/));
        int upperBound = (int) Math.sqrt(sources.get(0).size());
        DoubleMatrix correlationMatrix = layersRelationGraph.calculateCorrelation(sources, 2, upperBound, 1);
        DoubleMatrixUtils.write("correlation_matrix", correlationMatrix);
    }

    private static BiFunction<Instances, Double, ClusteringAlgorithm> getClusteringFunction(List<Instances> instancesList/*,
                                                                                            DoubleMatrix[] laplacians,
                                                                                            DoubleMatrix[][] eigenmatrix*/) {
        return (instances, clusterNumber) -> {
//            int layerIndex = instancesList.indexOf(instances);
            SpectralClustering spectralClustering = new SpectralClustering(instances, clusterNumber.intValue());
//            spectralClustering.setLaplacian(laplacians[layerIndex]);
//            spectralClustering.setEigenvectors(SpectralClusteringUtils.buildEigenvectors(eigenmatrix[layerIndex],
//                    clusterNumber.intValue()));
            return spectralClustering;
        };
    }
}

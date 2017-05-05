package sg.edu.nus.comp.lms.algorithm.util;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LayersRelationGraph {

    private final BiFunction<Instances, Double, ClusteringAlgorithm> clusteringAlgorithmGenerator;

    public LayersRelationGraph(BiFunction<Instances, Double, ClusteringAlgorithm> clusteringAlgorithmGenerator) {
        this.clusteringAlgorithmGenerator = clusteringAlgorithmGenerator;
    }

    public DoubleMatrix calculateCorrelation(List<Instances> sources, double lower, double higher, double step)
            throws Exception {
        DoubleMatrix correlationMatrix = new DoubleMatrix(sources.size(), sources.size());

        List<String> ids = InstancesUtils.extractIntersectionStringAttr(sources, Settings.ID_ATTR);
        List<Map<String, Instance>> idToInstance = sources.stream()
                .map(instances -> InstancesUtils.extractStringAttrToInstance(instances, Settings.ID_ATTR))
                .collect(Collectors.toList());

        for (double param = lower; param <= higher; param += step) {
            DoubleMatrix[] matrices = new DoubleMatrix[sources.size()];
            for (int i = 0; i < matrices.length; i++) {
                ClusteringAlgorithm clusteringAlgorithm = clusteringAlgorithmGenerator.apply(sources.get(i), param);
                clusteringAlgorithm.cluster();
                matrices[i] = getClusteringMatrix(ids, clusteringAlgorithm, sources.get(i), idToInstance.get(i));
            }

            System.out.print((int) param);
            for (int i = 0; i < sources.size(); i++) {
                for (int j = i + 1; j < sources.size(); j++) {
                    double value = matrices[i].sub(matrices[j]).norm2() /
                            Math.sqrt(matrices[i].getRows() * (matrices[i].getRows() - 1));
                    double newValue = correlationMatrix.get(i, j) + value;
                    correlationMatrix.put(i, j, newValue);
                    correlationMatrix.put(j, i, newValue);
                    System.out.print(" " + value);
                }
            }
            System.out.println();
        }

        double k = (higher - lower) / step;
        for (int i = 0; i < sources.size(); i++) {
            for (int j = i + 1; j < sources.size(); j++) {
                double value = correlationMatrix.get(i, j) / k;
                correlationMatrix.put(i, j, value);
                correlationMatrix.put(j, i, value);
            }
        }

        return correlationMatrix;
    }

    private DoubleMatrix getClusteringMatrix(List<String> ids, ClusteringAlgorithm clusteringAlgorithm,
                                             Instances instances, Map<String, Instance> idToInstance) {
        DoubleMatrix matrix = new DoubleMatrix(ids.size(), ids.size());

        Map<Integer, Set<String>> clusters = new HashMap<>();
        Map<String, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            idToIndex.put(id, i);
            int clusterId = clusteringAlgorithm.getClusterIndex(id);
            Set<String> cluster = clusters.getOrDefault(clusterId, new HashSet<>());
            cluster.add(id);
            clusters.put(clusterId, cluster);
        }

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            int clusterId = clusteringAlgorithm.getClusterIndex(id);
            for (String userId : clusters.get(clusterId)) {
                int userIndex = idToIndex.get(userId);
                double value = 0;
                if (clusterId != -1) {
                    value = 1;
                }
                matrix.put(i, userIndex, value);
                matrix.put(userIndex, i, value);
            }
        }

        return matrix;
    }
}

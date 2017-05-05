package sg.edu.nus.comp.lms.algorithm.util;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClusteringUtils {

    private ClusteringUtils() {
    }

    public static Map<Integer, List<String>> extractClusters(ClusteringAlgorithm clusteringAlgorithm, Instances instances) {
        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
        return extractClusters(clusteringAlgorithm, ids);
    }

    public static Map<Integer, List<String>> extractClusters(ClusteringAlgorithm clusteringAlgorithm, List<String> ids) {
        Map<Integer, List<String>> idToCluster = new HashMap<>();
        for (String id : ids) {
            int index = clusteringAlgorithm.getClusterIndex(id);
            List<String> cluster = idToCluster.getOrDefault(index, new ArrayList<>());
            cluster.add(id);
            idToCluster.put(index, cluster);
        }
        return idToCluster;
    }

    public static int[] clusterDistribution(ClusteringAlgorithm clusteringAlgorithm, Instances instances) {
        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
        int[] distribution = new int[clusteringAlgorithm.clusterNumber()];
        for (String id : ids) {
            distribution[clusteringAlgorithm.getClusterIndex(id)]++;
        }
        return distribution;
    }

    public static Instance[] findCentroids(ClusteringAlgorithm clusteringAlgorithm, Instances instances) {
        List<Attribute> attributes = InstancesUtils.extractAttributes(instances);
        return IntStream.range(0, clusteringAlgorithm.clusterNumber())
                .mapToObj(index -> {
                    List<Instance> cluster = instances.stream()
                            .filter(instance -> clusteringAlgorithm.getClusterIndex(instance) == index)
                            .collect(Collectors.toList());
                    return findCentroid(cluster, attributes);
                })
                .toArray(Instance[]::new);
    }

    public static Instance findCentroid(List<Instance> cluster, List<Attribute> attributes) {
        Instance resultedInstance = new DenseInstance(attributes.size());
        for (Attribute attribute : attributes) {
            if (attribute.isNumeric()) {
                resultedInstance.setValue(attribute, cluster.stream()
                        .mapToDouble(instance -> instance.value(attribute))
                        .average().getAsDouble());
            } else {
                resultedInstance.setValue(attribute, cluster.get(0).stringValue(attribute));
            }
        }
        return resultedInstance;
    }
}

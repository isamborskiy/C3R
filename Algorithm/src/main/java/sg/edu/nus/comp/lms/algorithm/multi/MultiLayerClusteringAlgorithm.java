package sg.edu.nus.comp.lms.algorithm.multi;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.operation.NormalizeInstances;
import sg.edu.nus.comp.lms.domain.weka.util.InstanceUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MultiLayerClusteringAlgorithm implements ClusteringAlgorithm {

    protected final int clusterNumber;
    protected final int n;

    protected final List<Instances> layers;

    protected final Map<String, Integer> clusteredInstances;

    public MultiLayerClusteringAlgorithm(List<Instances> layers, int clusterNumber) {
        this.layers = layers.stream().map(modal -> new NormalizeInstances(modal).eval()).collect(Collectors.toList());
        if (!layers.stream().filter(instances -> instances.size() > clusterNumber).findFirst().isPresent()) {
            throw new IllegalArgumentException("clusterNumber more then dataset dimension");
        }

        this.clusteredInstances = new HashMap<>();

        this.clusterNumber = clusterNumber;
        this.n = layers.get(0).size();
    }

    @Override
    public int clusterNumber() {
        return clusterNumber;
    }

    @Override
    public int getClusterIndex(String key) {
        return clusteredInstances.get(key);
    }

    @Override
    public int getClusterIndex(Instance instance) {
        return getClusterIndex(instance.stringValue(InstanceUtils.attrIndexOf(instance, Settings.ID_ATTR)));
    }
}

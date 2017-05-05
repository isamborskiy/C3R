package sg.edu.nus.comp.lms.algorithm.single;

import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithm;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.operation.NormalizeInstances;
import sg.edu.nus.comp.lms.domain.weka.util.InstanceUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClusteringAlgorithm implements ClusteringAlgorithm {

    protected final int clusterNumber;
    protected final int n;

    protected final Instances instances;

    protected final Map<String, Integer> clusteredInstances;

    public AbstractClusteringAlgorithm(Instances instances, int clusterNumber) {
        this.instances = new NormalizeInstances(instances).eval();

        this.clusteredInstances = new HashMap<>();

        this.clusterNumber = clusterNumber;
        this.n = instances.size();
    }

    public Instances getInstances() {
        return instances;
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

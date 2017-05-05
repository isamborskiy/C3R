package sg.edu.nus.comp.lms.algorithm;

import weka.core.Instance;
import weka.core.Instances;

public interface ClusteringAlgorithm {

    Instances getInstances();

    void cluster() throws Exception;

    int clusterNumber();

    int getClusterIndex(String key);

    int getClusterIndex(Instance instance);
}

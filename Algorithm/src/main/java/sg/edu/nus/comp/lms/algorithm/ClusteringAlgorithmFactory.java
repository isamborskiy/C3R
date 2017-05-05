package sg.edu.nus.comp.lms.algorithm;

import sg.edu.nus.comp.lms.algorithm.single.AbstractClusteringAlgorithm;
import weka.core.Instances;

public interface ClusteringAlgorithmFactory {

    AbstractClusteringAlgorithm create(Instances instances, int clusterNumber);
}

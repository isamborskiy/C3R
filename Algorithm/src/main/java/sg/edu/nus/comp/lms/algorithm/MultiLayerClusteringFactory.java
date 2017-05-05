package sg.edu.nus.comp.lms.algorithm;

import sg.edu.nus.comp.lms.algorithm.multi.MultiLayerClusteringAlgorithm;
import weka.core.Instances;

import java.util.List;

public interface MultiLayerClusteringFactory {

    MultiLayerClusteringAlgorithm create(List<Instances> layers, int clusterNumber);
}

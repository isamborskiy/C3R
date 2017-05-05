package com.samborskiy.visual.clustering;

import com.samborskiy.algorithm.Clustering;
import com.samborskiy.algorithm.single.KMeans;
import com.samborskiy.algorithm.single.SpectralClustering;
import weka.core.Instances;

import java.lang.reflect.Constructor;

public enum ClusteringAlgorithm {

    K_MEANS_PLUS_PLUS("k-means++", KMeans.class),
    SPECTRAL("spectral", SpectralClustering.class);

    private final String name;
    private final Class<? extends Clustering> clazz;

    ClusteringAlgorithm(String name, Class<? extends Clustering> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static ClusteringAlgorithm fromString(String name) {
        switch (name) {
            case "k-means++":
                return K_MEANS_PLUS_PLUS;
            case "spectral":
                return SPECTRAL;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public Clustering getInstance(Instances instances, int k) throws ReflectiveOperationException {
        Constructor<? extends Clustering> constructor = clazz.getConstructor(Instances.class, int.class);
        return constructor.newInstance(instances, k);
    }
}

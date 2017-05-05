package sg.edu.nus.comp.lms.algorithm;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import sg.edu.nus.comp.lms.algorithm.single.SpectralClustering;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.reader.CSVReader;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instances;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SpectralClusteringAlgorithmTest {

    private static File getFileFromResources(String filename) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
    }

    @Test
    public void simpleTest() throws Exception {
        Instances instances = new CSVReader(getFileFromResources("simpleTest.csv")).readAll();

        ClusteringAlgorithm clusteringAlgorithm = new SpectralClustering(instances, 2);
        clusteringAlgorithm.cluster();

        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
        for (int clusterSize : buildDistribution(clusteringAlgorithm, ids)) {
            MatcherAssert.assertThat(clusterSize, Matchers.equalTo(2));
        }
    }

    @Test
    public void simpleTest2() throws Exception {
        Instances instances = new CSVReader(getFileFromResources("simpleTest2.csv")).readAll();

        ClusteringAlgorithm clusteringAlgorithm = new SpectralClustering(instances, 3);
        clusteringAlgorithm.cluster();

        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
        int[] distr = buildDistribution(clusteringAlgorithm, ids);
        int max = Arrays.stream(distr).max().getAsInt();
        int min = Arrays.stream(distr).min().getAsInt();

        MatcherAssert.assertThat(max, Matchers.equalTo(2));
        MatcherAssert.assertThat(min, Matchers.equalTo(1));
    }

//    @Test
//    public void simpleTest3() throws Exception {
//        try (BufferedReader reader = new BufferedReader(new FileReader(getFileFromResources("simpleTest3.arff")))) {
//            Instances instances = new ArffLoader.ArffReader(reader).getData();
//
//            ClusteringAlgorithm clustering = new SpectralClustering(instances, 3);
//            clustering.cluster();
//
//            List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
//            for (int clusterSize : buildDistribution(clustering, ids)) {
//                MatcherAssert.assertThat(clusterSize, Matchers.equalTo(2));
//            }
//        }
//    }

    private int[] buildDistribution(ClusteringAlgorithm clusteringAlgorithm, List<String> ids) {
        int[] clusterDistribution = new int[clusteringAlgorithm.clusterNumber()];
        for (String key : ids) {
            int clusterId = clusteringAlgorithm.getClusterIndex(key);
            System.out.println(key + " " + clusterId);
            clusterDistribution[clusterId]++;
        }
        return clusterDistribution;
    }
}

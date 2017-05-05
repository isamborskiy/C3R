package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.algorithm.single.SpectralClustering;
import sg.edu.nus.comp.lms.algorithm.single.XMeans;
import sg.edu.nus.comp.lms.algorithm.util.ClusteringUtils;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import weka.core.Instances;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class CommunityProfiling {

    private static final int[] CLUSTERS = {3, 8, 5, 11, 9};

    public static void main(String[] args) throws Exception {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        for (int modalIndex = 0; modalIndex < sources.size(); modalIndex++) {
            Instances instances = sources.get(modalIndex);

            SpectralClustering clustering = new SpectralClustering(instances, CLUSTERS[modalIndex]);
            clustering.setLastStageClusteringFactory(new XMeans.XMeansFactory());
            clustering.cluster();

            Map<Integer, List<String>> idToCluster = ClusteringUtils.extractClusters(clustering, instances);
            for (int i = 0; i < clustering.clusterNumber(); i++) {
                try (PrintWriter writer = new PrintWriter(Settings.MODAL_NAME[modalIndex] + "_community" + i)) {
                    List<String> ids = idToCluster.get(i);
                    writer.println(ids.size());
                    ids.forEach(writer::println);
                }
            }
        }
    }
}

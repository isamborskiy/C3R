package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.algorithm.single.SpectralClustering;
import sg.edu.nus.comp.lms.algorithm.single.XMeans;
import sg.edu.nus.comp.lms.algorithm.util.ClusteringUtils;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.distance.CosineSimilarityDistance;
import sg.edu.nus.comp.lms.domain.weka.distance.Distance;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommunityCorrelationTable {

    private static final int[] CLUSTERS = {4, 4, 4, 4, 4};
    private static final String[] CLUSTERS_NAME = {"tw", "fsq", "inst", "temp", "mob"};

    public static void main(String[] args) throws Exception {
        List<Instances> instancesList = CommonMethods.readAllIntersectedSources();

        Map<Integer, Map<String, double[]>> modalToIdToValue = new HashMap<>();
        for (int modalIndex = 0; modalIndex < instancesList.size(); modalIndex++) {
            Instances instances = instancesList.get(modalIndex);

            SpectralClustering clustering = new SpectralClustering(instances, CLUSTERS[modalIndex]);
            clustering.setLastStageClusteringFactory(new XMeans.XMeansFactory());
            clustering.cluster();

            Instance[] centroids = ClusteringUtils.findCentroids(clustering, instances);

            ArrayList<Attribute> attributes = InstancesUtils.extractAttributes(instances);

            Attribute idAttr = instances.attribute(Settings.ID_ATTR);
            Map<String, double[]> idToValue = instances.stream()
                    .collect(Collectors.toMap(
                            instance -> instance.stringValue(idAttr),
                            instance -> {
                                String id = instance.stringValue(idAttr);
                                double[] vec = new double[clustering.clusterNumber()];
                                int index = clustering.getClusterIndex(id);
                                Instance centroid = centroids[index];
                                Instances inst = InstancesUtils.toInstances(Arrays.asList(instance, centroid), attributes);
                                Distance distance = new CosineSimilarityDistance();
                                distance.initialize(inst);
                                vec[index] = distance.distance(inst.get(0), inst.get(1));
                                return vec;
                            }
                    ));
            modalToIdToValue.put(modalIndex, idToValue);
        }

        List<String> ids = InstancesUtils.extractStringAttr(instancesList.get(0), Settings.ID_ATTR);
        try (PrintWriter writer = new PrintWriter("matrix")) {
            writer.print("_id");
            for (int modal = 0; modal < instancesList.size(); modal++) {
                String prefix = CLUSTERS_NAME[modal];
                writer.print(IntStream.range(0, CLUSTERS[modal])
                        .mapToObj(i -> prefix + i)
                        .collect(Collectors.joining(",", ",", "")));
            }
            writer.println();

            for (String id : ids) {
                writer.print(id);
                for (int modal = 0; modal < instancesList.size(); modal++) {
                    writer.print(Arrays.stream(modalToIdToValue.get(modal).get(id))
                            .mapToObj(val -> String.format("%.5f", val))
                            .collect(Collectors.joining(",", ",", "")));
                }
                writer.println();
            }
        }
    }
}

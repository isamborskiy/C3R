package sg.edu.nus.comp.lms;

import com.opencsv.CSVReader;
import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.distance.CosineSimilarityDistance;
import sg.edu.nus.comp.lms.domain.weka.distance.Distance;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModularityMaximization {

    private static final String METHOD_NAME = "nonGraph";

    public static void main(String[] args) throws IOException {
        Instances source = CommonMethods.readAllIntersectedSources().get(1);
        Attribute idAttr = source.attribute(Settings.ID_ATTR);
        Map<String, Instance> idToInstance = source.stream()
                .collect(Collectors.toMap(
                        instance -> instance.stringValue(idAttr),
                        Function.identity()
                ));

        Distance distance = new CosineSimilarityDistance();
        distance.initialize(source);

        Map<String, Integer> idToCluster = idToCluster();
        Map<Integer, List<String>> clusters = getClusters();

        List<String> vertexes = new ArrayList<>(idToCluster.keySet());
        // number of edges
        int m = clusters.values().stream()
                .mapToInt(List::size)
                .map(n -> n * (n - 1) / 2)
                .sum();

        DoubleMatrix s = new DoubleMatrix(vertexes.size(), clusters.size());
        for (int userIndex = 0; userIndex < vertexes.size(); userIndex++) {
            s.put(userIndex, idToCluster.get(vertexes.get(userIndex)), 1);
        }

        DoubleMatrix a = new DoubleMatrix(vertexes.size(), vertexes.size());
        for (int userIndex1 = 0; userIndex1 < vertexes.size(); userIndex1++) {
            String userId1 = vertexes.get(userIndex1);
            for (int userIndex2 = userIndex1 + 1; userIndex2 < vertexes.size(); userIndex2++) {
                String userId2 = vertexes.get(userIndex2);
                double value = /*distance.distance(idToInstance.get(userId1), idToInstance.get(userId2))*/1;
                a.put(userIndex1, userIndex2, value);
                a.put(userIndex2, userIndex1, value);
            }
        }

        DoubleMatrix b = new DoubleMatrix(vertexes.size(), vertexes.size());
        for (int userIndex1 = 0; userIndex1 < vertexes.size(); userIndex1++) {
            String userId1 = vertexes.get(userIndex1);
            int k1 = clusters.get(idToCluster.get(userId1)).size() - 1;
            for (int userIndex2 = 0; userIndex2 < vertexes.size(); userIndex2++) {
                String userId2 = vertexes.get(userIndex2);
                int k2 = clusters.get(idToCluster.get(userId2)).size() - 1;

                double value = a.get(userIndex1, userIndex2) - k1 * k2 / (2 * m);
                b.put(userIndex1, userIndex2, value);
                b.put(userIndex2, userIndex1, value);
            }
        }

        DoubleMatrix stb = s.transpose().mmul(b);
        DoubleMatrix stbs = stb.mmul(s);
        System.out.println(IntStream.range(0, stbs.getColumns()).mapToDouble(i -> stbs.get(i, i)).sum() / (2 * m));
    }

    private static Map<Integer, List<String>> getClusters() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader("clusters/" + Settings.CITY_NAME + "_" + METHOD_NAME + ".csv"))) {
            reader.readNext(); // skip header
            Map<Integer, List<String>> clusters = new HashMap<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                String userId = row[0];
                int clusterIndex = Integer.parseInt(row[1]);

                List<String> ids = clusters.getOrDefault(clusterIndex, new ArrayList<>());
                ids.add(userId);
                clusters.put(clusterIndex, ids);
            }
            return clusters;
        }
    }

    private static Map<String, Integer> idToCluster() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader("clusters/" + Settings.CITY_NAME + "_" + METHOD_NAME + ".csv"))) {
            reader.readNext(); // skip header
            Map<String, Integer> idToCluster = new HashMap<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                String userId = row[0];
                int clusterIndex = Integer.parseInt(row[1]);
                idToCluster.put(userId, clusterIndex);
            }
            return idToCluster;
        }
    }
}

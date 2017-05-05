package sg.edu.nus.comp.lms.domain;

import sg.edu.nus.comp.lms.domain.entity.SocialNetwork;
import sg.edu.nus.comp.lms.domain.reader.CSVReader;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;
import sg.edu.nus.comp.lms.domain.weka.operation.IntersectInstances;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

public class CommonMethods {

    private CommonMethods() {
    }

    public static List<Instances> readAllSources() {
        List<Instances> sources = new ArrayList<>();
        sources.add(new CSVReader(SocialNetwork.TWITTER, new File(Settings.FEATURE_FOLDER)).readAll());
        sources.add(new CSVReader(SocialNetwork.FOURSQUARE, new File(Settings.FEATURE_FOLDER)).readAll());
        sources.add(new CSVReader(SocialNetwork.INSTAGRAM, new File(Settings.FEATURE_FOLDER)).readAll());
        sources.add(new CSVReader(new File(Settings.TEMPORAL_MODAL_FILE)).readAll());
        sources.add(new CSVReader(new File(Settings.MOBILITY_MODAL_FILE)).readAll());
        return sources;
    }

    public static List<Instances> readAllIntersectedSources() {
        List<Instances> sources = readAllSources();
        for (int i = 0; i < sources.size(); i++) {
            for (int j = i + 1; j < sources.size(); j++) {
                new IntersectInstances(sources.get(i), sources.get(j)).eval();
            }
        }
        return sources;
    }

    public static Map<String, double[]> getTrain3(Set<String> testIds) {
        return getTrain(Settings.TRAIN3, testIds);
    }

    public static Map<String, double[]> getTrain2(Set<String> testIds) {
        return getTrain(Settings.TRAIN2, testIds);
    }

    public static Map<String, double[]> getTest2() {
        return getTest(Settings.TEST2);
    }

    public static Map<String, double[]> getTest1() {
        return getTest(Settings.TEST1);
    }

    private static Map<String, double[]> getTrain(String filename, Set<String> testIds) {
        return extractUsersVectors(new CSVReader(new File(filename)).readAll(), testIds);
    }

    private static Map<String, double[]> getTest(String filename) {
        return extractUsersVectors(new CSVReader(new File(filename)).readAll());
    }

    private static Map<String, double[]> extractUsersVectors(Instances instances, Set<String> ids) {
        Attribute idAttribute = instances.attribute(Settings.ID_ATTR);
        Map<String, double[]> usersVectors = new HashMap<>();

        for (Instance instance : instances) {
            String id = instance.stringValue(idAttribute);
            if (ids.contains(id)) {
                double[] values = IntStream.range(2, instances.numAttributes())
                        .mapToDouble(instance::value)
                        .toArray();
                usersVectors.put(id, ArraysUtils.normalize(values));
            }
        }

        return usersVectors;
    }

    private static Map<String, double[]> extractUsersVectors(Instances instances) {
        Attribute idAttribute = instances.attribute(Settings.ID_ATTR);
        Map<String, double[]> usersVectors = new HashMap<>();

        for (Instance instance : instances) {
            String id = instance.stringValue(idAttribute);
            double[] values = IntStream.range(2, instances.numAttributes())
                    .mapToDouble(instance::value)
                    .toArray();
            if (isCorrectInstance(values)) {
                usersVectors.put(id, ArraysUtils.normalize(values));
            }
        }

        return usersVectors;
    }

    private static boolean isCorrectInstance(double[] values) {
        return Arrays.stream(values)
                .filter(value -> value != 0)
                .count() >= Settings.MIN_VENUE_MENTION;
    }
}

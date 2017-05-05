package sg.edu.nus.comp.lms;

import sg.edu.nus.comp.lms.algorithm.util.ClusteringUtils;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.profiling.FoursquareLDA;
import sg.edu.nus.comp.lms.domain.profiling.FoursquareMapping;
import sg.edu.nus.comp.lms.domain.profiling.InstagramMapping;
import sg.edu.nus.comp.lms.domain.profiling.TwitterLDA;
import sg.edu.nus.comp.lms.domain.weka.operation.NormalizeInstances;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProfilingCommunities {

    private static final int SOURCE_INDEX = 4;

    private static final String SOURCE = Settings.MODAL_NAME[SOURCE_INDEX];
    private static final String FOLDER = "layers";
    private static final int TOP_FEATURES = 100;

    public static void main(String[] args) throws IOException {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();
        Instances source = sources.get(SOURCE_INDEX);

        File folder = new File(FOLDER);
        File[] files = folder.listFiles();
        if (files != null) {
            List<File> communities = Arrays.stream(files)
                    .filter(file -> file.getName().startsWith(SOURCE))
                    .collect(Collectors.toList());

            Map<File, Instance> communityCentroid = new HashMap<>();
            Map<File, List<Attribute>> communityAttributes = new HashMap<>();
            for (File file : communities) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    reader.readLine(); // skip number of user
                    List<String> ids = reader.lines().collect(Collectors.toList());

                    Instances instances = InstancesUtils.filterInstances(source, ids, Settings.ID_ATTR);
                    instances = new NormalizeInstances(instances).eval();
                    List<Instance> cluster = instances.stream().collect(Collectors.toList());
                    ArrayList<Attribute> attributes = (ArrayList<Attribute>) IntStream.range(0, instances.numAttributes())
                            .mapToObj(instances::attribute).collect(Collectors.toList());
                    Instance centroid = ClusteringUtils.findCentroid(cluster, attributes);
                    Instances tmpInstances = InstancesUtils.toInstances(Collections.singletonList(centroid), attributes);

                    List<Attribute> numericAttrs = IntStream.range(0, tmpInstances.numAttributes())
                            .mapToObj(tmpInstances::attribute)
                            .filter(Attribute::isNumeric)
                            .collect(Collectors.toList());
                    communityCentroid.put(file, centroid);
                    communityAttributes.put(file, numericAttrs);
                }
            }

            Map<Attribute, Double> attrLowerBound = new HashMap<>();
            List<Attribute> attrs = communityAttributes.values().stream().findFirst().get();
            for (Attribute attr : attrs) {
                double min = communityCentroid.values().stream().mapToDouble(inst -> inst.value(attr)).min().getAsDouble();
                double max = communityCentroid.values().stream().mapToDouble(inst -> inst.value(attr)).max().getAsDouble();
                double average = communityCentroid.values().stream().mapToDouble(inst -> inst.value(attr)).average().getAsDouble();

                double mid = (max + min) / 2;
                double threshold = max - Math.abs(mid - average);
                attrLowerBound.put(attr, threshold);
            }
            for (File file : communityAttributes.keySet()) {
                Instance centroid = communityCentroid.get(file);
                List<Attribute> numericAttrs = communityAttributes.get(file).stream()
                        .filter(attr -> centroid.value(attr) >= attrLowerBound.get(attr))
                        .sorted((o1, o2) -> -Double.compare(centroid.value(o1), centroid.value(o2)))
                        .collect(Collectors.toList());
                print(file.getName() + "_profile", numericAttrs, centroid, SOURCE_INDEX);
            }

        }
    }

    private static void print(String filename, List<Attribute> attributes, Instance centroid,
                              int sourceIndex) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename)) {
            switch (sourceIndex) {
                case 0: // twitter
                    printTwitter(writer, attributes, centroid);
                    break;
                case 1: // foursquare
                    printFoursquare(writer, attributes, centroid);
                    break;
                case 2: // instagram
                    printInstagram(writer, attributes, centroid);
                    break;
                case 3:
                case 4:
                    printLayer(writer, attributes, centroid);
                    break;
            }
        }
    }

    private static void printTwitter(PrintWriter writer, List<Attribute> attributes, Instance centroid) {
        TwitterLDA lda = new TwitterLDA();
        attributes.stream().limit(TOP_FEATURES)
                .forEach(attr -> {
                    String name = attr.name();
                    if (lda.getWords(name) != null) {
                        name = lda.getWords(name).stream().collect(Collectors.joining(", "));
                    }
                    writer.format("%.4f %s\n", centroid.value(attr), name);
                });
    }

    private static void printFoursquare(PrintWriter writer, List<Attribute> attributes, Instance centroid) {
        FoursquareLDA lda = new FoursquareLDA();
        FoursquareMapping mapping = new FoursquareMapping();
        attributes.stream().limit(TOP_FEATURES)
                .forEach(attr -> {
                    String name = attr.name();
                    if (lda.getCategories(name) != null) {
                        name = lda.getCategories(name).stream().collect(Collectors.joining(", "));
                    } else if (mapping.getCategoryByFeature(name) != null) {
                        name = mapping.getCategoryByFeature(name);
                    }
                    writer.format("%.4f %s\n", centroid.value(attr), name);
                });
    }

    private static void printInstagram(PrintWriter writer, List<Attribute> attributes, Instance centroid) {
        InstagramMapping mapping = new InstagramMapping();
        attributes.stream().limit(TOP_FEATURES)
                .forEach(attr -> {
                    String name = attr.name();
                    if (mapping.getCategory(name) != null) {
                        name = mapping.getCategory(name);
                    }
                    writer.format("%.4f %s\n", centroid.value(attr), name);
                });
    }

    private static void printLayer(PrintWriter writer, List<Attribute> attributes, Instance centroid) {
        attributes.stream().limit(TOP_FEATURES)
                .forEach(attr -> writer.format("%.4f %s\n", centroid.value(attr), attr.name()));
    }
}

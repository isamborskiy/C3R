package sg.edu.nus.comp.lms.domain.weka.util;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.operation.IntersectInstances;
import sg.edu.nus.comp.lms.domain.weka.operation.MergeInstances;
import weka.core.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstancesUtils {

    private static final String FILTERED_INSTANCES = "filtered_instances";
    private static final String GENERATED_INSTANCES = "generated_instances";
    private static final String ATTRIBUTE_PREFIX = "attr";

    public static Map<String, Instance> extractStringAttrToInstance(Instances instances, String attrName) {
        Attribute attribute = instances.attribute(attrName);
        return instances.stream()
                .collect(Collectors.toMap(
                        instance -> instance.stringValue(attribute),
                        Function.identity()
                ));
    }

    public static List<String> extractStringAttr(Instances instances, String attrName) {
        Attribute attribute = instances.attribute(attrName);
        return instances.stream()
                .map(instance -> instance.stringValue(attribute))
                .collect(Collectors.toList());
    }

    public static List<String> extractIntersectionStringAttr(List<Instances> sources, String attrName) {
        List<String> ids = extractStringAttr(sources.get(0), attrName);
        for (int i = 1; i < sources.size(); i++) {
            Set<String> tmp = new HashSet<>(extractStringAttr(sources.get(i), attrName));
            ids.removeIf(id -> !tmp.contains(id));
        }
        return ids;
    }

    public static Instances generateInstances(DoubleMatrix matrix, List<String> ids) {
        return generateInstances(matrix, ids, GENERATED_INSTANCES);
    }

    public static Instances generateInstances(DoubleMatrix matrix, List<String> ids, String relationName) {
        Attribute idAttribute = new Attribute(Settings.ID_ATTR, (FastVector) null);
        Instances instances = new Instances(relationName,
                generateAttributes(idAttribute, matrix.getColumns()), matrix.getRows());

        for (int i = 0; i < matrix.getRows(); i++) {
            Instance instance = new DenseInstance(instances.numAttributes());
            instance.setValue(idAttribute, ids.get(i));
            for (int j = 0; j < matrix.getColumns(); j++) {
                instance.setValue(instances.attribute(j + 1), matrix.get(i, j));
            }
            instances.add(instance);
        }

        return instances;
    }

    private static ArrayList<Attribute> generateAttributes(Attribute idAttribute, int k) {
        ArrayList<Attribute> attributes = new ArrayList<>(k + 1);
        attributes.add(idAttribute);
        IntStream.range(0, k).forEach(i -> attributes.add(new Attribute(ATTRIBUTE_PREFIX + i)));
        return attributes;
    }

    public static Instances toInstances(List<Instance> instances, ArrayList<Attribute> attributes) {
        return toInstances(GENERATED_INSTANCES, instances, attributes, instances.size());
    }

    public static Instances toInstances(String relationName, List<Instance> instances, ArrayList<Attribute> attributes) {
        return toInstances(relationName, instances, attributes, instances.size());
    }

    public static Instances toInstances(String relationName, List<Instance> instances, ArrayList<Attribute> attributes, int capacity) {
        Instances generatedInstances = new Instances(relationName, attributes, capacity);
        generatedInstances.addAll(instances);
        return generatedInstances;
    }

    public static Instances filterInstances(Instances instances, Collection<String> attrValues, String attrName) {
        List<Instance> filteredInstance = instances.stream()
                .filter(instance -> attrValues.contains(instance.stringValue(instances.attribute(attrName))))
                .collect(Collectors.toList());
        ArrayList<Attribute> attributes = IntStream.range(0, instances.numAttributes())
                .mapToObj(instances::attribute)
                .collect(Collectors.toCollection(ArrayList<Attribute>::new));

        Instances filteredInstances = new Instances(FILTERED_INSTANCES, attributes, filteredInstance.size());
        filteredInstances.addAll(filteredInstance);
        return filteredInstances;
    }

    public static Instance findInstance(Instances instances, String attrName, String attrValue) {
        Attribute attribute = instances.attribute(attrName);
        return instances.stream()
                .filter(instance -> instance.stringValue(attribute).equals(attrValue))
                .findFirst()
                .orElse(null);
    }

    public static ArrayList<Attribute> extractAttributes(Instances instances) {
        return (ArrayList<Attribute>) IntStream.range(0, instances.numAttributes())
                .mapToObj(instances::attribute)
                .collect(Collectors.toList());
    }

    public static Instances joinInstances(List<Instances> sources) {
        Instances instances = sources.get(0);
        for (int i = 1; i < sources.size(); i++) {
            instances = join(instances, sources.get(i));
        }
        instances.attribute(Settings.ID_ATTR).setWeight(0.); // put attribute name in params
        return instances;
    }

    public static Instances join(Instances first, Instances second) {
        new IntersectInstances(first, second).eval();
        return new MergeInstances(first, second).eval();
    }
}

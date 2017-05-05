package sg.edu.nus.comp.lms.domain.weka.operation;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MergeInstances extends BinaryInstanceOperation {

    protected static final String MERGED_DATASET_NAME = "merged_instances";

    public MergeInstances(Instances first, Instances second) {
        super(first, second);
    }

    @Override
    public Instances eval() {
        Map<String, Instance> firstMap = parseInstances(first);
        Map<String, Instance> secondMap = parseInstances(second);
        second.deleteAttributeAt(second.attribute(USER_ID_ATTR_NAME).index());

        List<Instance> mergedInstances = mergeInstances(firstMap, secondMap);
        ArrayList<Attribute> attributes = joinAttributes(first, second);

        Instances instances = new Instances(MERGED_DATASET_NAME, attributes, first.size() + second.size());
        instances.addAll(mergedInstances);
        return instances;
    }

    private Map<String, Instance> parseInstances(Instances instances) {
        Attribute userIdAttr = instances.attribute(USER_ID_ATTR_NAME);
        return instances.stream()
                .collect(Collectors.toMap(
                        instance -> getStringValue(instance, userIdAttr),
                        Function.identity()
                ));
    }

    private List<Instance> mergeInstances(Map<String, Instance> first, Map<String, Instance> second) {
        return first.keySet().stream()
                .map(id -> first.get(id).mergeInstance(second.get(id)))
                .collect(Collectors.toList());
    }

    private ArrayList<Attribute> joinAttributes(Instances first, Instances second) {
        ArrayList<Attribute> attributes = new ArrayList<>(first.size() + second.size());
        attributes.addAll(extractAttributes(first));
        attributes.addAll(extractAttributes(second));
        return attributes;
    }

    private List<Attribute> extractAttributes(Instances instances) {
        return IntStream.range(0, instances.numAttributes())
                .mapToObj(instances::attribute)
                .collect(Collectors.toList());
    }
}

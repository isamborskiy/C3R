package sg.edu.nus.comp.lms.domain.weka.operation;

import weka.core.Attribute;
import weka.core.Instances;

import java.util.Set;
import java.util.stream.Collectors;

public class IntersectInstances extends BinaryInstanceOperation {

    public IntersectInstances(Instances first, Instances second) {
        super(first, second);
    }

    /**
     * @return {@code null} cause modified instances put on constructor
     */
    @Override
    public Instances eval() {
        intersect(first, second);
        intersect(second, first);
        return null;
    }

    protected void intersect(Instances first, Instances second) {
        Set<String> ids = extractIds(second);
        Attribute userIdAttr = first.attribute(USER_ID_ATTR_NAME);
        first.removeIf(instance -> !ids.contains(getStringValue(instance, userIdAttr)));
    }

    private Set<String> extractIds(Instances instances) {
        Attribute userIdAttr = instances.attribute(USER_ID_ATTR_NAME);
        return instances.stream()
                .map(instance -> getStringValue(instance, userIdAttr))
                .collect(Collectors.toSet());
    }
}

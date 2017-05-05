package sg.edu.nus.comp.lms.domain.weka.distance;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public abstract class Distance {

    public abstract void initialize(Instances instances);

    public abstract double distance(Instance first, Instance second);

    protected double value(Instance instance, int attributeNumber) {
        return value(instance, instance.attribute(attributeNumber));
    }

    protected double value(Instance instance, Attribute attribute) {
        return instance.value(attribute) * attribute.weight();
    }
}

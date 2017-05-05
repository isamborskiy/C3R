package sg.edu.nus.comp.lms.domain.weka.operation;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public abstract class InstanceOperation {

    public static final String USER_ID_ATTR_NAME = "_id";

    public abstract Instances eval();

    protected String getStringValue(Instance instance, Attribute attribute) {
        try {
            switch (attribute.type()) {
                case Attribute.STRING:
                case Attribute.NOMINAL:
                    return instance.stringValue(attribute);
                default:
                    return String.valueOf(instance.value(attribute));
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

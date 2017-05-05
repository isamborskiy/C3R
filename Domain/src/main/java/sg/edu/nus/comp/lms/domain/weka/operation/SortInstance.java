package sg.edu.nus.comp.lms.domain.weka.operation;

import weka.core.Instances;

import java.util.Collections;

public class SortInstance extends InstanceOperation {

    private final Instances instances;

    public SortInstance(Instances instances) {
        this.instances = instances;
    }

    @Override
    public Instances eval() {
        int userIdAttrIndex = instances.attribute(USER_ID_ATTR_NAME).index();
        Collections.sort(instances, (o1, o2) -> o1.stringValue(userIdAttrIndex).compareTo(o2.stringValue(userIdAttrIndex)));
        return instances;
    }
}

package sg.edu.nus.comp.lms.domain.weka.operation;

import weka.core.Instances;

public abstract class BinaryInstanceOperation extends InstanceOperation {

    protected final Instances first;
    protected final Instances second;

    public BinaryInstanceOperation(Instances first, Instances second) {
        this.first = first;
        this.second = second;
    }
}

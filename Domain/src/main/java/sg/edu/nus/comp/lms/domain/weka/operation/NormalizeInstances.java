package sg.edu.nus.comp.lms.domain.weka.operation;

import sg.edu.nus.comp.lms.domain.entity.Pair;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public class NormalizeInstances extends InstanceOperation {

    private final Instances instances;

    public NormalizeInstances(Instances instances) {
        this.instances = instances;
    }

    @Override
    public Instances eval() {
        Instances normalizedInstances = new Instances(instances);

        Map<Attribute, Pair<Double, Double>> attrToBoard = new HashMap<>();
        for (int i = 0; i < normalizedInstances.numAttributes(); i++) {
            Attribute attribute = normalizedInstances.attribute(i);
            if (attribute.isNumeric()) {
                attrToBoard.put(attribute, getMinMax(attribute, normalizedInstances));
            }
        }

        for (int i = 0; i < normalizedInstances.numAttributes(); i++) {
            Attribute attribute = normalizedInstances.attribute(i);
            if (attribute.isNumeric()) {
                for (Instance instance : normalizedInstances) {
                    double value = instance.value(attribute);
                    Pair<Double, Double> board = attrToBoard.get(attribute);
                    instance.setValue(attribute, (value - board.first) / board.second);
                }
            }
        }

        return normalizedInstances;
    }

    private Pair<Double, Double> getMinMax(Attribute attribute, Instances instances) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Instance instance : instances) {
            double value = instance.value(attribute);
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        return new Pair<>(min, max - min);
    }
}

package sg.edu.nus.comp.lms.domain.weka.distance;

import sg.edu.nus.comp.lms.domain.entity.Pair;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public abstract class NormalizedDistance extends Distance {

    private Map<Attribute, Pair<Double, Double>> attrToBoard;

    @Override
    public void initialize(Instances instances) {
        this.attrToBoard = new HashMap<>();
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            if (attribute.isNumeric()) {
                attrToBoard.put(attribute, getMinMax(attribute, instances));
            }
        }
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

    @Override
    protected double value(Instance instance, Attribute attribute) {
        Pair<Double, Double> minMax = attrToBoard.get(attribute);
        return (instance.value(attribute) - minMax.first) / minMax.second * attribute.weight();
    }
}

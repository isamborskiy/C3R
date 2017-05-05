package sg.edu.nus.comp.lms.domain.weka.util;

import weka.core.Attribute;
import weka.core.Instance;

import java.util.stream.IntStream;

import static java.lang.Math.pow;

public class InstanceUtils {

    public static int attrIndexOf(Instance instance, String name) {
        for (int i = 0; i < instance.numAttributes(); i++) {
            Attribute attribute = instance.attribute(i);
            if (attribute.name().equals(name)) {
                return attribute.index();
            }
        }
        return -1;
    }

    public static double norm2(Instance first, Instance second) {
        return Math.sqrt(IntStream.range(0, first.numAttributes())
                .filter(i -> first.attribute(i).isNumeric())
                .mapToDouble(i -> pow((first.value(i) - second.value(i)) * first.attribute(i).weight(), 2))
                .sum());
    }
}

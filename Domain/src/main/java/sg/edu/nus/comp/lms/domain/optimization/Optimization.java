package sg.edu.nus.comp.lms.domain.optimization;

import java.util.function.Function;

public interface Optimization {

    default double[] findParams(Function<double[], Double> model) {
        return findParams(model, false);
    }

    double[] findParams(Function<double[], Double> model, boolean log);
}

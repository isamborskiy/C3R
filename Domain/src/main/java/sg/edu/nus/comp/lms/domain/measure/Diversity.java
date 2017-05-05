package sg.edu.nus.comp.lms.domain.measure;

import java.util.HashSet;
import java.util.Set;

public class Diversity implements Measure {

    private final int k;
    private final Set<Integer> recommendations;

    public Diversity(int k) {
        this.k = k;
        this.recommendations = new HashSet<>();
    }

    @Override
    public double get(int[] recommendation) {
        for (int i = 0; i < k; i++) {
            recommendations.add(recommendation[i]);
        }
        return recommendations.size();
    }
}

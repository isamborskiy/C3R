package sg.edu.nus.comp.lms.domain.optimization;

public abstract class AbstractOptimization implements Optimization {

    protected final double[] minParams;
    protected final double[] maxParams;
    protected final double[] step;

    public AbstractOptimization(double[] minParams, double[] maxParams, double[] step) {
        this.minParams = minParams;
        this.maxParams = maxParams;
        this.step = step;
    }

    protected void log(boolean log, String message) {
        if (log) {
            System.out.println(message);
        }
    }
}

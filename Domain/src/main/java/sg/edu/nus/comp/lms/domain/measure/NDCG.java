package sg.edu.nus.comp.lms.domain.measure;

public class NDCG extends DCG {

    private final IDCG idcg;

    public NDCG(double[] userVector, int k) {
        super(userVector, k);
        this.idcg = new IDCG(userVector, k);
    }

    @Override
    public double get(int[] recommendation) {
        return super.get(recommendation) / idcg.get(recommendation);
    }
}

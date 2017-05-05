package sg.edu.nus.comp.lms.util;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.multi.GraphRegularizedSpectralMultiLayerClustering;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.measure.Diversity;
import sg.edu.nus.comp.lms.domain.measure.Measure;
import sg.edu.nus.comp.lms.domain.measure.NDCG;
import sg.edu.nus.comp.lms.model.Recommender;
import sg.edu.nus.comp.lms.model.multi.LateFusion;
import sg.edu.nus.comp.lms.model.multi.SubcommunityRecommender.SubcommunityRecommenderFactory;
import sg.edu.nus.comp.lms.model.multi.c3r.C3R;
import sg.edu.nus.comp.lms.model.multi.c3r.C3RUnreg;
import sg.edu.nus.comp.lms.model.multi.c3r.LCapC3R;
import sg.edu.nus.comp.lms.model.single.*;
import weka.core.Instances;

import java.util.List;
import java.util.Map;

public class EvaluateMethods {

    private EvaluateMethods() {
    }

    public static double evaluateModelNDCG(Recommender model, Map<String, double[]> testData, int ndcgParam) {
        double measure = 0;
        int count = 0;
        for (String id : testData.keySet()) {
            if (model.canRecommend(id)) {
                int[] recommendation = model.recommend(id);
                measure += new NDCG(testData.get(id), ndcgParam).get(recommendation);
                count++;
            }
        }
        measure /= count;
        return measure;
    }

    public static double evaluateModelDiversity(Recommender model, Map<String, double[]> testData, int k) {
        double lastValue = 0;
        int count = 0;
        Measure measure = new Diversity(k);
        for (String id : testData.keySet()) {
            if (model.canRecommend(id)) {
                int[] recommendation = model.recommend(id);
                lastValue = measure.get(recommendation);
                count++;
            }
        }
        return lastValue / count;
    }

    public static Recommender getC3RModel(List<Instances> sources) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getC3RSingapore().create(sources);
        }
        return null;
    }

    public static C3R.C3RFactory getC3RSingapore() {
        C3R.C3RFactory factory = new C3R.C3RFactory();
        factory.setClusterNumber(10);
        factory.setAlpha(0.5205085532091129);
        factory.setBeta(0.8186413440954143);
        factory.setGamma(0.08192869479109814);
        factory.setK(28);
        return factory;
    }

    public static Recommender getC3RWithoutRegularizationModel(List<Instances> sources) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getC3RSingaporeWithoutRegularization().create(sources);
        }
        return null;
    }

    public static C3RUnreg.C3RUnregFactory getC3RSingaporeWithoutRegularization() {
        C3RUnreg.C3RUnregFactory factory = new C3RUnreg.C3RUnregFactory();
        factory.setClusterNumber(22);
        factory.setBeta(0.9794169535665517);
        factory.setGamma(0.09767367350671878);
        factory.setK(30);
        return factory;
    }

    public static Recommender getLCapC3RModel(List<Instances> sources) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getLCapC3RSingapore().create(sources);
        }
        return null;
    }

    public static LCapC3R.LCapC3RFactory getLCapC3RSingapore() {
        LCapC3R.LCapC3RFactory factory = new LCapC3R.LCapC3RFactory();
        factory.setWeights(new DoubleMatrix(5, 5, 0.000000, 0.846154, 0.980411, 0.718462, 0.836102, 0.846154, 0.000000, 1.006731, 0.809419, 0.961554, 0.980411, 1.006731, 0.000000, 0.987747, 0.897505, 0.718462, 0.809419, 0.987747, 0.000000, 0.863983, 0.836102, 0.961554, 0.897505, 0.863983, 0.000000));
        factory.setClusterNumber(10);
        factory.setAlpha(0.13990390031709746);
        factory.setBeta(0.7758022350152981);
        factory.setGamma(0.021026137984037532);
        factory.setK(31);
        return factory;
    }

    public static Recommender getSubcommunityRecommender(List<Instances> sources) {
        return getSubcommunityRecommender(sources, 1);
    }

    public static Recommender getSubcommunityRecommender(List<Instances> sources, int index) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getSubcommunitySingapore(index).create(sources);
        }
        return null;
    }

    public static SubcommunityRecommenderFactory getSubcommunitySingapore(int index) {
        SubcommunityRecommenderFactory factory = new SubcommunityRecommenderFactory(index);
        factory.setMultiClusterNumber(10);
        factory.setSingleClusterNumber(8);
        factory.setMultiFactory(new GraphRegularizedSpectralMultiLayerClustering.GraphRegularizedSpectralMultiLayerClusteringFactory(0.13990390031709746));
        factory.setRecommenderFactory(getPopularCommunitySingapore());
        return factory;
    }

    public static Recommender getPopularCommunityRecommender(List<Instances> sources, int index) {
        return getPopularCommunityRecommender(sources.get(index));
    }

    public static Recommender getPopularCommunityRecommender(Instances instances) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getPopularCommunitySingapore().create(instances);
        }
        return null;
    }

    public static PopularCommunity.PopularCommunityFactory getPopularCommunitySingapore() {
        PopularCommunity.PopularCommunityFactory factory = new PopularCommunity.PopularCommunityFactory();
        factory.setK(31);
        factory.setBeta(0.7758022350152981);
        factory.setGamma(0.021026137984037532);
        return factory;
    }

    public static Recommender getPopular(List<Instances> sources, int index) {
        return getPopular(sources.get(index));
    }

    public static Recommender getPopular(Instances instances) {
        return new Popular(instances);
    }

    public static Recommender getPopularHistory(List<Instances> sources, int index) {
        return getPopularHistory(sources.get(index));
    }

    public static Recommender getPopularHistory(Instances instances) {
        return new PopularHistory(instances);
    }

    public static Recommender getCFPlusPopular(List<Instances> sources, int index) {
        CFPlusPopular.CFPlusPopularFactory factory = new CFPlusPopular.CFPlusPopularFactory();
        factory.setAlpha(1);
        factory.setBeta(0.15);
        return factory.create(sources.get(index));
    }

    public static Recommender getSVD() {
        return new SVD();
    }

    public static Recommender getCFSingle(List<Instances> sources, int index) {
        return getCFSingle(sources.get(index));
    }

    public static Recommender getCFSingle(Instances instances) {
        return new CFSingleLayer(instances);
    }

    public static Recommender getCFEarlyFusion(List<Instances> sources) {
        return new CFJoinedLayers(sources);
    }

    public static Recommender getCFLateFusion(List<Instances> sources) {
        switch (Settings.CITY) {
            case LONDON:
            case NEW_YORK:
            case SINGAPORE:
                return getLateFusionSingapore().create(sources);
        }
        return null;
    }

    public static LateFusion.LateFusionFactory getLateFusionSingapore() {
        LateFusion.LateFusionFactory factory = new LateFusion.LateFusionFactory();
        factory.setWeights(new double[]{0.0, 0.9842842993842926, 0.0, 0.0, 0.0});
        factory.setK(26);
        return factory;
    }
}

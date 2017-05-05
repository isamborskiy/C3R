package sg.edu.nus.comp.lms.model;

import org.junit.Test;
import sg.edu.nus.comp.lms.algorithm.multi.SpectralMultiLayerClustering;
import sg.edu.nus.comp.lms.domain.reader.CSVReader;
import sg.edu.nus.comp.lms.domain.weka.operation.IntersectInstances;
import sg.edu.nus.comp.lms.model.multi.SubcommunityRecommender;
import sg.edu.nus.comp.lms.model.single.PopularCommunity;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubcommunityRecommenderTest {

    private static File getFileFromResources(String filename) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
    }

    @Test
    public void simpleTest() throws Exception {
        Map<String, double[]> trainData = new HashMap<>();
        trainData.put("user1", new double[]{1., 0., 1.});
        trainData.put("user2", new double[]{0., 1., 1.});

        List<Instances> sources = new ArrayList<>();
        sources.add(new CSVReader(getFileFromResources("simpleTest.csv")).readAll());
        sources.add(new CSVReader(getFileFromResources("simpleTest2.csv")).readAll());
        new IntersectInstances(sources.get(0), sources.get(1)).eval();

        SubcommunityRecommender.SubcommunityRecommenderFactory factory = new SubcommunityRecommender.SubcommunityRecommenderFactory(0);
        factory.setMultiClusterNumber(2);
        factory.setSingleClusterNumber(2);
        factory.setMultiFactory(new SpectralMultiLayerClustering.SpectralMultiLayerClusteringFactory(0.13990390031709746));

        PopularCommunity.PopularCommunityFactory innerFactory = new PopularCommunity.PopularCommunityFactory();
        innerFactory.setK(31);
        innerFactory.setBeta(0.7758022350152981);
        innerFactory.setGamma(0.021026137984037532);
        factory.setRecommenderFactory(innerFactory);

        factory.create(sources).train(trainData);
    }
}

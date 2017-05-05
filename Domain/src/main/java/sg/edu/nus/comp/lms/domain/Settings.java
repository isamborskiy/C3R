package sg.edu.nus.comp.lms.domain;

import sg.edu.nus.comp.lms.domain.entity.City;

import java.util.stream.IntStream;

public class Settings {

    public static final String ID_ATTR = "_id";

    public static final int DEFAULT_K = 20;
    public static final int MIN_VENUE_MENTION = 10;
    public static final int DEFAULT_NDCG_K = 60;

    public static final int DEFAULT_SEED = 10; // magic constant

    public static final City CITY = City.LONDON;
    public static final String CITY_NAME = CITY.toString();
    public static final String FEATURE_FOLDER = "features" + Settings.CITY_NAME;
    public static final String TEMPORAL_MODAL_FILE = "temporalPattern" + Settings.CITY_NAME + ".csv";
    public static final String MOBILITY_MODAL_FILE = "AOIFeatures" + Settings.CITY_NAME + ".csv";

    public static final String[] MODAL_NAME = {"twitter", "foursquare", "instagram", "temporal", "mobility"};
    public static final String[] MODAL_SHORT_NAME = {"tw", "fsq", "inst", "", ""};

    public static final String[] LAPLACIANS_MATRICES;
    public static final String[] EIGENVECTORS_MATRICES;
    public static final String[] EIGENVALUES_MATRICES;

    public static final String TRAIN3 = "features" + CITY_NAME + "/Foursquare/venueCategoriesFeatures3MonthsTrain.csv";
    public static final String TRAIN2 = "features" + CITY_NAME + "/Foursquare/venueCategoriesFeatures2MonthsTrain.csv";
    public static final String TEST2 = "features" + CITY_NAME + "/Foursquare/venueCategoriesFeatures2MonthsTest.csv";
    public static final String TEST1 = "features" + CITY_NAME + "/Foursquare/venueCategoriesFeatures1MonthTest.csv";

    public static final String FOURSQUARE_LDA = "features" + CITY_NAME + "/Foursquare/descriptions/venueCategoriesLDA6TopicTerms.csv";
    public static final String FOURSQUARE_MAPPING = "features" + CITY_NAME + "/Foursquare/descriptions/featuresCategoryMapping.csv";
    public static final String INSTAGRAM_MAPPING = "features" + CITY_NAME + "/Instagram/descriptions/imageConceptsDescriptionsMapping.csv";
    public static final String TWITTER_LDA = "features" + CITY_NAME + "/Twitter/descriptions/LDA50FeaturesTopicTerms.csv";

    private static final String VORONTSOV_PREFIX = "D:/Internship/Contribution/Topic" + CITY_NAME;
    public static final String VORONTSOV_CHECKINS = VORONTSOV_PREFIX + "/checkins" + CITY_NAME + ".csv";
    public static final String VORONTSOV_IMAGES = VORONTSOV_PREFIX + "/images" + CITY_NAME + ".csv";
    public static final String VORONTSOV_TWEETS = VORONTSOV_PREFIX + "/tweets" + CITY_NAME + ".csv";

    private static final String GT_PREFIX = "D:/Internship/NUS MSS Datasets";
    public static final String GT_FILE = GT_PREFIX + "/" + CITY_NAME + "GroundTruth.csv";

    static {
        LAPLACIANS_MATRICES = IntStream.range(0, MODAL_NAME.length)
                .mapToObj(i -> MODAL_NAME[i] + "_laplacian.csv").toArray(String[]::new);
        EIGENVECTORS_MATRICES = IntStream.range(0, MODAL_NAME.length)
                .mapToObj(i -> MODAL_NAME[i] + "_eigenvectors.csv").toArray(String[]::new);
        EIGENVALUES_MATRICES = IntStream.range(0, MODAL_NAME.length)
                .mapToObj(i -> MODAL_NAME[i] + "_eigenvalues.csv").toArray(String[]::new);
    }

    private Settings() {
    }
}

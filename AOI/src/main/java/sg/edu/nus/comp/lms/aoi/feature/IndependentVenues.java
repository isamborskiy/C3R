package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.UserAOI;

public class IndependentVenues implements AOIFeature {

    @Override
    public String getValue(UserAOI userAOI) {
        return String.format("%.3f", userAOI.getPercentOfNoise());
    }

    @Override
    public String getName() {
        return "independent_venues";
    }
}

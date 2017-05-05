package sg.edu.nus.comp.lms.aoi.util;

import sg.edu.nus.comp.lms.aoi.entity.CartesianPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FastConvexHull {

    public List<CartesianPoint> execute(List<CartesianPoint> points) {
        ArrayList<CartesianPoint> sortedPoints = new ArrayList<>(points);
        Collections.sort(sortedPoints);

        if (sortedPoints.size() <= 3) {
            return sortedPoints;
        }

        int n = sortedPoints.size();

        CartesianPoint[] lUpper = new CartesianPoint[n];

        lUpper[0] = sortedPoints.get(0);
        lUpper[1] = sortedPoints.get(1);

        int lUpperSize = 2;

        for (int i = 2; i < n; i++) {
            lUpper[lUpperSize] = sortedPoints.get(i);
            lUpperSize++;

            while (lUpperSize > 2 && !isRightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                lUpperSize--;
            }
        }

        CartesianPoint[] lLower = new CartesianPoint[n];

        lLower[0] = sortedPoints.get(n - 1);
        lLower[1] = sortedPoints.get(n - 2);

        int lLowerSize = 2;

        for (int i = n - 3; i >= 0; i--) {
            lLower[lLowerSize] = sortedPoints.get(i);
            lLowerSize++;

            while (lLowerSize > 2 && !isRightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                lLowerSize--;
            }
        }

        ArrayList<CartesianPoint> result = new ArrayList<>();
        result.addAll(Arrays.asList(lUpper).subList(0, lUpperSize));
        result.addAll(Arrays.asList(lLower).subList(1, lLowerSize - 1));

        return result;
    }

    private boolean isRightTurn(CartesianPoint a, CartesianPoint b, CartesianPoint c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }
}

package utils;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.Pair;

import java.util.LinkedList;

/**
 * Variety of useful functions
 */
public abstract class Utils {
    public static final MersenneTwister rand = new MersenneTwister(System.currentTimeMillis());

    /**
     * Search points in circle with middle in point (x,y) and radius r
     *
     * @param x - coordinate x of point
     * @param y - coordinate y of point
     * @param r - radius
     * @return list of points in given circle
     */
    public static LinkedList<Pair<Integer, Integer>> getPointsInVicinity(int x, int y, int r) {
        LinkedList<Pair<Integer, Integer>> returnList = new LinkedList<>();
        for (int i = x - r; i <= x + r; i++) {
            for (int j = y - r; j <= y + r; j++) {
                if ((Math.pow(x - i, 2) + Math.pow(y - j, 2)) <= r * r) {
                    returnList.add(new Pair<>(i, j));
                }
            }
        }
        return returnList;
    }

    /**
     * Calculates sum of squares of given values
     *
     * @param x - value
     * @param y - value
     * @return x^2 + y^2
     */
    private static double calculateSumOfSquares(double x, double y) {
        return Math.pow(x, 2) + Math.pow(y, 2);
    }

    /**
     * Calculates distance between points a and b
     *
     * @param a - point a
     * @param b - point b
     * @return distance
     */
    public static double calculateSquareDistancePoints(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return calculateSumOfSquares(a.getFirst() - b.getFirst(), a.getSecond() - b.getSecond());
    }

    /**
     * Calculates distance between points a and b
     *
     * @param x1 - coordinate x of point a
     * @param y1 - coordinate y of point a
     * @param x2 - coordinate x of point b
     * @param y2 - coordinate y of point b
     * @return distance
     */
    public static double calculateSquareDistancePoints(double x1, double y1, double x2, double y2) {
        return calculateSumOfSquares(x1 - x2, y1 - y2);
    }

    /**
     * Checks if LU is left upper corner and RD is right bottom, if not, returns coordinates in correct order in points
     *
     * @param constraintLU - left upper point
     * @param constraintRD - right bottom point
     * @return pair of points
     */
    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> makeRectangleProper(Pair<Integer, Integer> constraintLU, Pair<Integer, Integer> constraintRD) {
        int x1 = constraintLU.getFirst(),
                x2 = constraintRD.getFirst(),
                y1 = constraintLU.getSecond(),
                y2 = constraintRD.getSecond();
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        return new Pair<>(new Pair<>(x1, y1), new Pair<>(x2, y2));
    }
}

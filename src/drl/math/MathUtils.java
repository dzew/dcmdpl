/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package drl.math;

import java.util.ArrayList;
import java.util.List;

import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.math.tfs.Transform;

/**
 * A static utility class with convenience methods for mathematical operations.
 * 
 * @author Dawit
 * 
 */
public class MathUtils {

    private MathUtils() {
    }

    /**
     * Returns the dot product of its two inputs
     */
    public static double dot(int[] v1, double[] v2) {
        double d = 0;
        for (int i = 0; i < v1.length; i++) {
            d += v1[i] * v2[i];
        }
        return d;
    }

    /**
     * Returns the dot product of its two inputs
     */
    public static double dot(int[] v1, Vector v2) {
        double d = 0;
        for (int i = 0; i < v1.length; i++) {
            d += v1[i] * v2.get(i);
        }
        return d;
    }

    /**
     * Returns the square of the Euclidean distance between v1 and v2.
     */
    public static double squaredDistance(Vector v1, Vector v2) {
        double dist = 0;
        for (int i = 0; i < v1.getDimensions(); i++) {
            dist += (v1.get(i) - v2.get(i)) * (v1.get(i) - v2.get(i));
        }
        return dist;
    }

    /**
     * Returns the square of the Euclidean distance between v1 and v2.
     */
    public static double squaredDistance(double[] v1, double[] v2) {
        double dist = 0;
        for (int i = 0; i < v1.length; i++) {
            dist += (v1[i] - v2[i]) * (v1[i] - v2[i]);
        }
        return dist;
    }

    /**
     * Returns {@code number} raised to {@code power}. The returned value is
     * unspecified when there is an Integer overflow.
     */
    public static int raise(int number, int power) {
        int ret = number;
        for (int i = 1; i < power; i++) {
            ret *= number;
        }
        return ret;
    }

    /**
     * Returns sum([v**e for e,v in zip(exponents, values)]).
     */
    public static double raise(int[] exponents, double[] values) {
        double d = 1.;
        for (int i = 0; i < exponents.length; i++) {
            for (int j = 0; j < exponents[i]; j++) {
                d *= values[i];
            }
        }
        return d;
    }

    /**
     * Returns sum([v**e for e,v in zip(exponents, values)]).
     */
    public static double raise(int[] exponents, Vector values) {
        double d = 1.;
        for (int i = 0; i < exponents.length; i++) {
            for (int j = 0; j < exponents[i]; j++) {
                d *= values.get(i);
            }
        }
        return d;
    }

    /**
     * Returns the number of terms in a Taylor polynomial of the given degree
     * with the given number of dimensions.
     * 
     */
    public static int taylorExpansionTerms(int dimensions, int maxDegree) {
        return waysToDistribute(maxDegree, dimensions + 1);
    }

    /**
     * Returns the number of terms in a Fourier sum of the given degree with the
     * given number of dimensions.
     * 
     */
    public static int fourierExpansionTerms(int dimensions, int maxHarmonic) {
        return raise(maxHarmonic, dimensions);
    }

    /**
     * Return an element from {@code as} selected u.a.r.
     */
    public static <A> A sample(A[] as) {
        return as[(int) (Math.random() * as.length)];
    }

    /**
     * Increment the counter until the next time the sum of the entries is below
     * the cap. Modifies the given array.
     */
    public static boolean incrementWithSumCap(int[] counter, int cap) {
        do {
            increment(counter, cap + 1);
        } while (sum(counter) > cap);
        return counter[counter.length - 1] == cap;
    }

    /**
     * Increment the counter with the given radix. Modifies the given array.
     */
    public static void increment(int[] counter, int radix) {
        for (int i = 0; i < counter.length; i++) {
            counter[i] += 1;
            if (counter[i] < radix) {
                return;
            }
            counter[i] = 0;
        }
        throw new IllegalStateException("Increment overflow. You probably didn't want that.");
    }

    /**
     * Returns the Cell corresponding to [0, 1]^dimensions
     */
    public static Cell unitCell(int dimensions) {
        return regularCell(dimensions, 0, 1);
    }

    /**
     * Returns the Cell corresponding to [start, start + width]^dimensions.
     * Width must be positive.
     */
    public static Cell regularCell(int dimensions, double start, double width) {
        Interval unitInterval = new Interval(start, width);
        Interval[] intervals = new Interval[dimensions];
        for (int i = 0; i < dimensions; i++) {
            intervals[i] = unitInterval;
        }
        return Cell.of(intervals);
    }

    /**
     * Returns a Vector drawn u.a.r from domain.
     */
    public static Vector sampleUniformly(Cell domain) {
        double[] d = new double[domain.getDimensions()];
        for (int i = 0; i < d.length; i++) {
            Interval interval = domain.getInterval(i);
            d[i] = Math.random() * interval.getWidth() + interval.getStart();
        }
        return Vector.asVector(d);
    }

    /**
     * Scales the given vector using the transform that maps {@code from} to
     * {@code to}.
     */
    public static Vector rescale(Cell from, Cell to, Vector original) {
        double[] d = new double[original.getDimensions()];
        for (int i = 0; i < d.length; i++) {
            Interval start = from.getInterval(i);
            Interval end = to.getInterval(i);
            d[i] = end.getStart() + (original.get(i) - start.getStart()) / start.getWidth()
                    * end.getWidth();
        }
        return Vector.asVector(d);
    }

    /**
     * Returns [tf(v) for v in vecs].
     */
    public static List<Vector> transformAll(List<Vector> vecs, Transform tf) {
        List<Vector> ret = new ArrayList<Vector>(vecs.size());
        for (Vector v : vecs) {
            ret.add(tf.transform(v));
        }
        return ret;
    }

    /**
     * 
     * @param samples
     *            The number of samples per dimension.
     * @param domain
     *            The domain to be sampled.
     * @return A List of vectors that tile the given Cell.
     */
    public static List<Vector> tilingSample(int samples, Cell domain) {
        List<Vector> vectors = new ArrayList<Vector>(samples * domain.getDimensions());
        double[] values = new double[domain.getDimensions()];
        sampleSubspace(values, 0, domain, samples, vectors);
        return vectors;
    }

    /**
     * @param array
     * @return The sum of the elements in the given array.
     */
    public static int sum(int[] array) {
        int sum = 0;
        for (int i : array) {
            sum += i;
        }
        return sum;
    }

    /**
     * @param array
     * @return The sum of the elements in the given array.
     */
    public static double sum(double[] array) {
        double sum = 0;
        for (double i : array) {
            sum += i;
        }
        return sum;
    }

    /**
     * Clips {@code value} to lie within [min, max]
     */
    public static double clip(double value, double min, double max) {
        if (value <= min) {
            return min;
        }
        return Math.min(value, max);
    }

    private static void sampleSubspace(double[] values, int index, Cell domain, int samples,
            List<Vector> vectors) {
        if (index == values.length) {
            vectors.add(Vector.asVector(values));
        } else {
            double stepSize = domain.getInterval(index).getWidth() / samples;
            values[index] = domain.getInterval(index).getStart() + stepSize / 2;
            for (int i = 0; i < samples; i++) {
                sampleSubspace(values, index + 1, domain, samples, vectors);
                values[index] += stepSize;
            }
        }
    }

    private static int waysToDistribute(int items, int people) {
        if (people == 1) {
            return 1;
        }
        if (items == 1) {
            return people;
        }
        int sum = 0;
        for (int i = 0; i <= items; i++) {
            sum += waysToDistribute(i, people - 1);
        }
        return sum;
    }

    /**
     * 
     * @return The Euclidean distace between a pair of maximally distant points
     *         in the given Cell.
     */
    public static double diameterOf(Cell cell) {
        double sum = 0;
        for (int i = 0; i < cell.getDimensions(); i++) {
            double d = cell.getInterval(i).getWidth();
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

}

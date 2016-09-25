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

package drl.math.tfs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import drl.math.geom.Vector;
import drl.math.vfa.ValueFunction;

/**
 * Implementation of a Dimension-Adding VCM Relaxation.
 * 
 * @author Dawit
 * 
 */
public class ValueSmoothingDF implements DistanceFunction {

    private final DistanceFunction df;
    private final ValueFunction vf;
    private final double c1;
    private final Map<Vector, Double> cache;
    private final double aa;

    private ValueSmoothingDF(DistanceFunction df, ValueFunction vf, double targetSlope,
            double alpha, boolean threadSafe) {
        this.df = df;
        this.vf = vf;
        this.aa = alpha * alpha;
        this.c1 = aa / targetSlope / targetSlope;
        this.cache = threadSafe ? new ConcurrentHashMap<Vector, Double>()
                : new HashMap<Vector, Double>();
    }

    /**
     * Static constructor for a ValueSmoothingDf
     * 
     * @param df
     *            The old metric.
     * @param vf
     *            The function to be smoothed.
     * @param targetSlope
     *            1/mu_f of the value function (y_max - y_min)/diam(X).
     * @param alpha
     *            The relaxation rate.
     * @param threadSafe
     *            Set this to true to create a thread-safe ValueSmoothingDF
     *            instance.
     * @return
     */
    public static ValueSmoothingDF of(DistanceFunction df, ValueFunction vf, double targetSlope,
            double alpha, boolean threadSafe) {
        return new ValueSmoothingDF(df, vf, targetSlope, alpha, threadSafe);
    }

    @Override
    public double distance(Vector v1, Vector v2) {
        double dx = df.distance(v1, v2);
        double dy = Math.abs(getValue(v1) - getValue(v2));
        return Math.sqrt((dx * dx + dy * dy * c1) / (1 + aa));
    }

    private double getValue(Vector v) {
        if (!cache.containsKey(v)) {
            double val = vf.value(v);
            cache.put(v, val);
            return val;
        }
        return cache.get(v);
    }

    @Override
    public void memoize(Vector v) {
        if (!cache.containsKey(v)) {
            cache.put(v, vf.value(v));
        }
    }

}

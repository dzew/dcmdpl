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

package drl.math.vfa;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Vector;

/**
 * ValueFunction represented as the sum of sines. See the docs for CosineVf.
 * 
 * @author Dawit
 * 
 */
public class SineVF implements ValueFunction {

    final Vector coeffs;

    private final double[] offsets;
    private final double[] multipliers;
    private final int harmonics;

    private SineVF(Vector coeffs, double[] offsets, double[] multipliers, int harmonics) {
        this.offsets = offsets;
        this.multipliers = multipliers;
        this.coeffs = coeffs;
        this.harmonics = harmonics;
    }

    public static SineVF of(Vector coeffs, Cell domain, int terms) {
        double[] offsets = new double[domain.getDimensions()];
        double[] multipliers = new double[domain.getDimensions()];
        for (int i = 0; i < domain.getDimensions(); i++) {
            offsets[i] = domain.getInterval(i).getStart();
            multipliers[i] = Math.PI / domain.getInterval(i).getWidth();
        }
        return new SineVF(coeffs, offsets, multipliers, terms);
    }

    @Override
    public double value(Vector v) {
        double[] point = normalized(v);
        int[] counter = new int[point.length];
        double value = coeffs.get(0);
        for (int i = 1; i < coeffs.getDimensions(); i++) {
            MathUtils.increment(counter, harmonics);
            value += coeffs.get(i) * Math.sin(MathUtils.dot(counter, point));
        }
        return value;
    }

    private double[] normalized(Vector v) {
        double[] ret = new double[v.getDimensions()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = MathUtils.clip(multipliers[i] * (v.get(i) - offsets[i]), 0, Math.PI);
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SineVF(coeffs = " + coeffs + " rescale = [");
        for (int i = 0; i < offsets.length; i++) {
            sb.append(String.format("%.2f(x_%d-%.2f),", multipliers[i], i, offsets[i]));
        }
        sb.append("] terms = " + harmonics + ")");
        return sb.toString();
    }

    @Override
    public double difference(ValueFunction vf) {
        if (!(vf instanceof SineVF)) {
            return Double.POSITIVE_INFINITY;
        }
        return MathUtils.squaredDistance(coeffs, ((SineVF) vf).coeffs);
    }

}

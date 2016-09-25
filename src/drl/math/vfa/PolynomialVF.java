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
 * A ValueFunction represented as a polynomial.
 * 
 * @author Dawit
 * 
 */
public class PolynomialVF implements ValueFunction {

    final Vector coeffs;

    private final double[] offsets;
    private final double[] multipliers;
    private final int degree;

    private PolynomialVF(Vector coeffs, double[] offsets, double[] multipliers, int degree) {
        this.coeffs = coeffs;
        this.offsets = offsets;
        this.multipliers = multipliers;
        this.degree = degree;
    }

    /**
     * @param coeffs
     *            The coefficients of the polynomial.
     * @param domain
     *            The domain of the function.
     * @param degree
     *            The degree of the polynomial.
     * @return
     */
    public static PolynomialVF of(Vector coeffs, Cell domain, int degree) {
        double[] offsets = new double[domain.getDimensions()];
        double[] multipliers = new double[domain.getDimensions()];
        for (int i = 0; i < domain.getDimensions(); i++) {
            offsets[i] = domain.getInterval(i).getStart();
            multipliers[i] = 2. / domain.getInterval(i).getWidth();
        }
        return new PolynomialVF(coeffs, offsets, multipliers, degree);
    }

    @Override
    public double value(Vector v) {
        double[] point = normalized(v);
        int[] counter = new int[point.length];
        double value = coeffs.get(0);
        for (int i = 1; i < coeffs.getDimensions(); i++) {
            MathUtils.incrementWithSumCap(counter, degree);
            value += coeffs.get(i) * MathUtils.raise(counter, point);
        }
        return value;
    }

    private double[] normalized(Vector v) {
        double[] ret = new double[v.getDimensions()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Math.max(-1, multipliers[i] * (v.get(i) - offsets[i]) - 1);
            ret[i] = Math.min(1, ret[i]);
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PolynomialVF(coeffs = " + coeffs.toString() + " rescale = [");
        for (int i = 0; i < offsets.length; i++) {
            sb.append(String.format("%.2f(x-%.2f),", multipliers[i], offsets[i]));
        }
        sb.append("] degree = " + (degree) + ")");
        return sb.toString();
    }

    @Override
    public double difference(ValueFunction vf) {
        if (!(vf instanceof PolynomialVF)) {
            return Double.POSITIVE_INFINITY;
        }
        return MathUtils.squaredDistance(coeffs, ((PolynomialVF) vf).coeffs);
    }

}

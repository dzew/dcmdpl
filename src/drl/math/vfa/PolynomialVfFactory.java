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

import org.ejml.simple.SimpleMatrix;

/**
 * A Factory for Taylor polynomial function approximations.
 * 
 * @author Dawit
 * 
 */
public class PolynomialVfFactory implements LvfFactory {

    private final Cell domain;
    private final int degree;
    private final Cell preferredDomain;

    /**
     * @param domain
     *            The domain for the function.
     * @param degree
     *            The maximum degree of the polynomial.
     */
    public PolynomialVfFactory(Cell domain, int degree) {
        this.domain = domain;
        this.degree = degree;
        this.preferredDomain = MathUtils.regularCell(domain.getDimensions(), -1, 2);
    }

    @Override
    public PolynomialVF construct(SimpleMatrix coeffVector) {
        double[] coeffs = new double[coeffVector.getNumElements()];
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = coeffVector.get(i);
        }
        return PolynomialVF.of(Vector.asVector(coeffs), domain, degree);
    }

    @Override
    public SimpleMatrix generateBases(Vector[] vectors) {
        double[][] points = new double[vectors.length][];
        for (int v = 0; v < vectors.length; v++) {
            points[v] = generateBases(vectors[v]);
        }
        return new SimpleMatrix(points);
    }

    @Override
    public double[] generateBases(Vector vector) {
        int dimensions = vector.getDimensions();
        double[] ret = new double[MathUtils.taylorExpansionTerms(dimensions, degree)];
        vector = MathUtils.rescale(domain, preferredDomain, vector);
        int[] counter = new int[dimensions];
        ret[0] = 1;
        for (int i = 1; i < ret.length; i++) {
            MathUtils.incrementWithSumCap(counter, degree);
            ret[i] = MathUtils.raise(counter, vector);
        }
        return ret;
    }

}

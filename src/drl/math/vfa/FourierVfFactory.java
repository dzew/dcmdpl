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
 * A factory for FourierVFs
 * 
 * @author Dawit
 * 
 */
public class FourierVfFactory implements LvfFactory {

    private final Cell domain;
    private final int harmonics;
    private final Cell preferredDomain;

    /**
     * 
     * @param domain
     *            The domain of the function.
     * @param terms
     *            The number of harmonics.
     */
    public FourierVfFactory(Cell domain, int terms) {
        this.domain = domain;
        this.harmonics = terms;
        this.preferredDomain = MathUtils.regularCell(domain.getDimensions(), 0, Math.PI);
    }

    @Override
    public FourierVF construct(SimpleMatrix coeffVector) {
        int terms = MathUtils.fourierExpansionTerms(domain.getDimensions(), harmonics);
        double[] cosCoeffs = new double[terms];
        double[] sinCoeffs = new double[terms];
        cosCoeffs[0] = coeffVector.get(0);
        for (int i = 1; i < coeffVector.getNumElements(); i += 2) {
            cosCoeffs[(i + 1) / 2] = coeffVector.get(i);
            sinCoeffs[i / 2 + 1] = coeffVector.get(i + 1);
        }
        return FourierVF.of(Vector.asVector(cosCoeffs),
                Vector.asVector(sinCoeffs),
                domain,
                harmonics);
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
        double[] ret = new double[MathUtils.fourierExpansionTerms(dimensions, harmonics) * 2 - 1];
        vector = MathUtils.rescale(domain, preferredDomain, vector);
        int[] counter = new int[dimensions];
        ret[0] = 1;
        for (int i = 1; i < ret.length; i += 2) {
            MathUtils.increment(counter, harmonics);
            ret[i] = Math.cos(MathUtils.dot(counter, vector));
            ret[i + 1] = Math.sin(MathUtils.dot(counter, vector));
        }
        return ret;
    }
}

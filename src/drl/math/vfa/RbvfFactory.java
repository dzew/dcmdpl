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

import java.util.ArrayList;
import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Vector;

import org.ejml.simple.SimpleMatrix;

/**
 * A Factory for RadialBasisVF.
 * 
 * @author Dawit
 * 
 */
public class RbvfFactory implements LvfFactory {

    private final List<Vector> bases;
    private final double gamma;
    private final Cell domain;
    private final Cell unitCell;

    public RbvfFactory(Cell domain, int pointsPerDimension, double bandwidth) {
        this.gamma = -.5 / (bandwidth * bandwidth);
        this.domain = domain;
        this.unitCell = MathUtils.regularCell(domain.getDimensions(), 0, 1);
        bases = MathUtils.tilingSample(pointsPerDimension, unitCell);
    }

    public RbvfFactory(List<Vector> bases, Cell domain, double bandwidth) {
        this.gamma = -.5 / (bandwidth * bandwidth);
        this.domain = domain;
        this.unitCell = MathUtils.regularCell(domain.getDimensions(), 0, 1);
        this.bases = new ArrayList<Vector>();
        for (Vector v : bases) {
            this.bases.add(MathUtils.rescale(domain, unitCell, v));
        }
    }

    @Override
    public ValueFunction construct(SimpleMatrix coeffVector) {
        double[] coeffs = new double[bases.size()];
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = coeffVector.get(i);
        }
        return new RadialBasisVF(bases, domain, Vector.asVector(coeffs), gamma);
    }

    @Override
    public double[] generateBases(Vector vector) {
        Vector v = MathUtils.rescale(domain, unitCell, vector);
        double[] ret = new double[bases.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Math.exp(gamma * MathUtils.squaredDistance(v, bases.get(i)));
        }
        return ret;
    }

    @Override
    public SimpleMatrix generateBases(Vector[] vectors) {
        double[][] ret = new double[vectors.length][];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = generateBases(vectors[i]);
        }
        return new SimpleMatrix(ret);
    }

}

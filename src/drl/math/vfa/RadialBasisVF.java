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

import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Vector;

/**
 * A ValueFunction represented as the sum of Gaussians over some fixed set of
 * points.
 * 
 * @author Dawit
 * 
 */
public class RadialBasisVF implements ValueFunction {

    final Vector weights;

    private final List<Vector> bases;
    private final double gamma;
    private final Cell domain;
    private final Cell unitCell;

    public RadialBasisVF(List<Vector> bases, Cell domain, Vector weights, double gamma) {
        this.bases = bases;
        this.gamma = gamma;
        this.weights = weights;
        this.domain = domain;
        this.unitCell = MathUtils.regularCell(domain.getDimensions(), 0, 1);
    }

    @Override
    public double value(Vector v) {
        v = MathUtils.rescale(domain, unitCell, v);
        double ret = 0;
        for (int i = 0; i < bases.size(); i++) {
            ret += weights.get(i) * Math.exp(gamma * MathUtils.squaredDistance(v, bases.get(i)));
        }
        return ret;
    }

    @Override
    public String toString() {
        return "RBF(weights = " + weights + " )";
    }

    @Override
    public double difference(ValueFunction vf) {
        if (!(vf instanceof RadialBasisVF)) {
            return Double.POSITIVE_INFINITY;
        }
        return MathUtils.squaredDistance(weights, ((RadialBasisVF) vf).weights);
    }

}

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
import java.util.Set;

import drl.math.geom.Cell;
import drl.math.geom.Vector;

/**
 * A static utility class for linear value function approximations.
 * 
 * @author Dawit
 * 
 */
public class LinearFAs {

    private LinearFAs() {
    }

    public static CosineVfFactory cosineBasisFactory(Cell domain, int harmonics) {
        return new CosineVfFactory(domain, harmonics);
    }

    public static SineVfFactory sineBasisFactory(Cell domain, int harmonics) {
        return new SineVfFactory(domain, harmonics);
    }

    public static FourierVfFactory fourierBasisFactory(Cell domain, int harmonics) {
        return new FourierVfFactory(domain, harmonics);
    }

    public static PolynomialVfFactory polynomialBasisFactory(Cell domain, int degree) {
        return new PolynomialVfFactory(domain, degree);
    }

    public static RbvfFactory gaussianRbfFactory(Cell domain, int pointsPerDimension,
            double bandwidth) {
        return new RbvfFactory(domain, pointsPerDimension, bandwidth);
    }

    public static RbvfFactory gaussianRbvfFactory(Cell domain, List<Vector> bases, double bandwidth) {
        return new RbvfFactory(bases, domain, bandwidth);
    }

    public static ProtoVfFactory pvfFactory(List<Set<Integer>> graph, int numBases) {
        return ProtoVfFactory.of(graph, numBases);
    }

}

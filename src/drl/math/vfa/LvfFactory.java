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

import drl.math.geom.Vector;

import org.ejml.simple.SimpleMatrix;

/**
 * Interface for linear value function approximation factories.
 * 
 * @author Dawit
 * 
 */
public interface LvfFactory {

    /**
     * Create a value function with the given coefficients.
     */
    public ValueFunction construct(SimpleMatrix coeffVector);

    /**
     * Create the bases for a given vector.
     */
    public double[] generateBases(Vector vector);

    /**
     * Create a matrix where the ith row is the bases for the ith vector.
     */
    public SimpleMatrix generateBases(Vector[] vectors);

}

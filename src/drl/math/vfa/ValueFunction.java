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

/**
 * Interface for general scalar-valued functions.
 * 
 * @author Dawit
 * 
 */
public interface ValueFunction {

    /**
     * 
     * @param v
     *            The input vector.
     * @return This function evaluated at v.
     */
    public double value(Vector v);

    /**
     * To see when value-iteration processes converge, there needs to be a
     * measure of distance between two functions. This method provides a way to
     * express that distance.
     * 
     * This method returns zero only if vf.value(x) == this.value(x) everywhere
     * and Double.POSITIVE_INFINITY if vf is incomparable to this.
     * vf.difference(this) must return the same value as this.difference(vf).
     * 
     * For linear value functions, the most natural implementation of this
     * method is by the sum squared difference of the coefficients.
     * 
     * @param vf
     *            A ValueFunction
     * @return A non-negative number representing the difference between this
     *         function and vf.
     */
    public double difference(ValueFunction vf);

}

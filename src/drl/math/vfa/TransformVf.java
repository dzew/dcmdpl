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
import drl.math.tfs.Transform;

/**
 * A value function represented by a bases in a transformed space.
 * 
 * @author Dawit
 * 
 */
public class TransformVf implements ValueFunction {

    private final ValueFunction vf;
    private final Transform tf;

    public TransformVf(ValueFunction vf, Transform tf) {
        this.vf = vf;
        this.tf = tf;
    }

    @Override
    public double difference(ValueFunction other) {
        if (!(other instanceof TransformVf)) {
            return Double.POSITIVE_INFINITY;
        }
        TransformVf tvf = (TransformVf) other;
        if (tvf.tf != this.tf) {
            return Double.POSITIVE_INFINITY;
        }
        return this.vf.difference(tvf.vf);
    }

    @Override
    public double value(Vector v) {
        return vf.value(tf.transform(v));
    }

}
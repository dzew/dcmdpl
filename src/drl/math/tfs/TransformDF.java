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

package drl.math.tfs;

import drl.math.MathUtils;
import drl.math.geom.Vector;

/**
 * The metric that corresponds to Euclidean distance in the transformed space.
 * 
 * @author Dawit
 * 
 */
public class TransformDF implements DistanceFunction {

    private final Transform tf;

    public TransformDF(Transform tf) {
        this.tf = tf;
    }

    @Override
    public double distance(Vector v1, Vector v2) {
        return Math.sqrt(MathUtils.squaredDistance(tf.transform(v1), tf.transform(v2)));
    }

    @Override
    public void memoize(Vector v) {
    }

}

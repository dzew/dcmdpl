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

import drl.math.geom.Vector;

/**
 * A transform from some space to another space, with a potentially different
 * number of dimensions. The transform may be many-to-one.
 * 
 * @author Dawit
 * 
 */
public interface Transform {

    /**
     * @param v
     * @return Phi(x)
     */
    public Vector transform(Vector v);

}

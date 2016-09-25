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

package drl.math.algs;

import java.util.Collection;
import java.util.List;

import drl.math.geom.Vector;

/**
 * This interface describes a data structure for performing a coverage filter on
 * a collection of Vectors.
 * 
 * @author Dawit
 * 
 */
public interface Filter {

    /**
     * Adds the specified element to this data structure.
     * 
     * @param v
     *            The element to be added.
     */
    public void add(Vector v);

    /**
     * Add all the vectors to this data structure.
     * 
     * @param v
     *            The element to be added.
     */
    public void addAll(Collection<Vector> v);

    /**
     * Returns a subset of the Vectors in this data structure satisfying the
     * property that the maximum distance between a vector in this data
     * structure and its closest neighbor in the subset is minimized.
     * 
     * @param samples
     *            The desired size of the subset.
     */
    public List<Vector> subsample(int samples);

    /**
     * Returns all vectors in this data structure that are within distance
     * epsilon of v.
     * 
     * @param v
     * @param epsilon
     */
    public Collection<Vector> getNeighbors(Vector v, double epsilon);

}

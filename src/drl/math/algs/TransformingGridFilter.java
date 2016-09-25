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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Vector;
import drl.math.tfs.Normalizer;
import drl.math.tfs.Transform;

/**
 * A wrapper around GridFilter which filters in a transformed space.
 * 
 * @author Dawit
 * 
 */
public class TransformingGridFilter implements Filter {

    private final GridFilter filter;
    private final Map<Vector, Vector> map;
    private final Transform tf;

    /**
     * 
     * @param tf
     *            The transform.
     * @param image
     *            The image of the transform.
     * @param cellsPerDimension
     *            Parameter for GridFilter.
     */
    public TransformingGridFilter(Transform tf, Cell image, int cellsPerDimension) {
        this.filter = new GridFilter(MathUtils.unitCell(image.getDimensions()), cellsPerDimension);
        this.map = new HashMap<Vector, Vector>();
        this.tf = tf;
    }

    /**
     * A static constructor that normalizes the domain.
     * 
     * @param domain
     * @param cellsPerDimension
     * @return
     */
    public static TransformingGridFilter normalized(Cell domain, int cellsPerDimension) {
        return new TransformingGridFilter(new Normalizer(domain),
                MathUtils.unitCell(domain.getDimensions()),
                cellsPerDimension);
    }

    /**
     * @return The number on non-empty bins in the underlying GridFilter.
     */
    public int binsReached() {
        return filter.binsReached();
    }

    @Override
    public void add(Vector v) {
        Vector v2 = tf.transform(v);
        map.put(v2, v);
        filter.add(v2);
    }

    @Override
    public void addAll(Collection<Vector> vs) {
        for (Vector v : vs) {
            add(v);
        }
    }

    @Override
    public Collection<Vector> getNeighbors(Vector v, double epsilon) {
        List<Vector> vs = filter.getNeighbors(v, epsilon);
        List<Vector> ret = new ArrayList<Vector>();
        for (Vector vec : vs) {
            ret.add(map.get(vec));
        }
        return ret;
    }

    @Override
    public List<Vector> subsample(int samples) {
        List<Vector> vs = filter.subsample(samples);
        List<Vector> ret = new ArrayList<Vector>();
        for (Vector vec : vs) {
            ret.add(map.get(vec));
        }
        return ret;
    }

}

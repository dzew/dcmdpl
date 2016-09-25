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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;

/**
 * An implementation of the {@code Filter} interface that bins the data. This
 * class is performant (all operations are linear or sublinear in time and
 * space). However, it only works well when the bin granularity is such that
 * roughly one vector is selected per bin.
 * 
 * @author Dawit
 * 
 */
public class GridFilter implements Filter {

    private final Map<Integer, List<Vector>> table;
    private final int dimensions;
    private final int cellsPerDim;
    private final double width;
    private final Cell domain;
    private int size = 0;

    public GridFilter(Cell domain, int cellsPerDimension) {
        this.dimensions = domain.getDimensions();
        this.cellsPerDim = cellsPerDimension;
        this.domain = domain;
        table = new HashMap<Integer, List<Vector>>();
        double w = Double.POSITIVE_INFINITY;
        for (int i = 0; i < domain.getDimensions(); i++) {
            w = Math.min(w, domain.getInterval(i).getWidth());
        }
        this.width = w / cellsPerDimension;
    }

    /**
     * This method is only visible for testing. Returns the index of the bin in
     * which the vector will be placed.
     * 
     */
    public final int getIndex(Vector v) {
        int ret = 0;
        for (int i = 0; i < dimensions; i++) {
            ret = ret * cellsPerDim;
            int val = (int) ((v.get(i) - domain.getInterval(i).getStart()) * cellsPerDim / domain.getInterval(i)
                    .getWidth());
            val = Math.max(val, 0);
            val = Math.min(val, cellsPerDim - 1);
            ret += val;
        }
        return ret;
    }

    /**
     * @return The number of non-empty bins in the GridFilter.
     */
    public int binsReached() {
        return table.size();
    }

    @Override
    public void addAll(Collection<Vector> vs) {
        for (Vector v : vs) {
            add(v);
        }
    }

    @Override
    public void add(Vector v) {
        int index = getIndex(v);
        if (!table.containsKey(index)) {
            table.put(index, new ArrayList<Vector>());
        }
        table.get(index).add(v);
        size++;
    }

    /**
     * @return A histogram showing how many vectors are in each bin.
     */
    public Map<Integer, Integer> histogram() {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (List<Vector> list : table.values()) {
            int len = list.size();
            if (!ret.containsKey(len)) {
                ret.put(len, 0);
            }
            ret.put(len, ret.get(len) + 1);
        }
        return ret;
    }

    @Override
    public List<Vector> subsample(int numItems) {
        if (numItems <= 0) {
            throw new IllegalArgumentException("Cannot sample a negaive number of points "
                    + numItems);
        }
        if (numItems > size) {
            throw new IllegalArgumentException("Requested more points than available " + numItems
                    + " vs " + size);
        }
        if (numItems == size) {
            return allPoints();
        }
        if (numItems <= binsReached()) {
            return binCenters(numItems);
        }
        return intermediateSample(numItems);
    }

    /**
     * 
     * @return The vectors closest to the middle of each non-empty bin, plus
     *         some additional vectors to satisfy the size requirement.
     */
    private List<Vector> intermediateSample(int numItems) {
        ArrayList<Integer> binSizes = new ArrayList<Integer>(table.size());
        binSizes.add(0);
        for (List<Vector> vs : table.values()) {
            binSizes.add(vs.size());
        }
        Collections.sort(binSizes);
        int points = 0, i = 0, sub = 0;
        while (points < numItems) {
            sub = binSizes.get(i++);
            points += (binSizes.get(i) - sub) * (binSizes.size() - i);
        }
        List<Vector> ret = new ArrayList<Vector>();
        List<List<Vector>> temps = new ArrayList<List<Vector>>(table.size() - i + 2);
        for (List<Vector> vs : table.values()) {
            if (vs.size() <= sub) {
                ret.addAll(vs);
                continue;
            }
            Collections.shuffle(vs);
            Vector v = closest(vs, getBinCenter(getIndex(vs.get(0))));
            int index = vs.indexOf(v);
            vs.set(index, vs.get(0));
            vs.set(0, v);
            for (int j = 0; j < sub; j++) {
                ret.add(vs.get(j));
            }
            temps.add(vs);
        }
        Collections.shuffle(temps);
        i = 0;
        for (; ret.size() < numItems; i++) {
            if (i == temps.size()) {
                i = 0;
                sub++;
            }
            if (temps.get(i).size() <= sub) {
                temps.remove(i--);
                continue;
            }
            ret.add(temps.get(i).get(sub));
        }
        return ret;
    }

    private List<Vector> binCenters(int numItems) {
        List<Vector> ret = new ArrayList<Vector>(numItems);
        int bins = binsReached();
        for (int i : table.keySet()) {
            if (Math.random() * bins-- > numItems) {
                continue;
            }
            numItems--;
            ret.add(closest(table.get(i), getBinCenter(i)));

        }
        return ret;
    }

    private Vector closest(List<Vector> list, Vector vector) {
        double min = Double.POSITIVE_INFINITY;
        Vector closest = null;
        for (Vector v : list) {
            double dist = MathUtils.squaredDistance(vector, v);
            if (dist <= min) {
                min = dist;
                closest = v;
            }
        }
        return closest;
    }

    public Vector getBinCenter(int bin) {
        double[] ret = new double[domain.getDimensions()];
        for (int i = ret.length - 1; i >= 0; i--) {
            Interval itv = domain.getInterval(i);
            ret[i] = itv.getStart() + itv.getWidth() * ((bin % cellsPerDim) + .5) / cellsPerDim;
            bin /= cellsPerDim;
        }
        return Vector.asVector(ret);
    }

    private List<Vector> allPoints() {
        List<Vector> ret = new ArrayList<Vector>(size);
        for (List<Vector> vs : table.values()) {
            ret.addAll(vs);
        }
        return ret;
    }

    @Override
    public List<Vector> getNeighbors(Vector v, double epsilon) {
        List<Vector> ret = new ArrayList<Vector>();
        double eps2 = epsilon * epsilon;
        if (epsilon >= width) {
            for (List<Vector> vs : table.values()) {
                for (Vector v2 : vs) {
                    if (MathUtils.squaredDistance(v, v2) < eps2) {
                        ret.add(v2);
                    }
                }
            }
            return ret;
        }
        Set<Integer> indicies = getAdjacent(v, epsilon);
        for (Integer i : indicies) {
            if (!table.containsKey(i)) {
                continue;
            }
            for (Vector v2 : table.get(i)) {
                if (MathUtils.squaredDistance(v, v2) < eps2) {
                    ret.add(v2);
                }
            }
        }
        return ret;
    }

    private Set<Integer> getAdjacent(Vector v, double epsilon) {
        Set<Integer> indicies = new HashSet<Integer>();
        double[] d = new double[v.getDimensions()];
        for (int i = 0; i < d.length; i++) {
            d[i] = v.get(i) - epsilon;
        }
        int points = MathUtils.raise(3, domain.getDimensions());
        int[] counter = new int[v.getDimensions()];
        for (int j = 0; j <= points; j++) {
            for (int i = 0; i < counter.length; i++) {
                counter[i] += 1;
                d[i] += epsilon;
                if (counter[i] < 3) {
                    Vector vx = Vector.asVector(d);
                    if (domain.contains(vx)) {
                        indicies.add(getIndex(vx));
                    }
                    break;
                }
                counter[i] = 0;
                d[i] -= 3 * epsilon;
            }
        }
        return indicies;
    }
}

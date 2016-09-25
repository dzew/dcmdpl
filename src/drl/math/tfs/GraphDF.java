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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import drl.math.MathUtils;
import drl.math.algs.APSP;
import drl.math.algs.Filter;
import drl.math.algs.GraphBuilder;
import drl.math.algs.GridFilter;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;

/**
 * A metric that infers distances using a graph embedding of the space. This is
 * code from an early attempt to make a graph-theoretic representation discovery
 * algorithm for KBRL. This class has been deprecated and is only kept for
 * posterity.
 * 
 * @author Dawit
 * 
 */
@Deprecated
public class GraphDF implements DistanceFunction {

    private final double[][] distances;
    private final List<Vector> vecs;
    private final double epsilon;
    private final Map<Vector, List<Integer>> cache;
    private final Map<Vector, Integer> bases;
    private final Filter filter;

    private GraphDF(double[][] distances, List<Vector> vecs, Filter filter, double epsilon) {
        this.distances = distances;
        this.vecs = vecs;
        this.epsilon = epsilon;
        this.cache = new Hashtable<Vector, List<Integer>>();
        this.bases = new Hashtable<Vector, Integer>();
        for (int i = 0; i < vecs.size(); i++) {
            bases.put(vecs.get(i), i);
        }
        this.filter = filter;
    }

    public static GraphDF of(List<Vector> vecs, double[][] apsps, double epsilon) {
        double[] mins = new double[vecs.get(0).getDimensions()];
        double[] maxs = new double[vecs.get(0).getDimensions()];
        for (int i = 0; i < mins.length; i++) {
            mins[i] = Double.POSITIVE_INFINITY;
            maxs[i] = Double.NEGATIVE_INFINITY;
        }
        for (Vector v : vecs) {
            for (int i = 0; i < v.getDimensions(); i++) {
                mins[i] = Math.min(mins[i], v.get(i));
                maxs[i] = Math.max(maxs[i], v.get(i));
            }
        }
        Interval[] is = new Interval[mins.length];
        for (int i = 0; i < is.length; i++) {
            is[i] = new Interval(mins[i] - epsilon, maxs[i] - mins[i] + 2 * epsilon);
        }
        Cell domain = Cell.of(is);
        double w = Double.POSITIVE_INFINITY;
        for (int i = 0; i < domain.getDimensions(); i++) {
            w = Math.min(w, domain.getInterval(i).getWidth());
        }
        int cellsPerDim = Math.min((int) (w / epsilon),
                (int) (Math.log(vecs.size() / 4.) / Math.log(is.length)));
        GridFilter filter = new GridFilter(domain, cellsPerDim);
        filter.addAll(vecs);
        return new GraphDF(apsps, vecs, filter, epsilon);
    }

    public static GraphDF of(List<Vector> vecs, List<Set<Integer>> graph, double epsilon) {
        double[][] apsps = APSP.floydWarshall(vecs, graph);
        int degs = 0;
        for (Set<Integer> nbrhd : graph) {
            degs += nbrhd.size();
        }
        System.out.println(degs / graph.size() + " " + graph);
        return GraphDF.of(vecs, apsps, epsilon);
    }

    public static GraphDF of(List<Vector> vecs, double epsilon) {
        return GraphDF.of(vecs, GraphBuilder.buildConnectedGraph(vecs, epsilon), epsilon);
    }

    @Override
    public void memoize(Vector v) {
        if (!cache.containsKey(v) && !bases.containsKey(v)) {
            cache.put(v, getNeighbors(v));
        }
    }

    private List<Integer> getNeighbors(Vector a) {
        if (bases.containsKey(a)) {
            return Collections.singletonList(bases.get(a));
        }
        List<Integer> nbrs = new ArrayList<Integer>();
        for (Vector v : filter.getNeighbors(a, epsilon)) {
            nbrs.add(bases.get(v));
        }
        if (nbrs.size() > 0) {
            return nbrs;
        }
        int closea = -1;
        double dista = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vecs.size(); i++) {
            Vector v = vecs.get(i);
            double d = MathUtils.squaredDistance(v, a);
            if (d <= dista) {
                dista = d;
                closea = i;
            }
        }
        return Collections.singletonList(closea);
    }

    @Override
    public double distance(Vector a, Vector b) {
        double abdist = Math.sqrt(MathUtils.squaredDistance(a, b));
        if (abdist < 2 * epsilon) {
            return abdist;
        }
        List<Integer> closea = cache.containsKey(a) ? cache.get(a) : getNeighbors(a);
        List<Integer> closeb = cache.containsKey(b) ? cache.get(b) : getNeighbors(b);
        double dist = Double.POSITIVE_INFINITY;
        double minda = 0;
        for (int i : closea) {
            double dista = Math.sqrt(MathUtils.squaredDistance(a, vecs.get(i)));
            for (int j : closeb) {
                double distb = Math.sqrt(MathUtils.squaredDistance(b, vecs.get(j)));
                if (dista + distances[i][j] + distb < dist) {
                    minda = dista + distb;
                    dist = minda + distances[i][j];

                }
            }
        }
        if (abdist < 2 * minda) {
            return abdist;
        }
        return dist;
    }

}

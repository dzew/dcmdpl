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

import java.util.List;
import java.util.Set;

import drl.math.MathUtils;
import drl.math.geom.Vector;

/**
 * A static utility class for calculating shortest paths in graphs.
 * 
 * @author Dawit
 * 
 */
public class APSP {

    /**
     * @param vectors
     *            A list of Vectors.
     * @param graph
     *            An adjacency list representation of a graph.
     * @return The all-pairs shortest-paths matrix.
     */
    public static double[][] floydWarshall(List<Vector> vectors, List<Set<Integer>> graph) {
        double[][] ret = new double[vectors.size()][vectors.size()];
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < vectors.size(); j++) {
                if (i != j) {
                    ret[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
        for (int i = 0; i < vectors.size(); i++) {
            Set<Integer> nbrs = graph.get(i);
            for (int j : nbrs) {
                ret[i][j] = Math.sqrt(MathUtils.squaredDistance(vectors.get(i), vectors.get(j)));
            }
        }
        floydWarshall(ret);
        return ret;
    }

    /**
     * Performs Floyd-Warshall on the given matric of graph distances. Modifies
     * the matrix in place.
     */
    public static void floydWarshall(double[][] ret) {
        for (int k = 0; k < ret.length; k++) {
            for (int j = 0; j < ret.length; j++) {
                for (int i = 0; i < ret.length; i++) {
                    ret[i][j] = Math.min(ret[i][j], ret[i][k] + ret[k][j]);
                }
            }
        }
        for (double[] ds : ret) {
            for (double d : ds)
                if (Double.isInfinite(d)) {
                    System.err.println("Graph is not connected!");
                    return;
                }
        }
    }

}

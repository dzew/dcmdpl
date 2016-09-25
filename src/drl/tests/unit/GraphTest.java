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

package drl.tests.unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import drl.math.algs.APSP;
import drl.math.geom.Vector;
import drl.math.tfs.EuclideanDF;

public class GraphTest {

    @Test
    public void testFW() {
        List<Vector> vectors = new ArrayList<Vector>();
        vectors.add(Vector.asVector(1, 1));
        vectors.add(Vector.asVector(1, 0));
        vectors.add(Vector.asVector(2, 4));
        vectors.add(Vector.asVector(0, 3));
        vectors.add(Vector.asVector(3, 5));
        vectors.add(Vector.asVector(2, 2));
        vectors.add(Vector.asVector(5, 0));

        List<Set<Integer>> graph = new ArrayList<Set<Integer>>();
        graph.add(setof(1, 5, 3));
        graph.add(setof(0, 5));
        graph.add(setof(6));
        graph.add(setof(0, 4));
        graph.add(setof(3));
        graph.add(setof(0, 1, 6));
        graph.add(setof(2, 5));

        double[][] dists = APSP.floydWarshall(vectors, graph);

        for (double[] ds : dists) {
            System.out.println(Vector.asVector(ds));
        }

        for (int i = 0; i < graph.size(); i++) {
            Vector vi = vectors.get(i);
            for (int j : graph.get(i)) {
                Vector vj = vectors.get(j);
                double dist = EuclideanDF.instance.distance(vi, vj);
                assertTrue(Math.abs(dists[i][j] - dist) < .00000001);
                assertTrue(Math.abs(dists[j][i] - dist) < .00000001);
            }
        }

        for (int i = 0; i < vectors.size(); i++) {
            Vector vi = vectors.get(i);
            for (int j = 0; j < vectors.size(); j++) {
                Vector vj = vectors.get(j);
                assertTrue(EuclideanDF.instance.distance(vi, vj) <= dists[i][j]);
                for (int k = 0; k < vectors.size(); k++) {
                    assertTrue(dists[i][j] <= dists[i][k] + dists[k][j]);
                }
            }
        }
    }

    private static Set<Integer> setof(int... is) {
        Set<Integer> ret = new HashSet<Integer>();
        for (int i : is) {
            ret.add(i);
        }
        return (ret);
    }

}

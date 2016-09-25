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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import drl.math.MathUtils;
import drl.math.geom.Vector;

/**
 * A static utility class for creating nearest neighbor graphs. It is deprecated
 * and untested. Use with caution.
 * 
 * @author Dawit
 * 
 */
public class GraphBuilder {

    public static List<Set<Integer>> buildConnectedGraph(List<Vector> vecs, double eps) {
        List<Set<Integer>> ret = new ArrayList<Set<Integer>>();
        double epsSq = eps * eps;
        for (int i = 0; i < vecs.size(); i++) {
            Vector v = vecs.get(i);
            Set<Integer> nbrs = new HashSet<Integer>();
            for (int j = i + 1; j < vecs.size(); j++) {
                double dist = MathUtils.squaredDistance(v, vecs.get(j));
                if (dist <= epsSq) {
                    nbrs.add(j);
                }
            }
            ret.add(nbrs);
        }
        System.out.println("Computed adjacency");
        for (int i = vecs.size() - 1; i >= 0; i--) {
            for (int j : ret.get(i)) {
                ret.get(j).add(i);
            }
        }

        List<Set<Integer>> comps = connectedComponents(ret);
        if (comps.size() == 1) {
            return ret;
        }
        System.out.println(comps.size() + " connected components found. Trying to fix that");
        int largestIndex = 0;
        Set<Integer> largest = comps.get(0);
        System.out.print(largest.size() + " ");
        for (int i = 1; i < comps.size(); i++) {
            System.out.print(comps.get(i).size() + " ");
            if (comps.get(i).size() > largest.size()) {
                largest = comps.get(i);
                largestIndex = i;
            }
        }
        System.out.println();
        for (int i = 0; i < comps.size(); i++) {
            if (i != largestIndex) {
                connectClosest(comps.get(i), largest, vecs, ret);
                largest.addAll(comps.get(i));
            }
        }
        System.out.println("Computed graph.");
        return ret;
    }

    private static void connectClosest(Set<Integer> comp1, Set<Integer> comp2, List<Vector> vecs,
            List<Set<Integer>> graph) {
        double min = Double.POSITIVE_INFINITY;
        int v1 = -1;
        int v2 = -1;
        for (int i : comp1) {
            for (int j : comp2) {
                double dist = MathUtils.squaredDistance(vecs.get(i), vecs.get(j));
                if (dist < min) {
                    v1 = i;
                    v2 = j;
                    min = dist;
                }
            }
        }
        graph.get(v1).add(v2);
        graph.get(v2).add(v1);
    }

    private static List<Set<Integer>> connectedComponents(List<Set<Integer>> graph) {
        Set<Integer> done = new HashSet<Integer>();
        List<Set<Integer>> ret = new ArrayList<Set<Integer>>();
        for (int i = 0; i < graph.size(); i++) {
            if (!done.contains(i)) {
                Set<Integer> nbrs = search(graph, i);
                done.addAll(nbrs);
                ret.add(nbrs);
            }
        }
        return ret;
    }

    private static Set<Integer> search(List<Set<Integer>> graph, int start) {
        Set<Integer> done = new HashSet<Integer>();
        List<Integer> front = new ArrayList<Integer>();
        front.add(start);
        done.add(start);
        while (front.size() > 0) {
            Integer i = front.remove(front.size() - 1);
            for (Integer j : graph.get(i)) {
                if (!done.contains(j)) {
                    done.add(j);
                    front.add(j);
                }
            }
        }
        return done;
    }

}

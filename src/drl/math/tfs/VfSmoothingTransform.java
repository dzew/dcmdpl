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

import java.util.List;
import java.util.Set;

import drl.math.algs.APSP;
import drl.math.geom.Vector;
import drl.math.vfa.ValueFunction;

/**
 * A graph-theoretic approach to creating a Wrinkle-Ironing Transform.
 * 
 * @author Dawit
 * 
 */
@Deprecated
public class VfSmoothingTransform {

    private VfSmoothingTransform() {
    }

    public static double[][] transformedApsps(List<Vector> vecs, List<Set<Integer>> graph,
            ValueFunction vf, double trueMaxDist) {
        double[] qvals = new double[vecs.size()];
        for (int i = 0; i < qvals.length; i++) {
            qvals[i] = vf.value(vecs.get(i));
        }

        double slopeSum = 0.;
        int pairs = 0;
        for (int i = 0; i < qvals.length; i++) {
            Vector vi = vecs.get(i);
            for (int j : graph.get(i)) {
                double dist = EuclideanDF.instance.distance(vi, vecs.get(j));
                slopeSum += Math.abs(qvals[i] - qvals[j]) / dist;
                pairs++;
            }
        }
        slopeSum /= pairs;
        System.out.println("Average slope " + slopeSum);

        double[][] dists = new double[qvals.length][qvals.length];
        for (int i = 0; i < dists.length; i++) {
            for (int j = 0; j < dists.length; j++) {
                dists[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        for (int i = 0; i < qvals.length; i++) {
            dists[i][i] = 0.;
            Vector vi = vecs.get(i);
            for (int j : graph.get(i)) {
                double dist = EuclideanDF.instance.distance(vi, vecs.get(j));
                dists[i][j] = slopeSum * dist + Math.abs(qvals[j] - qvals[i]);
                dists[j][i] = dists[i][j];
            }
        }
        APSP.floydWarshall(dists);
        double maxDist = 0;
        for (double ds[] : dists) {
            for (double d : ds) {
                maxDist = Math.max(maxDist, d);
            }
        }
        System.out.println("Max dist: " + maxDist);
        for (int i = 0; i < dists.length; i++) {
            for (int j = 0; j < dists.length; j++) {
                dists[i][j] *= trueMaxDist / maxDist;
            }
        }
        return dists;
    }

}

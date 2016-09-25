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

package drl.tests.functional;

import java.util.ArrayList;
import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.EuclideanDF;
import drl.math.tfs.MappingTF;
import drl.math.tfs.Transform;

public class MappingTfTest {

    public static void main(String[] args) {
        Cell domain = Cell.of(new Interval(0, 1), new Interval(0, 1));
        List<Vector> vecs = new ArrayList<Vector>(20);
        int n = 150;
        for (int i = 0; i < n; i++) {
            vecs.add(MathUtils.sampleUniformly(domain));
        }
        System.out.println("Initial vectors: " + vecs);
        System.out.println();
        Transform tf = MappingTF.deduceTransform(vecs, d_tr, 4);
        System.out.println();
        System.out.println("The result: ");
        for (Vector v : vecs) {
            System.out.print(tf.transform(v) + ",");
        }

        double maxErr = 0;
        double sumErr = 0;
        int items = 0;

        for (int i = 0; i < vecs.size(); i++) {
            Vector vi = vecs.get(i);
            Vector tvi = tf.transform(vecs.get(i));
            for (int j = i; j < vecs.size(); j++) {
                Vector vj = vecs.get(j);
                Vector tvj = tf.transform(vecs.get(j));
                double error = Math.abs(d_tr.distance(vi, vj)
                        - EuclideanDF.instance.distance(tvi, tvj));
                maxErr = Math.max(maxErr, error);
                sumErr += error;
                items++;
            }
        }
        System.out.println();
        System.out.println("Maximum error: " + maxErr);
        System.out.println("Average error: " + sumErr / (n * (n + 1) / 2.0));
    }

    private static final Vector pivot = Vector.asVector(.8, .5);

    /**
     * The distance function that corresponds to shortest paths in TWO-ROOM
     */
    private static final DistanceFunction d_tr = new DistanceFunction() {

        @Override
        public void memoize(Vector v) {
        }

        @Override
        public double distance(Vector v1, Vector v2) {
            double x1 = v1.get(0), y1 = v1.get(1);
            double x2 = v2.get(0), y2 = v2.get(1);
            if ((y2 - .5) * (y1 - .5) > 0) {
                return EuclideanDF.instance.distance(v1, v2);
            }
            if (x1 + (x2 - x1) * (y1 - .5) / (y1 - y2) > .8) {
                return EuclideanDF.instance.distance(v1, v2);
            }
            return EuclideanDF.instance.distance(pivot, v2)
                    + EuclideanDF.instance.distance(v1, pivot);
        }
    };

}

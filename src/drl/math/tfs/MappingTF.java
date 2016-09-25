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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import drl.math.MathUtils;
import drl.math.geom.Vector;

import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

/**
 * A class for inferring a transform given a set of vectors and their distances
 * in the transformed space.
 * 
 * @author Dawit
 * 
 */
public class MappingTF implements Transform {

    private final Map<Vector, Vector> vectorMap;

    private MappingTF(Map<Vector, Vector> vectorMap) {
        this.vectorMap = vectorMap;
    }

    /**
     * Uses MDS (Kruskal, 1964) to infer a transform.
     * 
     * @param vecs
     *            A List of sample Vectors.
     * @param df
     *            A metric for the distances in the transformed space.
     * @param dims
     *            The desired dimensionality of the Transform.
     * @return
     */
    public static MappingTF deduceTransform(List<Vector> vecs, DistanceFunction df, int dims) {
        double[][] dists = makeSqDistMatrix(vecs, df);
        double[] rowSums = new double[vecs.size()];
        for (int i = 0; i < vecs.size(); i++) {
            rowSums[i] = MathUtils.sum(dists[i]) / vecs.size();
        }
        double sum = MathUtils.sum(rowSums) / vecs.size();
        for (double[] d : dists) {
            Vector.asVector(d);
        }
        for (int i = 0; i < vecs.size(); i++) {
            for (int j = 0; j < vecs.size(); j++) {
                dists[i][j] = -(dists[i][j] - rowSums[i] - rowSums[j] + sum) / 2;
            }
        }
        SimpleMatrix kernel = new SimpleMatrix(dists);
        @SuppressWarnings("unchecked")
        SimpleEVD<SimpleMatrix> evd = kernel.eig();
        System.out.print("Top 15 eigenvalues:");
        for (int i = 0; i < 15; i++) {
            System.out.print(String.format("%.4f ", evd.getEigenvalue(i).getMagnitude()));
        }
        System.out.println();
        double[][] newVecs = new double[vecs.size()][dims];
        for (int i = 0; i < dims; i++) {
            SimpleMatrix evec = evd.getEigenVector(i);
            double eval = Math.sqrt(evd.getEigenvalue(i).getMagnitude());
            for (int j = 0; j < vecs.size(); j++) {
                newVecs[j][i] = eval * evec.get(j);
            }
        }
        Map<Vector, Vector> map = new Hashtable<Vector, Vector>();
        for (int i = 0; i < vecs.size(); i++) {
            Vector v = Vector.asVector(newVecs[i]);
            System.out.print(v + ", ");
            map.put(vecs.get(i), v);
        }
        System.out.println();
        return new MappingTF(map);
    }

    private static double[][] makeSqDistMatrix(List<Vector> vecs, DistanceFunction df) {
        double[][] dists = new double[vecs.size()][vecs.size()];
        for (int i = 0; i < vecs.size(); i++) {
            Vector vi = vecs.get(i);
            for (int j = i + 1; j < vecs.size(); j++) {
                double d = df.distance(vi, vecs.get(j));
                dists[i][j] = d * d;
                dists[j][i] = dists[i][j];
            }
        }
        return dists;
    }

    @Override
    public Vector transform(Vector v) {
        if (vectorMap.containsKey(v)) {
            return vectorMap.get(v);
        }
        // TODO implement out-of-sample extensions.
        throw new UnsupportedOperationException("Out-of-sample extensions have not been implemented.");
    }
}

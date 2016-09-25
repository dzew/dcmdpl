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

package drl.math.vfa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import drl.math.MathUtils;
import drl.math.geom.Vector;

import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

/**
 * A factory for Proto-Value Functions (Mahadevan and Maggioni 2007). This
 * implementation only handles discrete case and does not perform the
 * extrapolation to continuous space.
 * 
 * This code is experimental and is largely untested. It is also not completely
 * finished. Use with care.
 * 
 * @author Dawit
 * 
 */
public class ProtoVfFactory implements LvfFactory {

    private final List<SimpleMatrix> bases;

    private ProtoVfFactory(List<SimpleMatrix> bases) {
        this.bases = bases;
    }

    /**
     * @return The eigenfunctions being used as a basis.
     */
    public SimpleMatrix getBases() {
        double[][] ret = new double[bases.get(0).getNumElements()][bases.size()];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < bases.size(); j++) {
                ret[i][j] = bases.get(j).get(i);
            }
        }
        return new SimpleMatrix(ret);
    }

    /**
     * Static constructor.
     * 
     * @param states
     *            An adjacency list representation of the graph.
     * @param numBases
     *            The number of eigenfunctions to use as a basis.
     * @return
     */
    public static ProtoVfFactory of(List<Set<Integer>> states, int numBases) {
        double[][] lap = new double[states.size()][states.size()];
        for (int i = 0; i < states.size(); i++) {
            Set<Integer> ends = states.get(i);
            lap[i][i] = ends.size();
            for (int j : ends) {
                lap[i][j]--;
            }
        }
        SimpleMatrix laplacian = new SimpleMatrix(lap);

        @SuppressWarnings("unchecked")
        SimpleEVD<SimpleMatrix> evd = laplacian.eig();

        PriorityQueue<EigenPair> heap = new PriorityQueue<EigenPair>(evd.getNumberOfEigenvalues(),
                eigenComparator);
        for (int i = 0; i < evd.getNumberOfEigenvalues(); i++) {
            heap.add(new EigenPair(evd.getEigenvalue(i).getMagnitude(), evd.getEigenVector(i)));
        }
        List<SimpleMatrix> bases = new ArrayList<SimpleMatrix>();
        for (int i = 0; i < numBases; i++) {
            bases.add(heap.poll().vector);
        }
        return new ProtoVfFactory(bases);
    }

    private static class EigenPair {
        private final double value;
        private final SimpleMatrix vector;

        public EigenPair(double value, SimpleMatrix vector) {
            this.value = value;
            this.vector = vector;
        }
    }

    private static final Comparator<EigenPair> eigenComparator = new Comparator<EigenPair>() {
        @Override
        public int compare(EigenPair a, EigenPair b) {
            if (a.value > b.value) {
                return 1;
            }
            if (a.value < b.value) {
                return -1;
            }
            return 0;
        }
    };

    private static class FiniteVf implements ValueFunction {

        private final double[] values;

        private FiniteVf(double[] values) {
            this.values = values;
        }

        @Override
        public double difference(ValueFunction vf) {
            if (!(vf instanceof FiniteVf)) {
                return Double.POSITIVE_INFINITY;
            }
            FiniteVf other = (FiniteVf) vf;
            return MathUtils.squaredDistance(values, other.values);
        }

        @Override
        public double value(Vector v) {
            return values[(int) v.get(0)];
        }

    }

    @Override
    public ValueFunction construct(SimpleMatrix coeffVector) {
        double[] values = new double[bases.get(0).getNumElements()];
        for (int j = 0; j < coeffVector.getNumElements(); j++) {
            SimpleMatrix base = bases.get(j);
            for (int i = 0; i < values.length; i++) {
                values[i] += base.get(i) * coeffVector.get(j);
            }
        }
        return new FiniteVf(values);
    }

    @Override
    public double[] generateBases(Vector vector) {
        int index = (int) vector.get(0);
        double[] ret = new double[bases.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bases.get(i).get(index);
        }
        return ret;
    }

    @Override
    public SimpleMatrix generateBases(Vector[] vectors) {
        double[][] ret = new double[vectors.length][];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = generateBases(vectors[i]);
        }
        return new SimpleMatrix(ret);
    }

}

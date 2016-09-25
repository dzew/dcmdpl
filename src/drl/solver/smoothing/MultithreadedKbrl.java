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

package drl.solver.smoothing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import drl.math.MathUtils;
import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.Transition;

import org.ejml.simple.SimpleMatrix;

/**
 * Static utility class containing multithreaded implementations of kernel based
 * reinforcement learning and kernel based stochastic factorization.
 * 
 * @author Dawit
 * 
 */
public class MultithreadedKbrl {

    private MultithreadedKbrl() {
    }

    /**
     * Solves an MDP using KBRL.
     * 
     * @param qval
     *            Initial value for the Q-values when starting value iteration.
     * @param mdp
     *            The MDP to be solved.
     * @param samples
     *            A set of sample transitions.
     * @param exec
     *            An executor service.
     * @param threads
     *            The maximum number of threads to use.
     * @param steps
     *            The number of rounds of value iteration to perform.
     * @return The Q-values with the given setting of parameters. The bandwidth
     *         and metric are both read from {@code qval}.
     */
    public static <S extends State, A extends Action> KernelQValue<S, A> solve(
            KernelQValue<S, A> qval, MDP<S, A> mdp, SampleTransitions<S, A> samples,
            ExecutorService exec, int threads, int steps) {
        System.out.println("Begin multithreaded KBRL computation");
        A[] actions = mdp.getActions();
        double[][] news = new double[actions.length][samples.get(actions[0]).size()];
        for (int trial = 0; trial < steps; trial++) {
            List<Future<double[][]>> futureVals = new ArrayList<Future<double[][]>>();
            for (int thread = 0; thread < threads; thread++) {
                futureVals.add(exec.submit(KbUtils.ValueIterator.of(thread,
                        threads,
                        samples,
                        qval,
                        mdp)));
            }
            try {
                for (int i = 0; i < threads; i++) {
                    int start = i * news[0].length / threads;
                    double[][] parts = futureVals.get(i).get();
                    for (int a = 0; a < parts.length; a++) {
                        for (int s = 0; s < parts[a].length; s++) {
                            news[a][s + start] = parts[a][s];
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            KernelQValue<S, A> vf2 = qval.update(news);
            double diff = qval.difference(vf2);
            System.out.println("#" + diff + " on iteration" + trial);
            qval = vf2;
            if (diff < .0001) {
                System.out.println("Converged in " + trial + " rounds of value iteration.");
                break;
            }
        }
        return qval;
    }

    private static class RowComputer implements Callable<SubMatrix> {
        private final Vector[] ys;
        private final Vector[] rxs;
        private final DistanceFunction df;
        private final double bandwidth;
        private final int start;
        private final int end;

        public RowComputer(Vector[] ys, Vector[] rxs, DistanceFunction df, int thread, int threads,
                double bandwidth) {
            this.ys = ys;
            this.rxs = rxs;
            this.df = df;
            this.bandwidth = bandwidth;
            this.start = thread * ys.length / threads;
            this.end = (thread + 1) * ys.length / threads;
        }

        @Override
        public SubMatrix call() throws Exception {
            double[][] ret = new double[end - start][rxs.length];
            for (int i = start; i < end; i++) {
                for (int j = 0; j < ret[0].length; j++) {
                    ret[i - start][j] = KbUtils.gaussian(df.distance(ys[i], rxs[j]), bandwidth);
                }
                double sum = MathUtils.sum(ret[i - start]);
                if (sum == 0.0) {
                    System.out.println("Zero row found in transition matrix. Bandwidth might be too small.");
                    int indexOfClosest = -1;
                    double closest = Double.POSITIVE_INFINITY;
                    for (int j = 0; j < rxs.length; j++) {
                        double dist = df.distance(ys[i], rxs[j]);
                        if (dist < closest) {
                            closest = dist;
                            indexOfClosest = j;
                        }
                    }
                    ret[i - start][indexOfClosest] = 1.0;
                }
                for (int j = 0; j < ret[0].length; j++) {
                    ret[i - start][j] /= sum;
                }
            }
            return new SubMatrix(start, ret);
        }
    }

    private static class SubMatrix {
        private final int start;
        private final double[][] rows;

        public SubMatrix(int start, double[][] rows) {
            this.start = start;
            this.rows = rows;
        }
    }

    private static class MatrixMultiplier implements Callable<SimpleMatrix> {
        private final SimpleMatrix a;
        private final SimpleMatrix b;

        public MatrixMultiplier(SimpleMatrix a, SimpleMatrix b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public SimpleMatrix call() throws Exception {
            SimpleMatrix c = a.mult(b);
            System.out.println("Multiplied.");
            return c;
        }
    }

    private static SimpleMatrix makeMatrix(Vector[] rowVs, Vector[] colVs, DistanceFunction df,
            double bandwidth, ExecutorService exec) {
        double[][] mat = new double[rowVs.length][colVs.length];
        CompletionService<SubMatrix> futures = new ExecutorCompletionService<SubMatrix>(exec);
        int threads = 10;
        for (int i = 0; i < threads; i++) {
            futures.submit(new RowComputer(rowVs, colVs, df, i, threads, bandwidth));
        }
        try {
            for (int i = 0; i < threads; i++) {
                SubMatrix sm = futures.take().get();
                for (int j = 0; j < sm.rows.length; j++) {
                    mat[j + sm.start] = sm.rows[j];
                }
                // System.out.println("Done with part " + sm.start);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return new SimpleMatrix(mat);
    }

    /**
     * Solves an MDP using KBSF
     * 
     * @param instance
     *            The MDP to be solved.
     * @param repStates
     *            The representative states
     * @param samples
     *            The sample transitions.
     * @param adf
     *            The action dependent metric.
     * @param exec
     *            An ExecutorService. The maximum number of threads usable is
     *            the number of actions in the MDP.
     * @param bandwidth
     *            The bandwidth to use.
     * @param steps
     *            The number of rounds of value iteration to perform.
     * @return The Q-values.
     */
    public static <S extends State, A extends Action> KernelQValue<S, A> solveByKbsf(
            MDP<S, A> instance, List<S> repStates, SampleTransitions<S, A> samples,
            ActionDistanceFn<A> adf, ExecutorService exec, double bandwidth, int steps) {
        System.out.println("Starting multithreaded KBSF");
        List<Transition<S, A>> t1s = samples.get(instance.getActions()[0]);
        Vector[] xs = new Vector[t1s.size()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = t1s.get(i).getStartVector();
            adf.memoize(xs[i]);
        }
        Vector[] rxs = new Vector[repStates.size()];
        for (int i = 0; i < repStates.size(); i++) {
            rxs[i] = instance.vectorFromState(repStates.get(i));
            adf.memoize(rxs[i]);
        }
        Vector[][] ys = new Vector[instance.getActions().length][xs.length];
        double[][] rewards = new double[ys.length][xs.length];
        SimpleMatrix[] repRewards = new SimpleMatrix[ys.length];
        for (A action : instance.getActions()) {
            int a = action.ordinal();
            List<Transition<S, A>> ts = samples.get(action);
            DistanceFunction df = adf.get(action);
            for (int i = 0; i < xs.length; i++) {
                ys[a][i] = ts.get(i).getEndVector();
                df.memoize(ys[a][i]);
                rewards[a][i] = ts.get(i).getReward();
            }
        }
        System.out.println("Prepared vectors.");

        SimpleMatrix[] dMatrix = new SimpleMatrix[instance.getActions().length];
        SimpleMatrix[] pMatrix = new SimpleMatrix[instance.getActions().length];
        List<Future<SimpleMatrix>> futurePs = new ArrayList<Future<SimpleMatrix>>();

        boolean uniqueDf = true;
        for (A action : instance.getActions()) {
            if (adf.get(action) != adf.get(instance.getActions()[0])) {
                uniqueDf = false;
                break;
            }
        }

        SimpleMatrix kMat = null;
        System.out.println("Processing actions");
        for (A action : instance.getActions()) {
            DistanceFunction df = adf.get(action);
            if (kMat == null || !uniqueDf) {
                kMat = makeMatrix(rxs, xs, df, bandwidth, exec);
            }
            System.out.println("Processing action " + action);
            int a = action.ordinal();

            double[] vals = new double[repStates.size()];
            for (int j = 0; j < repStates.size(); j++) {
                for (int i = 0; i < xs.length; i++) {
                    vals[j] += kMat.get(j, i) * rewards[action.ordinal()][i];
                }
            }

            repRewards[action.ordinal()] = new SimpleMatrix(vals.length, 1, false, vals);
            dMatrix[action.ordinal()] = makeMatrix(ys[a], rxs, df, bandwidth, exec);
            // pMatrix[action.ordinal()] = kMat.mult(dMatrix[action.ordinal()]);
            System.out.println("Matrices built, starting multiplication.");
            futurePs.add(exec.submit(new MatrixMultiplier(kMat, dMatrix[action.ordinal()])));
        }
        try {
            for (A action : instance.getActions()) {
                pMatrix[action.ordinal()] = futurePs.get(action.ordinal()).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        SimpleMatrix[] qValues = new SimpleMatrix[repRewards.length];
        for (int i = 0; i < qValues.length; i++) {
            qValues[i] = repRewards[i];
        }
        SimpleMatrix values = new SimpleMatrix(repStates.size(), 1);
        for (int i = 0; i < steps; i++) {
            // SimpleMatrix[] newVals = new SimpleMatrix[ys.length];
            for (int a = 0; a < ys.length; a++) {
                qValues[a] = repRewards[a].plus(instance.getDiscountFactor(),
                        pMatrix[a].mult(values));
            }
            double[] vals = new double[repStates.size()];
            for (int j = 0; j < vals.length; j++) {
                double val = Double.NEGATIVE_INFINITY;
                for (int a = 0; a < qValues.length; a++) {
                    val = Math.max(val, qValues[a].get(j));
                }
                vals[j] = val;
            }
            SimpleMatrix newVals = new SimpleMatrix(vals.length, 1, false, vals);
            SimpleMatrix diff = newVals.minus(values);
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (int w = 0; w < diff.getNumElements(); w++) {
                max = Math.max(max, diff.get(w));
                min = Math.min(min, diff.get(w));
            }
            // System.out.println(i + "   " + (max - min));
            values = newVals;
        }
        for (int a = 0; a < ys.length; a++) {
            qValues[a] = dMatrix[a].mult(qValues[a]);
        }
        double[][] qValArray = new double[qValues.length][xs.length];
        for (int a = 0; a < ys.length; a++) {
            for (int i = 0; i < qValArray[a].length; i++) {
                qValArray[a][i] = qValues[a].get(i);
            }
        }
        KernelQValue<S, A> qval = KernelQValue.of(instance, samples, adf, bandwidth);
        return qval.update(qValArray);
    }

}

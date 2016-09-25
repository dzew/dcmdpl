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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ejml.simple.SimpleMatrix;

import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.EuclideanDF;
import drl.math.tfs.Normalizer;
import drl.math.tfs.ValueSmoothingDF;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.Transition;

/**
 * A static utility class containing methods for kernel related computations.
 * 
 * @author Dawit
 * 
 */
public class KbUtils {

    private KbUtils() {
    };

    /**
     * 
     * @param x
     * @param sigma
     * @return {@code exp((x/sigma)^2)}
     */
    public static double gaussian(double x, double sigma) {
        return Math.exp(-x * x / (sigma * sigma));
    }

    /**
     * Create a Dimension-Adding VCM Relaxation.
     * 
     * @param qvf
     *            The Q-values for which to create the DAVR.
     * @param mdp
     *            The MDP.
     * @param oldDf
     *            The old metric.
     * @param transitions
     *            List of transitions to be memoized.
     * @param threadSafe
     *            Set this to true to create a thread safe DAVR.
     * @return
     */
    public static <S extends State, A extends Action> ActionDistanceFn<A> makeDavr(
            KernelQValue<S, A> qvf, MDP<S, A> mdp, ActionDistanceFn<A> oldDf,
            SampleTransitions<S, A> transitions, boolean threadSafe) {
        if (transitions == null) {
            List<Transition<S, A>> lst = Collections.emptyList();
            HashMap<A, List<Transition<S, A>>> mp = new HashMap<A, List<Transition<S, A>>>();
            for (A a : mdp.getActions()) {
                mp.put(a, lst);
            }
            transitions = new SampleTransitions<S, A>(mp);
        }
        if (oldDf == null) {
            oldDf = ActionDistanceFn.of(mdp.getActions(), EuclideanDF.instance);
        }
        Map<A, DistanceFunction> adfMap = new Hashtable<A, DistanceFunction>();
        double xSpread = Math.sqrt(mdp.getStateDimensions());
        for (A a : mdp.getActions()) {
            double ySpread = qvf.getMaxValue(a) - qvf.getMinValue(a);
            DistanceFunction df = ValueSmoothingDF.of(oldDf.get(a), qvf.getValue(a), ySpread
                    / xSpread, .5, threadSafe);
            int i = 0;
            for (Transition<S, A> transition : transitions.get(a)) {
                i++;
                df.memoize(transition.getStartVector());
            }
            adfMap.put(a, df);
            System.out.println("Finished processing action: " + a);
        }
        ActionDistanceFn<A> adf = new ActionDistanceFn<A>(adfMap);
        for (A a : mdp.getActions()) {
            int i = 0;
            for (Transition<S, A> transition : transitions.get(a)) {
                i++;
                adf.memoize(transition.getEndVector());
            }
            System.out.println("Finished memoizing for action: " + a);
        }
        return adf;
    }

    /**
     * Generates a collection of sample transitions by using sustained actions.
     * 
     * @param mdp
     *            The MDP from which to generate transitions.
     * @param states
     *            The start states.
     * @param df
     *            A metric for measuring how much the state changed. Set {@code
     *            df} to null to use a default value.
     * @param repeat
     *            A cap on how many times to repeat an action.
     * @param dist
     *            The minimum desired change in state.
     * @return The generated sample transitions.
     */
    public static <S extends State, A extends Action> SampleTransitions<S, A> generateSustainedTransitions(
            MDP<S, A> mdp, List<S> states, DistanceFunction df, int repeat, double dist) {
        df = df == null ? Normalizer.df(mdp.getStateSpace()) : df;
        Map<A, List<Transition<S, A>>> transitionData = new Hashtable<A, List<Transition<S, A>>>();
        for (A action : mdp.getActions()) {
            List<Transition<S, A>> data = MdpUtils.sustainedActionTransitions(mdp,
                    states,
                    action,
                    repeat,
                    dist,
                    df);
            for (Transition<S, A> t : data) {
                df.memoize(t.getStartVector());
                df.memoize(t.getEndVector());
            }
            transitionData.put(action, data);
        }
        return new SampleTransitions<S, A>(transitionData);
    }

    /**
     * Generates a collection of sample transitions.
     * 
     * @param mdp
     *            The MDP from which to generate transitions.
     * @param states
     *            The start states.
     * @param repeat
     *            A cap on how many times to repeat an action.
     * @param dist
     *            The minimum desired change in state.
     * @return The generated sample transitions.
     */
    public static <S extends State, A extends Action> SampleTransitions<S, A> generateTransitions(
            MDP<S, A> mdp, List<S> states) {
        Map<A, List<Transition<S, A>>> transitionData = new Hashtable<A, List<Transition<S, A>>>();
        for (A action : mdp.getActions()) {
            List<Transition<S, A>> data = new ArrayList<Transition<S, A>>(states.size());
            for (S state : states) {
                S end = mdp.simulate(state, action);
                Transition<S, A> t = Transition.of(state, action, end, mdp);
                data.add(t);
            }
            transitionData.put(action, data);
        }
        return new SampleTransitions<S, A>(transitionData);
    }

    static SimpleMatrix makeK(Vector[] xs, Vector[] rxs, DistanceFunction df, double bandwidth) {
        double[][] kMat = new double[rxs.length][xs.length];
        for (int j = 0; j < rxs.length; j++) {
            double sum = 0;
            for (int i = 0; i < xs.length; i++) {
                kMat[j][i] = gaussian(df.distance(rxs[j], xs[i]), bandwidth);
                sum += kMat[j][i];
            }
            if (sum == 0.0) {
                System.out.println("All-zero row in K matrix. Bandwidth too small?");
                int indexOfClosest = -1;
                double closest = Double.POSITIVE_INFINITY;
                for (int i = 0; i < xs.length; i++) {
                    double dist = df.distance(rxs[j], xs[i]);
                    if (dist < closest) {
                        closest = dist;
                        indexOfClosest = i;
                    }
                }
                kMat[j][indexOfClosest] = 1.0;
            } else {
                for (int i = 0; i < xs.length; i++) {
                    kMat[j][i] /= sum;
                }
            }
        }
        return new SimpleMatrix(kMat);
    }

    static class ValueIterator<S extends State, A extends Action> implements Callable<double[][]> {
        private final int start;
        private final int end;
        private final MDP<S, A> mdp;
        private final SampleTransitions<S, A> samples;
        private final AbstractKernelQValue<S, A> qval;

        public ValueIterator(int thread, int threads, SampleTransitions<S, A> samples,
                AbstractKernelQValue<S, A> qval, MDP<S, A> mdp) {
            int numStates = samples.get(mdp.getActions()[0]).size();
            this.start = thread * numStates / threads;
            this.end = (thread + 1) * numStates / threads;
            this.samples = samples;
            this.qval = qval;
            this.mdp = mdp;
        }

        public static <S extends State, A extends Action> ValueIterator<S, A> of(int thread,
                int threads, SampleTransitions<S, A> samples, AbstractKernelQValue<S, A> qval,
                MDP<S, A> mdp) {
            return new ValueIterator<S, A>(thread, threads, samples, qval, mdp);
        }

        @Override
        public double[][] call() throws Exception {
            double[][] ret = new double[mdp.getActions().length][end - start];
            for (A action : mdp.getActions()) {
                int a = action.ordinal();
                List<Transition<S, A>> ts = samples.get(action);
                for (int i = start; i < end; i++) {
                    double val = Double.NEGATIVE_INFINITY;
                    if (mdp.isTerminal(ts.get(i).getEndState())) {
                        val = 0;
                    } else {
                        for (A a2 : mdp.getActions()) {
                            val = Math.max(val, qval.getValue(ts.get(i).getEndVector(), a2));
                        }
                    }
                    ret[a][i - start] = val;
                }
            }
            return ret;
        }
    }
}

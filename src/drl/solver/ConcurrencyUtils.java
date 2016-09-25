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

package drl.solver;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import drl.math.tfs.DistanceFunction;
import drl.math.tfs.Normalizer;
import drl.math.tfs.ValueSmoothingDF;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.mdp.utils.Transition;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.SampleTransitions;

/**
 * A static utility class for parallelizing some computationally intensive
 * tasks.
 * 
 * @author Dawit
 * 
 */
public class ConcurrencyUtils {

    private ConcurrencyUtils() {
    }

    private static class Evaluator<S extends State, A extends Action> implements
            Callable<DistanceFunction> {
        private final MDP<S, A> mdp;
        private final KernelQValue<S, A> qvf;
        private final DistanceFunction oldDf;
        private final SampleTransitions<S, A> transitions;
        private final List<S> states;
        private final A action;
        private final double alpha;
        private final boolean threadSafe;

        public Evaluator(MDP<S, A> mdp, KernelQValue<S, A> qvf, DistanceFunction oldDf,
                SampleTransitions<S, A> transitions, List<S> states, A action, double alpha,
                boolean threadSafe) {
            this.mdp = mdp;
            this.qvf = qvf;
            this.oldDf = oldDf;
            this.transitions = transitions;
            this.states = states;
            this.action = action;
            this.alpha = alpha;
            this.threadSafe = threadSafe;
        }

        @Override
        public DistanceFunction call() throws Exception {
            double xSpread = Math.sqrt(mdp.getStateDimensions());
            double ySpread = qvf.getMaxValue(action) - qvf.getMinValue(action);
            DistanceFunction df = ValueSmoothingDF.of(oldDf, qvf.getValue(action), ySpread
                    / xSpread, alpha, threadSafe);
            for (Transition<S, A> transition : transitions.get(action)) {
                df.memoize(transition.getStartVector());
            }
            for (A a : mdp.getActions()) {
                for (Transition<S, A> transition : transitions.get(a)) {
                    df.memoize(transition.getEndVector());
                }
            }
            for (S state : states) {
                df.memoize(mdp.vectorFromState(state));
            }
            return df;
        }

    }

    /**
     * Constructs an ActionDistanceFn that corresponds to the Dimension-Adding
     * VCM Relaxation of the given Q-values.
     * 
     * @param mdp
     *            The MDP.
     * @param qvf
     *            The computed Q-values.
     * @param oldDf
     *            The metric used to create the Q-values. Set this to null to
     *            get an ActionDistanceFn that corresponds to Euclidean distance
     *            in the normalized domain.
     * @param transitions
     *            The sample transitions. These become memoized.
     * @param states
     *            Any additional states to be memoized.
     * @param exec
     *            The ExecutorService to use. The maximum concurrency possible
     *            equals the number of actions in the MDP (if there are 5
     *            actions this method can use 5 threads).
     * @param alpha
     *            The relaxation rate to use.
     * @param threadSafe
     *            Set this to true to produce a thread-safe ActionDistanceFn.
     * @return
     */
    public static <S extends State, A extends Action> ActionDistanceFn<A> parallelMakeAdfn(
            MDP<S, A> mdp, KernelQValue<S, A> qvf, ActionDistanceFn<A> oldDf,
            SampleTransitions<S, A> transitions, List<S> states, ExecutorService exec,
            double alpha, boolean threadSafe) {
        if (oldDf == null) {
            return ActionDistanceFn.of(mdp.getActions(), Normalizer.df(mdp.getStateSpace()));
        }
        try {
            Hashtable<A, Future<DistanceFunction>> futures = new Hashtable<A, Future<DistanceFunction>>();
            for (A a : mdp.getActions()) {
                futures.put(a, exec.submit(new Evaluator<S, A>(mdp,
                        qvf,
                        oldDf.get(a),
                        transitions,
                        states,
                        a,
                        alpha,
                        threadSafe)));
            }
            Hashtable<A, DistanceFunction> adfmap = new Hashtable<A, DistanceFunction>();
            for (A a : mdp.getActions()) {
                adfmap.put(a, futures.get(a).get());
            }
            return new ActionDistanceFn<A>(adfmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

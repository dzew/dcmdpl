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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drl.math.tfs.DistanceFunction;
import drl.math.tfs.Normalizer;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.solver.smoothing.ActionDistanceFn;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.Kbrl;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.MultithreadedKbrl;
import drl.solver.smoothing.SampleTransitions;

/**
 * A helper class to simplify the process of solving an MDP using KBRL.
 * 
 * @author Dawit
 * 
 */
public class KbrlCaller<S extends State, A extends Action> {

    private final MDP<S, A> mdp;
    private ActionDistanceFn<A> adfn = null;
    private DistanceFunction df;
    private List<S> states = null;
    private List<S> repStates = null;
    private SamplingStrategy strategy = SamplingStrategy.REACHABLE;
    private SampleTransitions<S, A> transitions = null;
    private int threads = 1;
    private double bandwidth = .01;
    private int steps = 300;
    private int numStates;

    private KbrlCaller(MDP<S, A> mdp) {
        this.mdp = mdp;
        df = Normalizer.df(mdp.getStateSpace());
    }

    /**
     * Static constructor.
     * 
     * @param mdp
     *            The MDP instance to be solved.
     * @return
     */
    public static <S extends State, A extends Action> KbrlCaller<S, A> of(MDP<S, A> mdp) {
        return new KbrlCaller<S, A>(mdp);
    }

    /**
     * Set the start states of the sample transitions.
     * 
     * @param states
     * @return {@code this}
     */
    public KbrlCaller<S, A> setStates(List<S> states) {
        this.states = states;
        this.transitions = null;
        return this;
    }

    /**
     * Set the metric {@code d^a} for use in the local averaging.
     * 
     * @param adfn
     * @return {@code this}
     */
    public KbrlCaller<S, A> setActionDistanceFn(ActionDistanceFn<A> adfn) {
        this.adfn = adfn;
        return this;
    }

    /**
     * Set a strategy for sampling states.
     * 
     * @param strategy
     *            The strategy to use.
     * @param numStates
     *            The number of states to sample.
     * @return {@code this}
     */
    public KbrlCaller<S, A> sampleStates(SamplingStrategy strategy, int numStates) {
        this.strategy = strategy;
        this.numStates = numStates;
        return this;
    }

    /**
     * Set the metric {@code d^a = df \forall a} for use in the local averaging.
     * 
     * @param df
     * @return {@code this}
     */
    public KbrlCaller<S, A> setDistanceFunction(DistanceFunction df) {
        this.df = df;
        this.adfn = null;
        return this;
    }

    // @Deprecated
    // public KbrlCaller<S, A> makeGraphDf(List<Vector> vecs, int size, double
    // epsilon) {
    // this.df = GraphDF.of(vecs, epsilon);
    // this.adfn = null;
    // return this;
    // }
    //
    // @Deprecated
    // public KbrlCaller<S, A> makeGraphDf(String filename, int size, double
    // epsilon) {
    // return makeGraphDf(Serializer.vectorsFromFile(filename), size, epsilon);
    // }

    /**
     * Set the list of representative states when using KBSF. If the
     * representative states are set to {@code null}, KBRL is used instead of
     * KBSF. The representative states default to {@code null} if this method is
     * not called.
     * 
     * @param repStates
     * @return {@code this}
     */
    public KbrlCaller<S, A> setRepresentativeStates(List<S> repStates) {
        this.repStates = repStates;
        return this;
    }

    /**
     * Use a set of sample transitions. Note that the current implementation of
     * KBSF requires that the sample transitions for each action have an
     * identical list of start states.
     * 
     * @param transitions
     * @return {@code this}
     */
    public KbrlCaller<S, A> useTransitions(SampleTransitions<S, A> transitions) {
        this.transitions = transitions;
        return this;
    }

    /**
     * Set the bandwidth parameter.
     * 
     * @param bandwidth
     * @return {@code this}
     */
    public KbrlCaller<S, A> setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    /**
     * Set an upper bound on the number of rounds of value iteration to perform.
     * If value iteration converges before the bound is reached, the computation
     * stops early.
     * 
     * @param steps
     * @return {@code this}
     */
    public KbrlCaller<S, A> setSteps(int steps) {
        this.steps = steps;
        return this;
    }

    /**
     * Use a multithreaded implementation of KBRL or KBSF.
     * 
     * @param threads
     *            The number of threads to use. The current implementation of
     *            KBSF uses ejml, which does not allow for good concurrency. For
     *            best results, you may want to implement iKBSF or change the
     *            implementation to use a different library (for instance,
     *            Parallel Colt).
     * @return {@code this}
     */
    public KbrlCaller<S, A> makeMultithreaded(int threads) {
        this.threads = threads;
        return this;
    }

    /**
     * @return The Q-values of the mdp as approximated by KBRL (or KBSF).
     */
    public KernelQValue<S, A> solve() {
        if (adfn == null) {
            adfn = ActionDistanceFn.of(mdp.getActions(), df);
        }

        if (transitions == null) {
            if (states == null) {
                states = StateSampler.sample(mdp, strategy, numStates);
            }
            transitions = KbUtils.generateTransitions(mdp, states);
        }

        if (repStates != null) {
            if (threads > 1) {
                ExecutorService exec = Executors.newFixedThreadPool(threads);
                KernelQValue<S, A> qvf = MultithreadedKbrl.solveByKbsf(mdp,
                        repStates,
                        transitions,
                        adfn,
                        exec,
                        bandwidth,
                        steps);
                exec.shutdown();
                return qvf;
            }
            return Kbrl.solveByKbsf(mdp, repStates, transitions, adfn, bandwidth, steps);
        }

        KernelQValue<S, A> qvf = KernelQValue.of(mdp, transitions, adfn, bandwidth);

        if (threads > 1) {
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            qvf = MultithreadedKbrl.solve(qvf, mdp, transitions, exec, threads, steps);
            exec.shutdown();
            return qvf;
        }
        return Kbrl.solve(qvf, mdp, transitions, steps);
    }

}

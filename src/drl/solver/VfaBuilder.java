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

import drl.math.geom.Cell;
import drl.math.vfa.LinearFAs;
import drl.math.vfa.LvfFactory;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;
import drl.solver.leastsquares.LSTDQ;
import drl.solver.leastsquares.LvfApproximator;
import drl.solver.leastsquares.MultithreadedLSTDQ;
import drl.solver.leastsquares.MultithreadedRolloutApproximator;
import drl.solver.leastsquares.RolloutApproximator;

/**
 * A builder to simplify the creation of value function approximators.
 * 
 * @author Dawit
 * 
 */
public class VfaBuilder<S extends State, A extends Action> {

    private static final int[] defaultSampleSizes = new int[] { 0, 60, 500, 1000 };

    private ExecutorService exec;
    private int threads;
    private int samples;
    private SamplingStrategy samplingStrategy = SamplingStrategy.RANDOM;
    private Approximator approximator = Approximator.LSTDQ;
    private LvfFactory factory;
    private List<S> states;
    private double ridgeEps = 0;

    private final MDP<S, A> mdp;

    /**
     * 
     * @param mdp
     *            The MDP for which an approximator is needed.
     */
    public VfaBuilder(MDP<S, A> mdp) {
        this.mdp = mdp;
        Cell domain = mdp.getStateSpace();
        this.factory = LinearFAs.fourierBasisFactory(domain, 5);
        this.samples = (domain.getDimensions() < defaultSampleSizes.length) ? defaultSampleSizes[domain.getDimensions()]
                : 3000;
    }

    /**
     * Static constructor. Use this instead of the actual constructor.
     * 
     * @param mdp
     *            The MDP for which an approximator is needed.
     */
    public static <S extends State, A extends Action> VfaBuilder<S, A> of(MDP<S, A> mdp) {
        return new VfaBuilder<S, A>(mdp);
    }

    public LvfApproximator<S, A> build() {
        List<S> supportStates = sampleStates();
        if (exec == null) {
            switch (approximator) {
            case ROLLOUT_BASED:
                return RolloutApproximator.of(factory, mdp, supportStates);
            case LSTDQ:
                return LSTDQ.of(factory, mdp, supportStates, ridgeEps);
            default:
                throw new IllegalArgumentException("No such approximator: " + approximator);
            }
        }
        switch (approximator) {
        case LSTDQ:
            return MultithreadedLSTDQ.of(factory, mdp, supportStates, ridgeEps, exec, threads);
        case ROLLOUT_BASED:
            return MultithreadedRolloutApproximator.of(factory, mdp, supportStates, exec, threads);
        default:
            throw new UnsupportedOperationException(approximator + " cannot be multithreaded.");
        }

    }

    private List<S> sampleStates() {
        return states == null ? StateSampler.sample(mdp, samplingStrategy, samples) : states;
    }

    /**
     * Use the multithreaded version of the value function approximator if it
     * exists. If there is no multithreaded version, the build method will throw
     * an {@link UnsupportedOperationException}
     * 
     * @param exec
     *            The ExecutorService to use. Set this to {@code null} to create
     *            the single threaded version.
     * @param threads
     *            The number of tasks the approximator is allowed to give the
     *            ExecutorService for concurrent execution.
     * @return this builder.
     */
    public VfaBuilder<S, A> makeMultithreaded(ExecutorService exec, int threads) {
        this.exec = exec;
        this.threads = threads;
        return this;
    }

    /**
     * Set the number of points to sample.
     * 
     * @param samples
     * @return
     */
    public VfaBuilder<S, A> setNumberSamples(int samples) {
        this.samples = samples;
        return this;
    }

    /**
     * Set the sampling strategy (assuming the samples have not been pre set).
     * 
     * @param samplingStrategy
     * @return
     */
    public VfaBuilder<S, A> setSamplingStrategy(SamplingStrategy samplingStrategy) {
        this.samplingStrategy = samplingStrategy;
        return this;
    }

    /**
     * Set the value function approximation algorithm to use.
     * 
     * @param approximator
     * @return
     */
    public VfaBuilder<S, A> setApproximator(Approximator approximator) {
        this.approximator = approximator;
        return this;
    }

    /**
     * Set the value function approximation basis to use.
     * 
     * @param factory
     * @return
     */
    public VfaBuilder<S, A> setBasisFunction(LvfFactory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Set the states to use in the approximator. If this is set, the
     * approximation scheme and number of samples settings are ignored.
     * 
     * @param states
     * @return
     */
    public VfaBuilder<S, A> useSupportStates(List<S> states) {
        this.states = states;
        return this;
    }

    /**
     * Set a value for ridge regression.
     * 
     * @param eps
     * @return
     */
    public VfaBuilder<S, A> setRegularizer(double eps) {
        this.ridgeEps = eps;
        return this;
    }

    public static enum Approximator {
        /**
         * A naive approximator that does a Monte Carlo roll-out from each
         * sampled point to estimate its value.
         */
        ROLLOUT_BASED,

        /**
         * Use the algorithm described in Figure 5 of Lagoudakis, Michail G.,
         * and Ronald Parr. "Least-squares policy iteration." The Journal of
         * Machine Learning Research 4 (2003): 1107-1149.
         */
        LSTDQ
    }

}

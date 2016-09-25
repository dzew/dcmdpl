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

package drl.solver.leastsquares;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import drl.math.geom.Vector;
import drl.math.vfa.LvfFactory;
import drl.math.vfa.ValueFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;
import drl.mdp.utils.MdpUtils;
import drl.mdp.utils.QValueImpl;
import drl.mdp.utils.Transition;

import org.ejml.simple.SimpleMatrix;

/**
 * A multithreaded, linear value function approximator that evaluates policies
 * by performing Monte-Carlo rollouts.
 * 
 * @author Dawit
 * 
 */
public class MultithreadedRolloutApproximator<S extends State, A extends Action> implements
        LvfApproximator<S, A> {

    private static final int HORIZON = 20;

    private final SimpleMatrix multiplier;
    private final List<S> states;
    private final MDP<S, A> instance;
    private final LvfFactory constructor;
    private final int threads;
    private final ExecutorService exec;

    private MultithreadedRolloutApproximator(LvfFactory constructor, MDP<S, A> instance,
            SimpleMatrix multiplier, List<S> states, ExecutorService exec, int threads) {
        this.constructor = constructor;
        this.multiplier = multiplier;
        this.states = states;
        this.instance = instance;
        this.threads = threads;
        this.exec = exec;
    }

    /**
     * Static constructor.
     * 
     * @param constructor
     *            The linear value function factory to use.
     * @param instance
     *            The MDP to be solved.
     * @param states
     *            List of states from which to start rollouts.
     * @param exec
     *            An executor service.
     * @param threads
     *            The maximum number of threads to use.
     * @return
     */
    public static <S extends State, A extends Action> MultithreadedRolloutApproximator<S, A> of(
            LvfFactory constructor, MDP<S, A> instance, List<S> states, ExecutorService exec,
            int threads) {
        Vector[] stateVectors = new Vector[states.size()];
        for (int i = 0; i < stateVectors.length; i++) {
            stateVectors[i] = instance.vectorFromState(states.get(i));
        }
        SimpleMatrix phi = constructor.generateBases(stateVectors);
        SimpleMatrix multiplier = phi.transpose().mult(phi).invert().mult(phi.transpose());
        return new MultithreadedRolloutApproximator<S, A>(constructor,
                instance,
                multiplier,
                states,
                exec,
                threads);
    }

    private class MatrixMultiplier implements Callable<ValueFunction> {

        private final SimpleMatrix a;
        private final double[] b;

        public MatrixMultiplier(SimpleMatrix a, double[] b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public ValueFunction call() throws Exception {
            return constructor.construct(a.mult(new SimpleMatrix(b.length, 1, false, b)));
        }

    }

    private class QValueComputer implements Callable<Map<A, double[]>> {
        private final int start;
        private final int end;
        private final Policy<S, A> policy;
        private final A[] actions;

        public QValueComputer(int thread, Policy<S, A> policy, A[] actions) {
            this.start = thread * states.size() / threads;
            this.end = (thread + 1) * states.size() / threads;
            this.policy = policy;
            this.actions = actions;
        }

        @Override
        public Map<A, double[]> call() throws Exception {
            Map<A, double[]> ret = new HashMap<A, double[]>();
            for (A action : actions) {
                double[] values = new double[end - start];
                for (int i = 0; i < values.length; i++) {
                    S state = states.get(i + start);
                    S end = instance.simulate(state, action);
                    List<Transition<S, A>> trajectory = MdpUtils.rollout(instance,
                            end,
                            HORIZON,
                            policy);
                    values[i] = MdpUtils.getValue(trajectory, instance.getDiscountFactor());
                    values[i] *= instance.getDiscountFactor();
                    values[i] += instance.getReward(state, action, end);

                }
                ret.put(action, values);
            }
            return ret;
        }
    }

    /**
     * @param policy
     * @return The value of executing {@code policy}.
     */
    public ValueFunction approximateValueOf(Policy<S, A> policy) {
        double[] values = new double[states.size()];
        for (int i = 0; i < values.length; i++) {
            S state = states.get(i);
            List<Transition<S, A>> trajectory = MdpUtils.rollout(instance, state, HORIZON, policy);
            values[i] = MdpUtils.getValue(trajectory, instance.getDiscountFactor());
        }
        SimpleMatrix valueVector = new SimpleMatrix(values.length, 1, false, values);
        SimpleMatrix coeffVector = multiplier.mult(valueVector);
        ValueFunction ret = constructor.construct(coeffVector);
        return ret;
    }

    @Override
    public QValue<S, A> approximateQValueOf(Policy<S, A> policy) {
        List<Future<Map<A, double[]>>> results = new ArrayList<Future<Map<A, double[]>>>(threads);
        for (int thread = 1; thread < this.threads; thread++) {
            results.add(exec.submit(new QValueComputer(thread, policy, instance.getActions())));
        }

        Map<A, double[]> qValues = null;
        try {
            qValues = new QValueComputer(0, policy, instance.getActions()).call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (A action : instance.getActions()) {
            double[] values = new double[states.size()];
            double[] partialVals = qValues.get(action);
            for (int i = 0; i < partialVals.length; i++) {
                values[i] = partialVals[i];
            }
            qValues.put(action, values);
        }
        Map<A, double[]> part = null;
        for (int thread = 1; thread < this.threads; thread++) {
            try {
                part = results.get(thread - 1).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (A action : instance.getActions()) {
                double[] values = qValues.get(action);
                double[] partialVals = part.get(action);
                int offset = thread * states.size() / threads;
                for (int i = 0; i < partialVals.length; i++) {
                    values[i + offset] = partialVals[i];
                }
            }
        }
        Map<A, ValueFunction> ret = new HashMap<A, ValueFunction>();
        Map<A, Future<ValueFunction>> vfas = new HashMap<A, Future<ValueFunction>>();
        for (A action : instance.getActions()) {
            vfas.put(action, exec.submit(new MatrixMultiplier(multiplier, qValues.get(action))));
        }
        try {
            for (A action : instance.getActions()) {
                ret.put(action, vfas.get(action).get());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new QValueImpl<S, A>(ret, instance);
    }

}

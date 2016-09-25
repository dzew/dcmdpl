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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import org.ejml.simple.SimpleMatrix;

import drl.math.vfa.ValueFunction;
import drl.math.vfa.LvfFactory;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;
import drl.mdp.utils.QValueImpl;

/**
 * A multitreaded implementation of LSTDQ, the algorithm described in Figure 5
 * of Lagoudakis, Michail G., and Ronald Parr. "Least-squares policy iteration."
 * The Journal of Machine Learning Research 4 (2003): 1107-1149.
 * 
 * @author Dawit
 * 
 */
public class MultithreadedLSTDQ<S extends State, A extends Action> implements LvfApproximator<S, A> {

    private final LvfFactory constructor;
    private final MDP<S, A> instance;
    private final List<S> states;
    private final SimpleMatrix aMatrixBase;
    private final int threads;
    private final ExecutorService exec;

    private MultithreadedLSTDQ(LvfFactory constructor, MDP<S, A> instance, List<S> states,
            SimpleMatrix aMatrixBase, ExecutorService exec, int threads) {
        this.constructor = constructor;
        this.instance = instance;
        this.states = states;
        this.aMatrixBase = aMatrixBase;
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
     *            List of states from which to generate sample transitions.
     * @param eps
     *            Ridge regression epsilon.
     * @param exec
     *            An ExecutorService.
     * @param threads
     *            The maximum number of threads to use.
     * @return
     */
    public static <S extends State, A extends Action> MultithreadedLSTDQ<S, A> of(
            LvfFactory constructor, MDP<S, A> instance, List<S> states, double eps,
            ExecutorService exec, int threads) {
        int bases = constructor.generateBases(instance.vectorFromState(states.get(0))).length;
        SimpleMatrix aMatrixBase = SimpleMatrix.identity(bases).scale(eps * states.size());

        CompletionService<SimpleMatrix> service = new ExecutorCompletionService<SimpleMatrix>(exec);
        for (int thread = 0; thread < threads; thread++) {
            service.submit(new AMatrixInitializer<S, A>(thread,
                    threads,
                    states,
                    constructor,
                    instance));
        }

        try {
            for (int thread = 0; thread < threads; thread++) {
                aMatrixBase = aMatrixBase.plus(service.take().get());
            }
        } catch (Exception e) {
            System.err.println("Concurency problem during initialization.");
            throw new RuntimeException(e);
        }

        return new MultithreadedLSTDQ<S, A>(constructor,
                instance,
                states,
                aMatrixBase,
                exec,
                threads);
    }

    private double[][] generateA() {
        int bases = aMatrixBase.getMatrix().numCols;
        int actions = instance.getActions().length;
        double[][] aMatrix = new double[bases * actions][bases * actions];
        for (int i = 0; i < bases; i++) {
            for (int j = 0; j < bases; j++) {
                for (int a = 0; a < actions; a++) {
                    aMatrix[a * bases + i][a * bases + j] = aMatrixBase.get(i, j);
                }
            }
        }
        return aMatrix;
    }

    private SimpleMatrix stretch(SimpleMatrix ds, int a, int numA) {
        double[] ret = new double[ds.numRows() * numA];
        for (int i = 0; i < ds.numRows(); i++) {
            ret[a * ds.numRows() + i] = ds.get(i);
        }
        return new SimpleMatrix(ret.length, 1, false, ret);
    }

    private static class AMatrixInitializer<S extends State, A extends Action> implements
            Callable<SimpleMatrix> {
        private final int start;
        private final int end;
        private final MDP<S, A> instance;
        private final LvfFactory constructor;
        private final List<S> states;

        public AMatrixInitializer(int thread, int threads, List<S> states, LvfFactory constructor,
                MDP<S, A> instance) {
            this.start = thread * states.size() / threads;
            this.end = (thread + 1) * states.size() / threads;
            this.instance = instance;
            this.constructor = constructor;
            this.states = states;
        }

        @Override
        public SimpleMatrix call() {
            int bases = constructor.generateBases(instance.vectorFromState(states.get(0))).length;
            SimpleMatrix aMatrixBase = new SimpleMatrix(bases, bases);
            for (int s = start; s < end; s++) {
                double[] phiValues = constructor.generateBases(instance.vectorFromState(states.get(s)));
                SimpleMatrix phi = new SimpleMatrix(bases, 1, true, phiValues);
                aMatrixBase = aMatrixBase.plus(phi.mult(phi.transpose()));
            }
            return aMatrixBase;
        }
    }

    private static class MatrixPair {
        private final SimpleMatrix a;
        private final SimpleMatrix b;

        public MatrixPair(SimpleMatrix a, SimpleMatrix b) {
            this.a = a;
            this.b = b;
        }
    }

    private class MatrixAdder implements Callable<MatrixPair> {
        private final int start;
        private final int end;
        private final Policy<S, A> policy;

        public MatrixAdder(int thread, Policy<S, A> policy) {
            this.start = thread * states.size() / threads;
            this.end = (thread + 1) * states.size() / threads;
            this.policy = policy;
        }

        @Override
        public MatrixPair call() {
            int bases = aMatrixBase.getMatrix().numCols;
            A[] actions = instance.getActions();
            double[][] aMatrix = new double[bases * actions.length][bases * actions.length];
            SimpleMatrix bMatrix = new SimpleMatrix(bases * actions.length, 1);
            double gamma = instance.getDiscountFactor();
            for (int s = start; s < end; s++) {
                for (int a = 0; a < actions.length; a++) {
                    A action = actions[a];
                    S endState = instance.simulate(states.get(s), action);
                    if (instance.isTerminal(endState)) {
                        continue;
                    }
                    SimpleMatrix phi1 = new SimpleMatrix(bases,
                            1,
                            false,
                            constructor.generateBases(instance.vectorFromState(states.get(s))));
                    int a2 = policy.getAction(endState).ordinal();
                    SimpleMatrix phi2 = new SimpleMatrix(bases,
                            1,
                            false,
                            constructor.generateBases(instance.vectorFromState(endState)));

                    SimpleMatrix addOn = phi1.mult(phi2.transpose());
                    for (int i = 0; i < bases; i++) {
                        for (int j = 0; j < bases; j++) {
                            aMatrix[i + a * bases][j + a2 * bases] -= gamma * addOn.get(i, j);
                        }
                    }
                    phi1 = stretch(phi1, a, actions.length);
                    bMatrix = bMatrix.plus(phi1.scale(instance.getReward(states.get(s),
                            action,
                            endState)));
                }
            }
            return new MatrixPair(new SimpleMatrix(aMatrix), bMatrix);
        }
    }

    @Override
    public QValue<S, A> approximateQValueOf(Policy<S, A> policy) {
        CompletionService<MatrixPair> service = new ExecutorCompletionService<MatrixPair>(exec);
        for (int thread = 0; thread < this.threads; thread++) {
            service.submit(new MatrixAdder(thread, policy));
        }
        SimpleMatrix a = new SimpleMatrix(generateA());
        SimpleMatrix b = new SimpleMatrix(a.numRows(), 1);
        try {
            for (int thread = 0; thread < this.threads; thread++) {
                MatrixPair pair = service.take().get();
                a = a.plus(pair.a);
                b = b.plus(pair.b);
            }
        } catch (Exception e) {
            System.err.println("Concurency problem!");
            throw new RuntimeException(e);
        }
        // System.out.println(a);
        return qValueFromWeights(a.solve(b));
    }

    private QValue<S, A> qValueFromWeights(SimpleMatrix weights) {
        A[] actions = instance.getActions();
        int bases = weights.getNumElements() / actions.length;
        HashMap<A, ValueFunction> valueMapping = new HashMap<A, ValueFunction>();
        // System.out.print("weights.append([");
        for (int i = 0; i < actions.length; i++) {
            double[] coeffs = new double[bases];
            for (int j = 0; j < coeffs.length; j++) {
                coeffs[j] = weights.get(j + i * coeffs.length);
                // System.out.print(coeffs[j] + ", ");
            }
            valueMapping.put(actions[i], constructor.construct(new SimpleMatrix(coeffs.length,
                    1,
                    false,
                    coeffs)));
        }
        // System.out.println("])");
        return new QValueImpl<S, A>(valueMapping, instance);
    }
}

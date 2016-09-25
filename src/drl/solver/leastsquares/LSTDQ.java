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
 * A linear value function approximator that evaluates policies by performing
 * LSTDQ, the algorithm described in Figure 5 of Lagoudakis, Michail G., and
 * Ronald Parr. "Least-squares policy iteration." The Journal of Machine
 * Learning Research 4 (2003): 1107-1149.
 * 
 * @author Dawit
 * 
 */
public class LSTDQ<S extends State, A extends Action> implements LvfApproximator<S, A> {

    private final LvfFactory constructor;
    private final MDP<S, A> instance;
    private final List<S> states;
    private final SimpleMatrix aMatrixBase;

    private LSTDQ(LvfFactory constructor, MDP<S, A> instance, List<S> states,
            SimpleMatrix aMatrixBase) {
        this.constructor = constructor;
        this.instance = instance;
        this.states = states;
        this.aMatrixBase = aMatrixBase;
    }

    /**
     * Static constructor.
     * 
     * @param constructor
     *            The linear value function factory to use.
     * @param instance
     *            The MDP to be solved.
     * @param states
     *            The list of sample states to use (this class calls {@code
     *            instance.simulate} on these states to generate sample
     *            transitions.
     * @param eps
     *            Ridge regression epsilon.
     */
    public static <S extends State, A extends Action> LSTDQ<S, A> of(LvfFactory constructor,
            MDP<S, A> instance, List<S> states, double eps) {
        int bases = constructor.generateBases(instance.vectorFromState(states.get(0))).length;
        SimpleMatrix aMatrixBase = SimpleMatrix.identity(bases).scale(eps * states.size());
        for (int s = 0; s < states.size(); s++) {
            double[] phiValues = constructor.generateBases(instance.vectorFromState(states.get(s)));
            SimpleMatrix phi = new SimpleMatrix(bases, 1, true, phiValues);
            aMatrixBase = aMatrixBase.plus(phi.mult(phi.transpose()));
        }
        return new LSTDQ<S, A>(constructor, instance, states, aMatrixBase);
    }

    private SimpleMatrix stretch(SimpleMatrix ds, int a, int numA) {
        double[] ret = new double[ds.numRows() * numA];
        for (int i = 0; i < ds.numRows(); i++) {
            ret[a * ds.numRows() + i] = ds.get(i);
        }
        return new SimpleMatrix(ret.length, 1, false, ret);
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

    @Override
    public QValue<S, A> approximateQValueOf(Policy<S, A> policy) {
        A[] actions = instance.getActions();
        int bases = aMatrixBase.getMatrix().numCols;
        double[][] aMatrix = generateA();
        SimpleMatrix bMatrix = new SimpleMatrix(bases * actions.length, 1);
        double gamma = instance.getDiscountFactor();

        for (int s = 0; s < states.size(); s++) {
            for (int a = 0; a < actions.length; a++) {
                A action = actions[a];
                S end = instance.simulate(states.get(s), action);
                if (instance.isTerminal(end)) {
                    continue;
                }
                SimpleMatrix phi1 = new SimpleMatrix(bases,
                        1,
                        false,
                        constructor.generateBases(instance.vectorFromState(states.get(s))));
                int a2 = policy.getAction(end).ordinal();
                SimpleMatrix phi2 = new SimpleMatrix(bases,
                        1,
                        false,
                        constructor.generateBases(instance.vectorFromState(end)));

                SimpleMatrix addOn = phi1.mult(phi2.transpose());
                for (int i = 0; i < bases; i++) {
                    for (int j = 0; j < bases; j++) {
                        aMatrix[i + a * bases][j + a2 * bases] -= gamma * addOn.get(i, j);
                    }
                }
                phi1 = stretch(phi1, a, actions.length);
                bMatrix = bMatrix.plus(phi1.scale(instance.getReward(states.get(s), action, end)));
            }
        }

        SimpleMatrix a = new SimpleMatrix(aMatrix);
        // System.out.println(a);
        return qValueFromWeights(a.solve(bMatrix));
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

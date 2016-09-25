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
import java.util.Map;

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
 * A linear value function approximator that evaluates policies by performing
 * Monte-Carlo rollouts. This class mainly exists for verifying the correctness
 * of other, more sophisticated approximators.
 * 
 * @author Dawit
 * 
 */
public class RolloutApproximator<S extends State, A extends Action> implements
        LvfApproximator<S, A> {

    private static final int HORIZON = 200;

    private final SimpleMatrix multiplier;
    private final List<S> states;
    private final MDP<S, A> mdp;
    private final LvfFactory factory;

    private RolloutApproximator(LvfFactory factory, MDP<S, A> mdp, SimpleMatrix multiplier,
            List<S> states) {
        this.factory = factory;
        this.multiplier = multiplier;
        this.states = states;
        this.mdp = mdp;
    }

    /**
     * Static constructor.
     * 
     * @param factory
     *            A factory for linear value functions.
     * @param mdp
     *            The MDP to be solved.
     * @param states
     *            The list of states from which to perform rollouts.
     * @return
     */
    public static <S extends State, A extends Action> RolloutApproximator<S, A> of(
            LvfFactory factory, MDP<S, A> mdp, List<S> states) {
        Vector[] stateVectors = new Vector[states.size()];
        for (int i = 0; i < stateVectors.length; i++) {
            stateVectors[i] = mdp.vectorFromState(states.get(i));
        }
        SimpleMatrix phi = factory.generateBases(stateVectors);
        SimpleMatrix multiplier = phi.transpose().mult(phi).invert().mult(phi.transpose());
        return new RolloutApproximator<S, A>(factory, mdp, multiplier, states);
    }

    /**
     * 
     * @param policy
     * @return The value of {@code policy}.
     */
    public ValueFunction approximateValueOf(Policy<S, A> policy) {
        double[] values = new double[states.size()];
        for (int i = 0; i < values.length; i++) {
            S state = states.get(i);
            List<Transition<S, A>> trajectory = MdpUtils.rollout(mdp, state, HORIZON, policy);
            values[i] = MdpUtils.getValue(trajectory, mdp.getDiscountFactor());
        }
        SimpleMatrix valueVector = new SimpleMatrix(values.length, 1, false, values);
        SimpleMatrix coeffVector = multiplier.mult(valueVector);
        ValueFunction ret = factory.construct(coeffVector);
        return ret;
    }

    @Override
    public QValue<S, A> approximateQValueOf(Policy<S, A> policy) {
        Map<A, ValueFunction> ret = new HashMap<A, ValueFunction>();
        for (A action : mdp.getActions()) {
            double[] values = new double[states.size()];
            for (int i = 0; i < values.length; i++) {
                S state = states.get(i);
                S end = mdp.simulate(state, action);
                List<Transition<S, A>> trajectory = MdpUtils.rollout(mdp, end, HORIZON, policy);
                values[i] = mdp.getDiscountFactor()
                        * MdpUtils.getValue(trajectory, mdp.getDiscountFactor());
                values[i] += mdp.getReward(state, action, end);
            }
            SimpleMatrix valueVector = new SimpleMatrix(values.length, 1, false, values);
            // System.out.println(valueVector.transpose());
            SimpleMatrix coeffVector = multiplier.mult(valueVector);
            ret.put(action, factory.construct(coeffVector));
        }
        return new QValueImpl<S, A>(ret, mdp);
    }

}

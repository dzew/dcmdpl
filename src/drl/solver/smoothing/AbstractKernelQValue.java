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

import drl.math.geom.Vector;
import drl.math.vfa.ValueFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.QValue;
import drl.mdp.api.State;
import drl.mdp.utils.ActionValueFn;

/**
 * Abstract class providing partial implementation of a Q-value function
 * represented through kernel regression.
 * 
 * @author Dawit
 * 
 * @param <S>
 * @param <A>
 */
abstract class AbstractKernelQValue<S extends State, A extends Action> implements QValue<S, A> {

    private final MDP<S, A> mdp;

    public AbstractKernelQValue(MDP<S, A> mdp) {
        this.mdp = mdp;
    }

    @Override
    public A getAction(S state) {
        double bestValue = Double.NEGATIVE_INFINITY;
        A bestAction = null;

        for (A a : mdp.getActions()) {
            double value = getValue(state, a);
            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }
        return bestAction;
    }

    @Override
    public double getValue(S state) {
        double val = Double.NEGATIVE_INFINITY;
        for (A action : mdp.getActions()) {
            val = Math.max(val, getValue(state, action));
        }
        return val;
    }

    @Override
    public double getValue(S state, A action) {
        Vector x = mdp.vectorFromState(state);
        return getValue(x, action);
    }

    @Override
    public ValueFunction getValue(A action) {
        return ActionValueFn.of(this, mdp, action);
    }

    /**
     * Returns the expected value after taking the specified action from the
     * state with the given vector representation.
     * 
     * @param x
     *            The Vector representation of a state
     * @param action
     * @return Q(x,action);
     */
    protected abstract double getValue(Vector x, A action);

    /**
     * 
     * @param b
     * @return A new KernelQValue identical to {@code this} except for its
     *         bandwidth, which is {@code b}.
     */
    public abstract AbstractKernelQValue<S, A> withBandwidth(double b);

}

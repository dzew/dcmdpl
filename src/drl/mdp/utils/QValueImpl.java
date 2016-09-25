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

package drl.mdp.utils;

import java.util.Map;

import drl.math.geom.Vector;
import drl.math.vfa.ValueFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.QValue;
import drl.mdp.api.State;

/**
 * An implementation of the QValue interface.
 * 
 * @author Dawit
 * 
 * @param <S>
 * @param <A>
 */
public class QValueImpl<S extends State, A extends Action> implements QValue<S, A> {

    private final Map<A, ValueFunction> values;
    private final MDP<S, A> instance;

    public QValueImpl(Map<A, ValueFunction> values, MDP<S, A> instance) {
        this.values = values;
        this.instance = instance;
    }

    @Override
    public double getValue(S state) {
        double max = Double.NEGATIVE_INFINITY;
        for (A action : values.keySet()) {
            max = Math.max(max, getValue(state, action));
        }
        return max;
    }

    @Override
    public double getValue(S s, A a) {
        return values.get(a).value(instance.vectorFromState(s));
    }

    @Override
    public A getAction(S state) {
        double bestValue = Double.NEGATIVE_INFINITY;
        A bestAction = null;
        Vector stateVector = instance.vectorFromState(state);

        for (A a : instance.getActions()) {
            double value = values.get(a).value(stateVector);
            if (value > bestValue) {
                bestValue = value;
                bestAction = a;
            }
        }
        return bestAction;
    }

    @Override
    public String toString() {
        return "QValues:" + values;
    }

    @Override
    public ValueFunction getValue(A action) {
        return values.get(action);
    }

}

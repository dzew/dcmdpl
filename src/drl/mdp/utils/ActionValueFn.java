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

import drl.math.geom.Vector;
import drl.math.vfa.ValueFunction;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.QValue;
import drl.mdp.api.State;

/**
 * A class for currying the action from a Q-value.
 * 
 * @author Dawit
 * 
 */
public class ActionValueFn<S extends State, A extends Action> implements ValueFunction {

    private final QValue<S, A> qvf;
    private final MDP<S, A> mdp;
    private final A action;

    public ActionValueFn(QValue<S, A> qvf, MDP<S, A> mdp, A action) {
        super();
        this.qvf = qvf;
        this.mdp = mdp;
        this.action = action;
    }

    public static <S extends State, A extends Action> ActionValueFn<S, A> of(QValue<S, A> qvf,
            MDP<S, A> mdp, A action) {
        return new ActionValueFn<S, A>(qvf, mdp, action);
    }

    @Override
    public double value(Vector v) {
        return qvf.getValue(mdp.stateFromVector(v), action);
    }

    @Override
    public double difference(ValueFunction vf) {
        throw new UnsupportedOperationException("ActionValueFn does not support the difference function.");
    }

}

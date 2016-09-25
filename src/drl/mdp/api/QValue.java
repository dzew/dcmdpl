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

package drl.mdp.api;

import drl.math.vfa.ValueFunction;

/**
 * A representation of the state-action value function of a Markov Decision
 * Process.
 * 
 * @author Dawit
 * 
 */
public interface QValue<S extends State, A extends Action> extends Policy<S, A> {

    /**
     * 
     * @param state
     * @return The max over actions of the Q-value of {@code state}
     */
    public double getValue(S state);

    /**
     * 
     * @param state
     * @param action
     * @return {@code Q(state, action)}
     */
    public double getValue(S state, A action);

    /**
     * Returns the best action for {@code state} given these Q-Values.
     * 
     * @param state
     * @return The arg-max over actions of the Q-value of {@code state}. Ties
     *         broken arbitrarily.
     */
    public A getAction(S state);

    /**
     * 
     * @param action
     * @return The value function {@code V(s) = Q(s,a)}
     */
    public ValueFunction getValue(A action);

}

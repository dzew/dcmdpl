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

import drl.math.geom.Cell;
import drl.math.geom.Vector;

/**
 * A representation of a Markov Decision Process M = <S,A,T,R,gamma>. Instances
 * of this class must be immutable. See the code in mdp.impl.* for exemplary
 * usage.
 * 
 * @author Dawit
 * 
 * @param <S>
 *            Possible states of this MDP.
 * @param <A>
 *            Possible actions in this MDP.
 */
public interface MDP<S extends State, A extends Action> {

    /**
     * @return The start state of this MDP. If the MDP has a probability
     *         distribution over possible start states, this method returns a
     *         draw from that distribution.
     */
    S getStartState();

    /**
     * @return The number of dimensions in the vector representation of a state.
     */
    int getStateDimensions();

    /**
     * The reward associated with a transition (s,a,s').
     * 
     * @param start
     *            the start state, s
     * @param action
     *            the action, a
     * @param end
     *            the end state, s'
     * @return R(s,a,s'). If the transition (s,a,s') has probability zero, this
     *         method may return any arbitrary value. If s' isTerminal, and the
     *         transition (s,a,s') has positive probability, the associated
     *         reward must be zero.
     */
    double getReward(S start, A action, S end);

    /**
     * 
     * @return gamma
     */
    double getDiscountFactor();

    /**
     * 
     * @param s
     * @return {@code true} if and only if s is a terminal state of the MDP.
     */
    boolean isTerminal(S s);

    /**
     * @return An ordered array of actions available in this MDP. The ordering
     *         is the natural enumeration. The action at index {@code i} must
     *         have ordinal {@code i}.
     */
    A[] getActions();

    /**
     * 
     * @param state
     *            the starting state
     * @param action
     *            the action taken
     * @return The state reached when taking {@code action} from {@code state}.
     *         If the result is stochastic, returns a draw from the probability
     *         distribution over next states. Terminal states must
     *         self-transition with probability 1.
     */
    S simulate(S state, A action);

    /**
     * 
     * @param v
     *            the vector representation of a state
     * @return The state whose vector representation is {@code v}. If {@code v}
     *         represents an invalid state, the behavior of this method is
     *         unspecified.
     */
    S stateFromVector(Vector v);

    /**
     * 
     * @param s
     *            a state
     * @return The vector representation of {@code s}.
     */
    Vector vectorFromState(S s);

    /**
     * @return The smallest k-Cell containing the state space of this MDP.
     */
    Cell getStateSpace();

}

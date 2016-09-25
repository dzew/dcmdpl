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
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.State;

/**
 * A sample transition in some MDP.
 * 
 * @author Dawit
 * 
 */
public class Transition<S extends State, A extends Action> {
    private final S startState;
    private final A action;
    private final S endState;
    private final Vector startVector;
    private final Vector endVector;
    private final double reward;

    /**
     * 
     * @param start
     *            The start of the transition
     * @param action
     *            The action taken
     * @param end
     *            The end of the transition
     * @param s
     * @param e
     * @param reward
     *            The reward for the transition
     */
    public Transition(S start, A action, S end, Vector s, Vector e, double reward) {
        this.startState = start;
        this.action = action;
        this.endState = end;
        this.startVector = s;
        this.endVector = e;
        this.reward = reward;
    }

    public static <S extends State, A extends Action> Transition<S, A> of(S start, A action, S end,
            MDP<S, A> mdp) {
        double reward = mdp.getReward(start, action, end);
        Vector s = mdp.vectorFromState(start);
        Vector e = mdp.vectorFromState(end);
        return new Transition<S, A>(start, action, end, s, e, reward);
    }

    public S getStartState() {
        return startState;
    }

    public A getAction() {
        return action;
    }

    public S getEndState() {
        return endState;
    }

    public double getReward() {
        return reward;
    }

    public Vector getStartVector() {
        return startVector;
    }

    public Vector getEndVector() {
        return endVector;
    }

    @Override
    public String toString() {
        return "Trans<" + startState + " " + action + endState + " " + reward + ">";
    }

}

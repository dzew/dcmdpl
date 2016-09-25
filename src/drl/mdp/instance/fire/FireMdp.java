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

package drl.mdp.instance.fire;

import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;

/**
 * Representation of a very simple one-dimensional MDP where if the state is in
 * some interval the agent gets a reward of -1, otherwise it gets reward 0. The
 * agent can stay in place, move left, or move right. The Q-Value for each
 * action, a, is approximately {@code Q_a = lambda v: min(0, abs(x - c1_a) -
 * c2_a )} where c1_a and c2_a are constants depending on the parametrization of
 * the problem.
 * 
 * @author Dawit
 */
public class FireMdp implements MDP<FireState, FireAction> {

    private final Interval fire;
    private final Cell domain;
    private final FireState startState;

    // private final double noise = .49;

    /**
     * An instance of FireMdp where {@code fire} is the interval with reward -1.
     */
    public FireMdp(Interval fire) {
        this.fire = fire;
        this.domain = Cell.of(new Interval(fire.getStart() - .5, fire.getWidth() + 1));
        startState = new FireState(fire.getStart() + fire.getWidth() / 2);
    }

    @Override
    public FireAction[] getActions() {
        return FireAction.values();
    }

    @Override
    public Cell getStateSpace() {
        return domain;
    }

    @Override
    public double getDiscountFactor() {
        return .9;
    }

    @Override
    public double getReward(FireState start, FireAction action, FireState end) {
        if (isTerminal(end)) return 0;
        return -1;
    }

    @Override
    public FireState getStartState() {
        return startState;
    }

    @Override
    public int getStateDimensions() {
        return 1;
    }

    private double expectedResult(double start, FireAction action) {
        return fire.contains(start) ? start + action.impulse : start;
    }

    @Override
    public FireState simulate(FireState state, FireAction action) {
        return new FireState(expectedResult(state.pos, action));
    }

    @Override
    public FireState stateFromVector(Vector v) {
        return new FireState(v.get(0));
    }

    @Override
    public Vector vectorFromState(FireState s) {
        return Vector.asVector(s.pos);
    }

    @Override
    public boolean isTerminal(FireState s) {
        return !fire.contains(s.pos);
    }

}

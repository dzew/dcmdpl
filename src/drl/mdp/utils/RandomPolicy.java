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

import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.State;

/**
 * A Policy that selects actions uniformly at random from the set of possible
 * actions. Note that the policy evaluated multiple times at the same state may
 * give different actions.
 * 
 * @author Dawit
 * 
 */
public class RandomPolicy<S extends State, A extends Action> implements Policy<S, A> {

    private MDP<S, A> mdp;
    private final int actions;

    private RandomPolicy(MDP<S, A> mdp) {
        this.mdp = mdp;
        this.actions = mdp.getActions().length;
    }

    public static <S extends State, A extends Action> RandomPolicy<S, A> of(MDP<S, A> mdp) {
        return new RandomPolicy<S, A>(mdp);
    }

    @Override
    public A getAction(S state) {
        return mdp.getActions()[(int) (actions * Math.random())];
    }

}

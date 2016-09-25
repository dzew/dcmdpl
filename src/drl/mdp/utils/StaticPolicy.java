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
import drl.mdp.api.Policy;
import drl.mdp.api.State;

/**
 * A policy that returns the same action for every state.
 * 
 * @author Dawit
 * 
 */
public class StaticPolicy<S extends State, A extends Action> implements Policy<S, A> {

    private final A action;

    public StaticPolicy(A action) {
        this.action = action;
    }

    public static <S extends State, A extends Action> StaticPolicy<S, A> of(A action) {
        return new StaticPolicy<S, A>(action);
    }

    @Override
    public A getAction(S state) {
        return action;
    }
}

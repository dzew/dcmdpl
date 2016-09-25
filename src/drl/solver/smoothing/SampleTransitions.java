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

import java.util.List;
import java.util.Map;

import drl.mdp.api.Action;
import drl.mdp.api.State;
import drl.mdp.utils.Transition;

/**
 * A container for a list of transitions partitioned by action.
 * 
 * @author Dawit
 * 
 * @param <S>
 * @param <A>
 */
public class SampleTransitions<S extends State, A extends Action> {

    private final Map<A, List<Transition<S, A>>> data;

    /**
     * @param data
     *            The partitioned list of sample transitions. Do not modify
     *            {@code data} after calling this method.
     */
    public SampleTransitions(Map<A, List<Transition<S, A>>> data) {
        this.data = data;
    }

    /**
     * @param action
     * @return A list of transitions associated with {@code action}.
     */
    public List<Transition<S, A>> get(A action) {
        return data.get(action);
    }

}

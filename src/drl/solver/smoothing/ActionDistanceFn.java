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

import java.util.Hashtable;
import java.util.Map;

import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.mdp.api.Action;

/**
 * An action-dependent metric on the state space of an MDP (as described in
 * Dawit and Konidaris, 2014).
 * 
 * @author Dawit
 * 
 * @param <A>
 */
public class ActionDistanceFn<A extends Action> {

    private final Map<A, DistanceFunction> map;

    public ActionDistanceFn(Map<A, DistanceFunction> map) {
        this.map = map;
    }

    public static <A extends Action> ActionDistanceFn<A> of(Map<A, DistanceFunction> map) {
        return new ActionDistanceFn<A>(map);
    }

    public static <A extends Action> ActionDistanceFn<A> of(A[] actions, DistanceFunction df) {
        Map<A, DistanceFunction> map = new Hashtable<A, DistanceFunction>();
        for (A action : actions) {
            map.put(action, df);
        }
        return new ActionDistanceFn<A>(map);
    }

    public DistanceFunction get(A action) {
        return map.get(action);
    }

    public void memoize(Vector v) {
        for (DistanceFunction df : map.values()) {
            df.memoize(v);
        }
    }

}

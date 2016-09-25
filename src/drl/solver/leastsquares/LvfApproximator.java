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

package drl.solver.leastsquares;

import drl.mdp.api.Action;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;
import drl.mdp.api.State;

/**
 * Interface for classes that approximate the value of a given policy for some
 * MDP.
 * 
 * @author Dawit
 * 
 */
public interface LvfApproximator<S extends State, A extends Action> {

    /**
     * @param policy
     *            The policy to be evaluated.
     * @return The Q-values of states under the given policy.
     */
    public QValue<S, A> approximateQValueOf(Policy<S, A> policy);

}

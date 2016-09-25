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

/**
 * A marker interface for states in an MDP. Implementing classes may take any
 * form. It is not recommended that {@code null} be an element of the reachable
 * state space. To check States for equality, use their vector representations.
 * Implementations of this class are not required to override the {@code equals}
 * method.
 * 
 * @author Dawit
 * 
 */
public interface State {

}

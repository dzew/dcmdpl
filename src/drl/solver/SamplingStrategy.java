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

package drl.solver;

/**
 * The set of possible sampling strategies
 * 
 * @author Dawit
 * 
 */
public enum SamplingStrategy {
    /**
     * Select points u.a.r. from the k-cell bounding the state space. The points
     * are sampled independently.
     */
    RANDOM,

    /**
     * Select points that tile the state space. The number of samples generated
     * is the smallest number greater than or equal to desired number of samples
     * that allows for an even tiling of the state space. (i.e. if the state
     * space is d dimensional, the number of samples generated is the smallest
     * power of d greater than or equal to the desired number of samples).
     */
    TILING,

    /**
     * Selects points that uniformly cover the states reachable from the start
     * state of the mdp. Reachability is determined by performing several random
     * walks.
     */
    REACHABLE
}
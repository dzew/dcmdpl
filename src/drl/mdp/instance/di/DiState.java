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

package drl.mdp.instance.di;

import drl.math.geom.Vector;
import drl.mdp.api.State;

/**
 * State of the Double Itegrator MDP.
 * 
 * @author Dawit
 * 
 */
public class DiState implements State {

    final double velocity;
    final double position;
    final Vector stateVector;

    public DiState(double velocity, double position) {
        this.velocity = velocity;
        this.position = position;
        this.stateVector = Vector.asVector(velocity, position);
    }

    public DiState(Vector v) {
        this.velocity = v.get(0);
        this.position = v.get(1);
        this.stateVector = v;
    }

}

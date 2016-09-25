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

package drl.mdp.instance.mtncar;

import drl.math.geom.Vector;
import drl.mdp.api.State;

/**
 * A state in MountainCar.
 * 
 * @author Dawit
 * 
 */
public class CarState implements State {

    public final double x;
    public final double xDot;
    final Vector asVector;

    public CarState(Vector v) {
        this.x = v.get(0);
        this.xDot = v.get(1);
        this.asVector = v;
    }

    public CarState(double x, double xDot) {
        this.x = x;
        this.xDot = xDot;
        this.asVector = Vector.asVector(x, xDot);
    }

    @Override
    public String toString() {
        return String.format("MtCar" + asVector);
    }

}

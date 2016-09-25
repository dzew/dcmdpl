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

package drl.mdp.instance.pinball;

import drl.mdp.api.Action;

/**
 * Action in the PinBall domain.
 * 
 * @author Dawit
 * 
 */
public enum PinBallAction implements Action {
    NOOP(0., 0., "O"), // Do nothing.
    RIGHT(0.2, 0., ">"), // Accelerate right.
    LEFT(-0.2, 0., "<"), // Accelerate left.
    UP(0., -0.2, "n"), // Accelerate up.
    DOWN(0., 0.2, "v"); // Accelerate down.

    final double dx;
    final double dy;
    private final String name;

    private PinBallAction(double dx, double dy, String name) {
        this.dx = dx;
        this.dy = dy;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

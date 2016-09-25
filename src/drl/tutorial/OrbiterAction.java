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

package drl.tutorial;

import drl.mdp.api.Action;

/**
 * Representation of an action in the Orbiter domain.
 * 
 * @author Dawit
 * 
 */
public enum OrbiterAction implements Action {

    // TODO add action that corresponds to firing no thruster.
    IN(-.3, 0., "a"), // Accelerate towards the planet (fire thruster 0).
    OUT(.3, 0., "d"), // Accelerate away from the planet (fire thruster 1).
    FORWARD(0., .3, "w"), // Fire thruster 2.
    BACK(0., -.3, "s"); // Fire thruster 3.

    final double v_norm;
    final double v_tangent;
    private final String name;

    /**
     * @param v1
     *            Thrust magnitude away from planet.
     * @param v2
     *            Thrust magnitude around planet.
     * @param name
     *            The string representation of this action. If you choose a
     *            single letter of the alphabet as the name, you can use that
     *            letter on the keyboard to choose the action when running
     *            InteractiveFrame.
     */
    private OrbiterAction(double v1, double v2, String name) {
        this.v_norm = v1;
        this.v_tangent = v2;
        this.name = name;
    }

    public String toString() {
        return name;
    }
}

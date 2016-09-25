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

package drl.mdp.instance.acrobot;

import drl.mdp.api.Action;

/**
 * The actions in the Acrobot.
 * 
 * @author Dawit
 * 
 */
public enum AcrobotAction implements Action {

    LEFT(-1, ">"), NOOP(0, "o"), RIGHT(1, "<");

    final double torque;
    private final String name;

    private AcrobotAction(int torque, String name) {
        this.torque = torque;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}

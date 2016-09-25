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

import drl.mdp.api.Action;

/**
 * An action in the Mountian-Car MDP.
 * 
 * @author Dawit
 * 
 */
public enum CarAction implements Action {
    LEFT(-1, "<"), NOOP(0, "-"), RIGHT(1, ">");

    final double force;
    final String name;

    private CarAction(double force, String name) {
        this.force = force;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

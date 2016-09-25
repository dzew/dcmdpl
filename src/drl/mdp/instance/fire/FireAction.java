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

package drl.mdp.instance.fire;

import drl.mdp.api.Action;

/**
 * The Actions in FireMdp.
 * 
 * @author Dawit
 * 
 */
public enum FireAction implements Action {

    STAY("o", 0.), LEFT("<", -1.), RIGHT(">", 1.);

    private final String name;
    final double impulse;

    private FireAction(String name, double impulse) {
        this.name = name;
        this.impulse = impulse;
    }

    @Override
    public String toString() {
        return name;
    }

}

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

package drl.mdp.instance.grid;

import drl.mdp.api.Action;

/**
 * Actions in GridMdp.
 * 
 * @author Dawit
 * 
 */
public enum GridAction implements Action {

    RIGHT(">", 1, 0), LEFT("<", -1, 0), UP("^", 0, -1), DOWN("v", 0, 1);

    final String name;
    final int dx;
    final int dy;

    private GridAction(String name, int dx, int dy) {
        this.name = name;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public String toString() {
        return name;
    }

}

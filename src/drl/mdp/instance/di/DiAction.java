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

import drl.mdp.api.Action;

/**
 * Actions in the Double Integrator MDP.
 * 
 * @author Dawit
 * 
 */
public enum DiAction implements Action {

    NOOP(0.0), LEFT_3(-9.), LEFT_2(-4.), LEFT_1(-1.), RIGHT_1(1.), RIGHT_2(4.), RIGHT_3(9.);

    final double impulse;
    final double sqrtImpulse;

    private DiAction(double impulse) {
        this.impulse = impulse;
        this.sqrtImpulse = Math.sqrt(Math.abs(impulse));
    }

    @Override
    public String toString() {
        return (impulse > 0 ? "+" : "") + (int) impulse;
    }

}

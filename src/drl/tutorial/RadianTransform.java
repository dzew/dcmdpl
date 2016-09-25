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

import drl.math.geom.Vector;
import drl.math.tfs.Transform;

public class RadianTransform implements Transform {

    // private final OrbiterMdp mdp;

    public RadianTransform(OrbiterMdp mdp) {
        // this.mdp = mdp;
    }

    @Override
    public Vector transform(Vector v) {
        // OrbiterState s = mdp.stateFromVector(v);

        // double theta = -Math.atan2(s.y, s.x);
        // double radius = Math.sqrt(s.x * s.x + s.y * s.y);
        // double sin = Math.sin(theta);
        // double cos = Math.cos(theta);

        // TODO return [x, y, xDot, yDot] \to [r, rDot, r * thetaDot]
        // where r = radius,
        // rDot = cos * s.xDot - sin * s.yDot, and
        // r * thetaDot = sin * s.xDot + cos * s.yDot);
        throw new RuntimeException("RadianTransform implementation not yet complete");
    }

}

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

import java.awt.Color;
import java.awt.Graphics;

import drl.data.vis.GraphicsUtils;
import drl.mdp.api.MdpVisualizer;

public class OrbiterVisualizer implements MdpVisualizer<OrbiterState> {

    private final OrbiterMdp mdp;

    public OrbiterVisualizer(OrbiterMdp mdp) {
        this.mdp = mdp;
    }

    @Override
    public void render(Graphics g, OrbiterState state) {
        // `scale' is half the width (and height) of the drawing area.
        double scale = .5 * Math.min(g.getClipBounds().height, g.getClipBounds().width);

        // Drawing the celestial sphere.
        g.setColor(Color.BLUE);
        GraphicsUtils.drawCircle(g, scale, scale, scale * mdp.maxRadius);

        // Drawing the goal region.
        g.setColor(Color.black);
        GraphicsUtils.drawCircle(g, scale, scale, scale * (mdp.targetRadius + mdp.tolerance));
        GraphicsUtils.drawCircle(g, scale, scale, scale * (mdp.targetRadius - mdp.tolerance));

        // Drawing the satellite.
        GraphicsUtils.fillCircle(g, (1 + state.x) * scale, (1 + state.y) * scale, 5);
    }

}

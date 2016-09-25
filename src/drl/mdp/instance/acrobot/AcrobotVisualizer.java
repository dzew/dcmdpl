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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import drl.data.vis.GraphicsUtils;

import drl.mdp.api.MdpVisualizer;

/**
 * A Visualizer for the Acrobot MDP.
 * 
 * @author Dawit
 * 
 */
public class AcrobotVisualizer implements MdpVisualizer<AcrobotState> {

    private final AcrobotParams params;

    public AcrobotVisualizer(AcrobotParams params) {
        this.params = params;
    }

    @Override
    public void render(Graphics g, AcrobotState state) {
        g.setColor(Color.black);
        double metersToPixels = .25 * Math.min(g.getClipBounds().height, g.getClipBounds().width);
        double center = 2 * metersToPixels;

        // Drawing two lines to display the velocity of the bot.
        GraphicsUtils.drawHorizontalLine(g, metersToPixels, center, state.theta1Dot * 20);
        GraphicsUtils.drawVerticalLine(g, metersToPixels, center, state.theta2Dot * 20);

        // Drawing the anchor point.
        GraphicsUtils.drawCircle(g, center, center, 5);

        // Drawing the goal line.
        g.setColor(Color.red);
        GraphicsUtils.drawHorizontalLine(g, 0, metersToPixels, 4 * metersToPixels);

        // Drawing the limbs.
        ((Graphics2D) g).setStroke(new BasicStroke(3));
        double x1 = metersToPixels * params.l1 * Math.sin(state.theta1);
        double y1 = metersToPixels * params.l1 * Math.cos(state.theta1);
        GraphicsUtils.drawLine(g, x1 + center, y1 + center, center, center);
        g.setColor(Color.blue);
        double x2 = metersToPixels * params.l2
                * Math.cos(Math.PI / 2 - state.theta1 - state.theta2);
        double y2 = metersToPixels * params.l2
                * Math.sin(Math.PI / 2 - state.theta1 - state.theta2);
        GraphicsUtils.drawLine(g, x1 + center, y1 + center, x1 + x2 + center, y1 + y2 + center);

    }

}

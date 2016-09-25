/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 * Adapted (with modifications) from code by George Konidaris
 * (gdk at csail dot mit dot edu).
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import drl.data.vis.GraphicsUtils;

import drl.mdp.api.MdpVisualizer;

/**
 * A visualizer for PinBall that displays the entire trajectory of the ball.
 * 
 * @author Dawit
 * 
 */
public class PinBallVisualizer implements MdpVisualizer<PinBallState> {

    private final PinBallParams params;
    private final List<Polygon> polygons;
    private static final int scale = 375;
    private final List<PinBallState> path;
    private final boolean show;

    private PinBallVisualizer(PinBallParams params, List<Polygon> polygons, boolean show) {
        this.params = params;
        this.polygons = polygons;
        path = new ArrayList<PinBallState>();
        this.show = show;
    }

    /**
     * Static constructor for {@code PinBallVisualizer}.
     * 
     * @param params
     *            The parameter settings for the PinBall domain.
     * @param showTrajectory
     *            set this flag to {@code true} to view entire trajectories as
     *            opposed to just the current state. If this flag is set to
     *            true, do not call setState or render more than once every
     *            30ms.
     * @return
     */
    public static PinBallVisualizer of(PinBallParams params, boolean showTrajectory) {
        List<Polygon> polygons = new ArrayList<Polygon>();
        for (Obstacle o : params.obstacles) {
            PolygonObstacle p = (PolygonObstacle) o;
            ArrayList<Point> points = p.getPoints();

            int[] xp = new int[points.size()];
            int[] yp = new int[points.size()];

            for (int j = 0; j < points.size(); j++) {
                xp[j] = (int) (points.get(j).getX() * scale);
                yp[j] = (int) (points.get(j).getY() * scale);
            }

            Polygon P = new Polygon(xp, yp, points.size());
            polygons.add(P);
        }
        return new PinBallVisualizer(params, polygons, showTrajectory);
    }

    @Override
    public void render(Graphics graphics, PinBallState state) {
        Graphics2D g2d = (Graphics2D) graphics;
        for (Polygon p : polygons) {
            g2d.setColor(Color.darkGray);
            g2d.fill(p);
            g2d.setColor(Color.black);
            g2d.draw(p);
        }

        path.add(state);
        for (int i = 0; i < path.size(); i++) {
            PinBallState s = path.get(i);
            float hue = .01f * i;
            graphics.setColor(Color.getHSBColor(hue, 1.f, .5f));
            GraphicsUtils.fillCircle(graphics, s.x * scale, s.y * scale, params.ballRadius * scale);
        }

        for (int i = 0; i < path.size(); i++) {
            PinBallState s = path.get(i);
            graphics.setColor(Color.black);
            GraphicsUtils.fillCircle(graphics, s.x * scale, s.y * scale, params.ballRadius * scale
                    / 5);
        }
        graphics.setColor(Color.red);
        GraphicsUtils.drawCircle(graphics, state.x * scale, state.y * scale, params.ballRadius
                * scale);

        graphics.setColor(Color.blue);
        GraphicsUtils.drawCircle(graphics,
                params.targetX * scale,
                params.targetY * scale,
                params.targetRadius * scale);

        if (!show) {
            path.clear();
        }
    }

}

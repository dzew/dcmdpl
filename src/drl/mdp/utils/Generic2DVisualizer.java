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

package drl.mdp.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import drl.math.geom.Vector;
import drl.math.tfs.Normalizer;
import drl.mdp.api.MDP;
import drl.mdp.api.MdpVisualizer;
import drl.mdp.api.State;
import drl.data.vis.GraphicsUtils;

/**
 * A visualizer for arbitrary two-dimensional MDPs.
 * 
 * @author Dawit
 * 
 * @param <S>
 */
public class Generic2DVisualizer<S extends State> implements MdpVisualizer<S> {

    private final MDP<S, ?> mdp;
    private final List<Vector> path;
    private final boolean show;
    private final Normalizer norm;

    private Generic2DVisualizer(MDP<S, ?> mdp, boolean showTrajectory) {
        this.mdp = mdp;
        this.path = new ArrayList<Vector>();
        this.show = showTrajectory;
        this.norm = new Normalizer(mdp.getStateSpace());
    }

    /**
     * @return The length of the trajectory being maintained (if the
     *         showTrajectory flag was set to true).
     */
    public int getTrajectoryLength() {
        return path.size();
    }

    /**
     * Static constructor for Generic2DVisualizer
     * 
     * @param <S>
     * @param mdp
     *            The MDP to be visualized.
     * @param showTrajectory
     *            Set this flag to {@code true} to view slime trails of entire
     *            trajectories as opposed to only the current state.
     */
    public static <S extends State> Generic2DVisualizer<S> of(MDP<S, ?> mdp, boolean showTrajectory) {
        return new Generic2DVisualizer<S>(mdp, showTrajectory);
    }

    @Override
    public synchronized void render(Graphics graphics, S state) {
        path.add(norm.transform(mdp.vectorFromState(state)));
        for (int i = 0; i < path.size(); i++) {
            Vector v = path.get(i);
            float hue = .01f * i;
            graphics.setColor(Color.getHSBColor(hue, 1.f, .5f));
            GraphicsUtils.fillCircle(graphics, 400 * v.get(0), 400 * v.get(1), 5);
        }
        if (!this.show) {
            path.clear();
        }
    }

}

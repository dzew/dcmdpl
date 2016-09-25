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

package drl.data.vis;

import java.awt.Dimension;

import javax.swing.JFrame;

import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.MdpVisualizer;
import drl.mdp.api.State;

/**
 * A simple class for visualizing MDPs.
 * 
 * @author Dawit
 * 
 * @param <S>
 * @param <A>
 */
public class DisplayFrame<S extends State> extends JFrame {

    private static final long serialVersionUID = -5546989468589518698L;

    private final Visualization<S> vis;

    private DisplayFrame(MDP<S, ?> mdp, MdpVisualizer<S> v) {
        vis = new Visualization<S>(v);
        this.setSize(new Dimension(400, 400));
        this.add(vis);
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public static <S extends State, A extends Action> DisplayFrame<S> of(MDP<S, ?> mdp,
            MdpVisualizer<S> v) {
        return new DisplayFrame<S>(mdp, v);
    }

    public void setState(final S state) {
        vis.setState(state);
        repaint();
    }
}

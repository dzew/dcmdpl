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

import java.awt.Graphics;

import javax.swing.JPanel;

import drl.mdp.api.MdpVisualizer;
import drl.mdp.api.State;

class Visualization<S extends State> extends JPanel {

    private static final long serialVersionUID = -9194382460744251234L;

    private final MdpVisualizer<S> visualizer;
    private S state = null;
    private boolean isStateSet = false;

    public Visualization(MdpVisualizer<S> visualizer) {
        this.visualizer = visualizer;
    }

    public void setState(S state) {
        isStateSet = true;
        this.state = state;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isStateSet) {
            visualizer.render(g, state);
        }
    }

}

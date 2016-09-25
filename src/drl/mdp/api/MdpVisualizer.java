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

package drl.mdp.api;

import java.awt.Graphics;

/**
 * A tool for visualizing a Markov Decision process.
 * 
 * @author Dawit
 * 
 * @param <S>
 */
public interface MdpVisualizer<S extends State> {

    /**
     * Draws a representation of {@code state} using {@code graphics}.
     * 
     * @param graphics
     *            where the state is to be rendered.
     * @param state
     *            the state to be visualized.
     */
    public void render(Graphics graphics, S state);

}

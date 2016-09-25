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

package drl.tests.functional;

import java.util.List;

import drl.data.vis.DisplayFrame;
import drl.mdp.instance.mtncar.CarState;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.utils.Generic2DVisualizer;
import drl.mdp.utils.MdpUtils;
import drl.solver.StateSampler;

public class StateSamplerTest {

    public static void main(String[] args) {
        MountainCarMdp mdp = MountainCarMdp.defaultParams();
        List<CarState> samples = StateSampler.reachabilitySample(mdp, 600);

        Generic2DVisualizer<CarState> vis = Generic2DVisualizer.of(mdp, true);
        DisplayFrame<CarState> frame = DisplayFrame.of(mdp, vis);

        for (CarState state : samples) {
            frame.setState(state);
            MdpUtils.sleep(30);
        }
        frame.repaint();
    }

}

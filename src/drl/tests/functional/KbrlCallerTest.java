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

import drl.data.print.PrintUtils;
import drl.data.vis.DisplayFrame;
import drl.mdp.instance.mtncar.CarAction;
import drl.mdp.instance.mtncar.CarState;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.utils.Generic2DVisualizer;
import drl.mdp.utils.MdpUtils;
import drl.solver.KbrlCaller;
import drl.solver.StateSampler;
import drl.solver.smoothing.KbUtils;
import drl.solver.smoothing.KernelQValue;
import drl.solver.smoothing.SampleTransitions;

/**
 * Tests KbrlCaller by solving MountainCar using sustained actions.
 * 
 * @author Dawit
 * 
 */
public class KbrlCallerTest {

    public static void main(String[] args) {
        MountainCarMdp mdp = MountainCarMdp.defaultParams();
        Generic2DVisualizer<CarState> vis = Generic2DVisualizer.of(mdp, false);

        List<CarState> states = StateSampler.reachabilitySample(mdp, 600);
        SampleTransitions<CarState, CarAction> transitions = KbUtils.generateSustainedTransitions(mdp,
                states,
                null,
                50,
                .05);

        KernelQValue<CarState, CarAction> qvf = KbrlCaller.of(mdp)
                .useTransitions(transitions)
                .setRepresentativeStates(StateSampler.tilingSample(mdp, 81))
                .setSteps(200)
                .makeMultithreaded(4)
                .setBandwidth(.04)
                .solve();

        PrintUtils.printValueFunction(mdp, qvf);
        PrintUtils.printPolicy(mdp, qvf);

        MdpUtils.visualize(mdp, DisplayFrame.of(mdp, vis), qvf, null, 300, 30);
    }

}

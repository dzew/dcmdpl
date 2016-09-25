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

import drl.data.vis.InteractiveFrame;
import drl.mdp.instance.acrobot.AcrobotMdp;
import drl.mdp.instance.acrobot.AcrobotParams;
import drl.mdp.instance.acrobot.AcrobotVisualizer;
import drl.mdp.instance.pinball.PinBallMdp;
import drl.mdp.instance.pinball.PinBallParams;
import drl.mdp.instance.pinball.PinBallVisualizer;

public class InteractiveFrameTest {

    public static void main(String[] args) {
        if (Math.random() < .5) {
            testPinBall();
        } else {
            testAcrobot();
        }
    }

    private static void testAcrobot() {
        AcrobotParams params = AcrobotParams.builder()
                .setGoalHeight(.7)
                .setLen1(1.5)
                .setLen2(.4)
                .setMass1(2)
                .build();

        // Alternatively, uncomment the following to use default parameters.
        // params = AcrobotParams.defaultParams();

        AcrobotMdp mdp = new AcrobotMdp(params);
        AcrobotVisualizer vis = new AcrobotVisualizer(params);

        InteractiveFrame.of(mdp, vis);
    }

    private static void testPinBall() {
        PinBallParams params = PinBallParams.fromFile("data/pinball-easy.cfg");
        PinBallMdp mdp = new PinBallMdp(params);
        PinBallVisualizer vis = PinBallVisualizer.of(params, false);

        InteractiveFrame.of(mdp, vis);
    }

}

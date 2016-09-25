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

import drl.data.vis.DisplayFrame;
import drl.mdp.api.Policy;
import drl.mdp.instance.acrobot.AcrobotAction;
import drl.mdp.instance.acrobot.AcrobotMdp;
import drl.mdp.instance.acrobot.AcrobotParams;
import drl.mdp.instance.acrobot.AcrobotState;
import drl.mdp.instance.acrobot.AcrobotVisualizer;
import drl.mdp.utils.MdpUtils;

public class MdpRolloutTest {

    public static void main(String[] args) {
        AcrobotMdp mdp = AcrobotMdp.defaultAcrobot();
        AcrobotVisualizer vis = new AcrobotVisualizer(AcrobotParams.defaultParams());
        MdpUtils.visualize(mdp, DisplayFrame.of(mdp, vis), acrobotSolution, null, 200, 80);
    }

    private static final Policy<AcrobotState, AcrobotAction> acrobotSolution = new Policy<AcrobotState, AcrobotAction>() {
        @Override
        public AcrobotAction getAction(AcrobotState state) {
            return state.theta2Dot < 0 ? AcrobotAction.LEFT : AcrobotAction.RIGHT;
        }
    };

}

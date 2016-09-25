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

import java.util.HashMap;

import drl.data.vis.DisplayFrame;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.MdpVisualizer;
import drl.mdp.api.State;
import drl.mdp.instance.pinball.PinBallMdp;
import drl.mdp.instance.pinball.PinBallParams;
import drl.mdp.instance.pinball.PinBallState;
import drl.mdp.instance.pinball.PinBallVisualizer;
import drl.mdp.utils.MdpUtils;

public class DisplayFrameTest {

    public static void main(String[] args) {
        PinBallParams theta = PinBallParams.fromFile("data/pinball-hard.cfg");
        PinBallMdp mdp = new PinBallMdp(theta);

        // The action sequences (starting from the default start state) for
        // KBRL, DKBRL iteration 1, and DKBRL iteration 2. Taken from the
        // experiment in the technical report Dawit and Konidaris 2015.
        String[] actions = new String[] {
                ">nn>nOOOOOOOOOOnnOOOO>nOOvOOOOOOOOOOOnOOOOOOOO>n>>><OnnOOOOOOOOOOOOOOOOOnvnOOOOOOOOOO><><vv<nOOOOO<OOOOOOOOvOOOOOOO>OnOOO>OOOvOOOOOOOOOOOOO>OOOOOOOOOOOOOOOOOOOOOOOOOO>OOnOOOOOOOOOOOnOOOOOOOOOOOOOOOOOOOOnnnnOn<nOvnvOOOOOOOOOOOOOOOO>>n>nOOOvvOOOOOOOOOO<OOOOOOOOOOO>OOOOO>OOOOOOnOOO>OOOOOOOOOnOOvOOOOOOOOOOOOOOOOOOn<OOOOOOOOOOv<<><>><><><><><>OOOOOOOOOOOOOOO>>>>nOOOOOOOOOnOOOOOOOOOOOO>OOOOOOOOO>OOO>OOn>OOOOOOOvOOOOOOOvvvO<<O<<<nOnO<OOOO<n><<<nOOOOOOOOnnOOnOOOOOOOOOOO><OOOnnv<vOOOOOOOOOOOOOOOOOO<<",
                "nnnnOOOOOOOnnOOOOOOOOnOOOOOOO>OnnOOOOOOO>>>>>O<nO>OOOOOOOOOOOOO<<OOOOO><nOOOOOOOOvOOOOOOOOOOOOOOOnv<><><>vv>>>vv>nOOOOnOOOOOnnOOOOOnnnvOOOOO<nOOOOOOOOOOOOOOOOnOOOOO>>OO>OOOn>OOOOOvvOnnOOOvnOOOOOOOOOOOOOOOOOvnOOOOOnOO<OOOOO>OOOOvOOOOOOOOOOOOOO",
                "nnnnOOOOOOOnnOOOOOnOOOOOOOOOO>OOOnOOOOOOOO>>OOOOO<<OOO>nvOOOOOO>vnnOOOOOOOOOv><nvvOOOO>>v<OOOOOOOOOO<<OOOOOOOOOOO<><nvn<OOOOOOO<nOOOOOOOOOOOO>v><<nOOnOO<OOnOOOOOOOOOOOn<OOn<><v><O><><>>vv>OOOOOOOOOOOOOOOnnOOOOvvOOOOOOOOOOOOOOOnOOOOOOOOOOnOOOOOOO>>OOOOOOOO<><vvvOOOOOO<OO<vOOvOOOO<v<OOOO>O><OOOOOOv<OOOOOnOOOOOOOOOOO<OOOOOvnO><O><O<OOOOO<Ov<nOOvOOOvnnOvv<vOOOOOOOOOOO<nvOO<v<OOOv<OOOOv<OO<vnOO<OOOO<v><OOOOvO<OvOOOOOOOOOOOOOO" };
        PinBallState startState = mdp.getStartState();

        for (int i = 0; i < actions.length; i++) {
            play(mdp, startState, actions[i], PinBallVisualizer.of(theta, true), "Iteration " + i);
        }
    }

    /**
     * Play a sequence of actions on some deterministic MDP.
     */
    private static <S extends State, A extends Action> void play(MDP<S, A> mdp, S start,
            String actionSeq, MdpVisualizer<S> vis, String title) {
        HashMap<String, A> map = new HashMap<String, A>();
        for (A action : mdp.getActions()) {
            map.put(action.toString(), action);
        }

        DisplayFrame<S> frame = DisplayFrame.of(mdp, vis);
        frame.setTitle(title);
        for (int j = 0; j < actionSeq.length(); j++) {
            A action = map.get(actionSeq.substring(j, j + 1));
            start = mdp.simulate(start, action);
            System.out.println(j + " " + start + " " + action);
            MdpUtils.sleep(50);
            frame.setState(start);
        }
    }
}

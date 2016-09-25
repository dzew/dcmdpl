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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

import drl.math.geom.Vector;
import drl.mdp.api.Action;
import drl.mdp.api.MDP;
import drl.mdp.api.MdpVisualizer;
import drl.mdp.api.State;

/**
 * A class for testing implementations of MDPs and visualizers. It provides a
 * GUI for seeing the MDP in different states, rolling out actions, and
 * observing the transitions / rewards.
 * 
 * @author Dawit
 * 
 * @param <S>
 * @param <A>
 */
public class InteractiveFrame<S extends State, A extends Action> extends JFrame {

    private static final long serialVersionUID = 3671512449584964502L;

    private final MDP<S, A> mdp;
    private final Visualization<S> visn;

    private final JButton help = new JButton("     Help      ");
    private final JButton reset = new JButton("   Reset     ");
    private final JButton setState = new JButton("Set State");
    private final JButton step = new JButton("    Start   ");
    private final JTextPane log = new JTextPane();
    private final JTextField[] currentStates;
    private final S startState;
    private S state;
    double reward = 0;
    int time = 0;
    private Map<Integer, A> keymap;
    private static Map<String, Integer> arrows = cacheKeys();

    /**
     * InteractiveFrame constructor
     * 
     * @param mdp
     *            The MDP to be tested.
     * @param vis
     *            A visualizer for the MDP
     * @param state
     *            The initial state. Set this to null to use mdp.getStartState()
     *            on every reset.
     */
    private InteractiveFrame(MDP<S, A> mdp, MdpVisualizer<S> vis, S state) {
        this.mdp = mdp;
        this.startState = state;
        state = state == null ? mdp.getStartState() : state;
        this.state = state;
        this.visn = new Visualization<S>(vis);
        this.visn.setState(state);
        this.setSize(580, 550);
        this.setTitle("Interactive MDP Visualizer");

        keymap = new HashMap<Integer, A>();
        for (A action : mdp.getActions()) {
            keymap.put(KeyEvent.VK_0 + action.ordinal(), action);
            keymap.put(KeyEvent.VK_NUMPAD0 + action.ordinal(), action);
            String s = action.toString();
            if (s.length() == 1 && Character.isLetter(s.charAt(0))) {
                keymap.put((int) s.toUpperCase().charAt(0), action);
            }
            if (arrows.containsKey(s)) {
                keymap.put(arrows.get(s), action);
            }
        }
        currentStates = new JTextField[mdp.getStateDimensions()];
        this.setContentPane(makeContentPane());
        createListeners();
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        step.requestFocusInWindow();
        this.repaint();
        this.setVisible(true);
    }

    /**
     * Static constructor for InteractiveFrame
     * 
     * @param mdp
     *            The MDP to be tested
     * @param vis
     *            The visualizer for the MDP
     * @param state
     *            The initial state to use. Clicking reset returns the MDP to
     *            this state.
     * @return
     */
    public static <S extends State, A extends Action> InteractiveFrame<S, A> of(MDP<S, A> mdp,
            MdpVisualizer<S> vis, S state) {
        return new InteractiveFrame<S, A>(mdp, vis, state);
    }

    /**
     * Static constructor for InteractiveFrame. Uses mdp.getStartState() as the
     * initial state.
     * 
     * @param mdp
     *            The MDP to be tested
     * @param vis
     *            The visualizer for the MDP
     * @return
     */
    public static <S extends State, A extends Action> InteractiveFrame<S, A> of(MDP<S, A> mdp,
            MdpVisualizer<S> vis) {
        return new InteractiveFrame<S, A>(mdp, vis, null);
    }

    // Select action names get mapped to arrow keys.
    private static Map<String, Integer> cacheKeys() {
        Map<String, Integer> ret = new HashMap<String, Integer>();
        String[] up = new String[] { "^", "n", "UP", "NORTH" };
        String[] down = new String[] { "u", "v", "DOWN", "SOUTH" };
        String[] left = new String[] { "<", "LEFT", "EAST" };
        String[] right = new String[] { ">", "RIGHT", "WEST" };
        String[] noop = new String[] { "-", "=", "o", "O", "STAY", "IDLE", "NOOP" };

        for (String s : up) {
            ret.put(s, KeyEvent.VK_UP);
        }
        for (String s : down) {
            ret.put(s, KeyEvent.VK_DOWN);
        }
        for (String s : left) {
            ret.put(s, KeyEvent.VK_LEFT);
        }
        for (String s : right) {
            ret.put(s, KeyEvent.VK_RIGHT);
        }
        for (String s : noop) {
            ret.put(s, KeyEvent.VK_SPACE);
        }

        return ret;
    }

    // Creates the content pane for the entire GUI.
    private JPanel makeContentPane() {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pane.setTopComponent(makeTopHalf());
        StyleConstants.setForeground(log.addStyle("Plain", null), Color.BLACK);
        log.setEditable(false);
        pane.setBottomComponent(new JScrollPane(log));
        pane.setDividerLocation(400);
        pane.setDividerSize(0);
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(pane, BorderLayout.CENTER);
        return content;
    }

    // Creates the top half of the GUI (visualizer, buttons, and text fields)
    private JPanel makeTopHalf() {
        JPanel ret = new JPanel();
        ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
        JSplitPane split = new JSplitPane();
        visn.setSize(new Dimension(400, 400));
        split.setLeftComponent(visn);
        split.setRightComponent(makeTopRight());
        split.setDividerLocation(400);
        split.setDividerSize(0);
        ret.add(split, BorderLayout.CENTER);
        return ret;
    }

    // Arranges the buttons and text fields.
    private JPanel makeTopRight() {
        JPanel current = new JPanel();
        current.add(new JLabel("Current State Vector"));
        current.setLayout(new BoxLayout(current, BoxLayout.Y_AXIS));
        Vector v = mdp.vectorFromState(state);
        for (int i = 0; i < mdp.getStateDimensions(); i++) {
            currentStates[i] = new JTextField("" + v.get(i));
            current.add(currentStates[i]);
        }
        for (int i = mdp.getStateDimensions(); i < 11; i++) {
            current.add(new JLabel(" "));
        }

        JPanel action = new JPanel();
        action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));
        action.add(setState);
        action.add(new JLabel(" "));
        action.add(new JLabel("Click start to "));
        action.add(new JLabel("simulate the MDP."));
        action.add(help);
        action.add(reset);
        action.add(step);

        JPanel ret = new JPanel();
        JScrollPane jsp = new JScrollPane(current);
        jsp.setPreferredSize(new Dimension(150, 240));
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(jsp);
        split.setBottomComponent(action);
        split.setDividerSize(0);
        ret.add(split, BorderLayout.CENTER);
        return ret;
    }

    private void setState(final S s) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                state = s;
                Vector v = mdp.vectorFromState(s);
                for (int i = 0; i < mdp.getStateDimensions(); i++) {
                    currentStates[i].setText("" + v.get(i));
                }
                visn.setState(s);
                appendToLog(mdp.vectorFromState(s).toString());
                repaint();
            }
        });
    }

    private void appendToLog(String str) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(),
                    str + "\n",
                    log.getStyle("Plain"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (BadLocationException e) {
            System.err.println("Unable to add to log: " + str);
        }
    }

    private void showHelp() {
        JTextPane help = new JTextPane();
        help.setText("Click on \"start\" to activate the keyboard listener"
                + " then choose actions using the keyboard. The available actions are: "
                + Arrays.toString(mdp.getActions())
                + ".These actions are mapped to keys as follows: " + keyMappingText());
        help.setEditable(false);
        JFrame frame = new JFrame();
        frame.setSize(370, 200);
        frame.add(help);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private String keyMappingText() {
        StringBuilder builder = new StringBuilder();
        StringBuilder[] reverseMap = new StringBuilder[mdp.getActions().length];
        for (int i = 0; i < reverseMap.length; i++) {
            reverseMap[i] = new StringBuilder();
        }
        for (int i : keymap.keySet()) {
            reverseMap[keymap.get(i).ordinal()].append(KeyEvent.getKeyText(i) + ", ");
        }
        for (A action : mdp.getActions()) {
            builder.append("\"" + action + "\"" + ": [" + reverseMap[action.ordinal()] + "] ");
        }
        return builder.toString();
    }

    private void createListeners() {
        setState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                double[] d = new double[mdp.getStateDimensions()];
                for (int i = 0; i < d.length; i++) {
                    d[i] = Double.parseDouble(currentStates[i].getText());
                }
                Vector v = Vector.asVector(d);
                if (!mdp.getStateSpace().contains(v)) {
                    System.err.println("Invalid state: " + v);
                } else {
                    time = 0;
                    reward = 0.;
                    appendToLog("SETTING state to");
                    setState(mdp.stateFromVector(v));
                }
                step.requestFocusInWindow();
            }
        });
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                appendToLog("RESETTING state to");
                time = 0;
                reward = 0.;
                setState(startState == null ? mdp.getStartState() : startState);
                step.requestFocusInWindow();
            }
        });
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showHelp();
            }
        });
        step.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int i = e.getKeyCode();
                if (keymap.containsKey(i)) {
                    A action = keymap.get(i);
                    appendToLog("Taking action: " + action);
                    S s = mdp.simulate(state, action);
                    double r = mdp.getReward(state, action, s);
                    reward += Math.pow(mdp.getDiscountFactor(), time) * r;
                    appendToLog(String.format("Time: %d, Reward: %.3f, Return: %.3f",
                            time,
                            r,
                            reward));
                    time++;
                    setState(s);
                    if (mdp.isTerminal(s)) {
                        appendToLog("Reached Terminal State");
                    }
                }
            }
        });

        step.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent arg0) {
                step.setText("Use Keyboard");
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                step.setText("    Start   ");
            }

        });
    }

}

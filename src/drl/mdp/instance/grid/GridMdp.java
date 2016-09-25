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

package drl.mdp.instance.grid;

import java.util.ArrayList;
import java.util.List;

import drl.math.MathUtils;
import drl.math.geom.Cell;
import drl.math.geom.Vector;
import drl.mdp.api.MDP;
import drl.mdp.api.Policy;
import drl.mdp.api.QValue;

/**
 * Representation of a simple grid world MDP. The terminal state is (0, 0).
 * 
 * @author Dawit
 * 
 */
public class GridMdp implements MDP<GridCell, GridAction> {

    private final GridCell[][] world;
    private final Vector[][] cellVectors;
    private final List<GridCell> states;
    private final int size;
    private final Cell domain;

    public GridMdp(int size) {
        this.size = size;
        world = new GridCell[size][size];
        cellVectors = new Vector[size][size];
        states = new ArrayList<GridCell>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                world[i][j] = new GridCell(i, j);
                // cellVectors[i][j] = Vector.asVector(i * size + j + .3);
                cellVectors[i][j] = Vector.indicator(i * size + j, size * size);
                states.add(world[i][j]);
            }
        }
        // this.domain = MathUtils.regularCell(1, -1, size * size + 2);
        this.domain = MathUtils.regularCell(size * size, -.1, 1.1);
    }

    public List<GridCell> getStates() {
        return new ArrayList<GridCell>(states);
    }

    @Override
    public Cell getStateSpace() {
        return domain;
    }

    @Override
    public GridAction[] getActions() {
        return GridAction.values();
    }

    @Override
    public double getDiscountFactor() {
        return .99;
    }

    @Override
    public double getReward(GridCell start, GridAction action, GridCell end) {
        return (end == world[0][0]) ? 0 : -1;
    }

    @Override
    public GridCell getStartState() {
        return world[0][size - 1];
    }

    @Override
    public int getStateDimensions() {
        return states.size();
    }

    private int clip(int n) {
        if (n < 0) {
            return 0;
        }
        return n >= size ? size - 1 : n;
    }

    @Override
    public GridCell simulate(GridCell state, GridAction action) {
        if (state == world[0][0]) {
            return state;
        }
        int x = clip(state.x + action.dx);// + random.nextInt(3) - 1);
        int y = clip(state.y + action.dy);// + random.nextInt(3) - 1);
        return world[x][y];
    }

    @Override
    public GridCell stateFromVector(Vector v) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (v.get(size * i + j) > .5) {
                    return world[i][j];
                    // TODO when state vector is not an indicator?
                }
            }
        }
        throw new IllegalArgumentException("Vector represents invalid state: " + v);
    }

    @Override
    public Vector vectorFromState(GridCell s) {
        return cellVectors[s.x][s.y];
    }

    /**
     * @param policy
     * @return A string representing {@code policy}.
     */
    public String representPolicy(Policy<GridCell, GridAction> policy) {
        StringBuilder sb = new StringBuilder(size * (size + 1));
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                sb.append(policy.getAction(world[x][y]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @param qValues
     * @return A String representing the given Q-values.
     */
    public String representValues(QValue<GridCell, GridAction> qValues) {
        StringBuilder sb = new StringBuilder(6 * size * (size + 1));
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                sb.append(String.format("%.3f ", qValues.getValue(world[x][y],
                        qValues.getAction(world[x][y]))));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean isTerminal(GridCell s) {
        return s == world[0][0];
    }

}
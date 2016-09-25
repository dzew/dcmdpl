/*
 * Copyright 2014 Dawit Zewdie (dawit at alum dot mit dot edu)
 * Adapted (with modifications) from code by George Konidaris
 * (gdk at csail dot mit dot edu).
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

package drl.mdp.instance.pinball;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for parameter settings of the PinBall domain.
 * 
 * @author Dawit
 * 
 */
public class PinBallParams {

    final double ballRadius;
    final double startX;
    final double startY;
    final List<Obstacle> obstacles;
    final double targetX;
    final double targetY;
    final double targetRadius;
    final double drag;

    public PinBallParams(double ballRadius, double startX, double startY, List<Obstacle> obstacles,
            double targetX, double targetY, double targetRadius, double drag) {
        this.ballRadius = ballRadius;
        this.startX = startX;
        this.startY = startY;
        this.obstacles = obstacles;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRadius = targetRadius;
        this.drag = drag;
    }

    /**
     * @param filename
     *            A path to a file containing parameter settings.
     * @return A PinBallParams object constructed from reading the contents of
     *         the given file.
     */
    public static PinBallParams fromFile(String filename) {
        List<String> lines = readFile(filename);
        List<Obstacle> obstacles = new ArrayList<Obstacle>();
        double ballRadius = -1;
        double targetX = -1;
        double targetY = -1;
        double targetRad = -1;
        double startX = -1;
        double startY = -1;
        double drag = .995;

        for (String line : lines) {
            if (PolygonObstacle.matchTag(line)) {
                obstacles.add(PolygonObstacle.create(line));
            } else if (line.startsWith("ball")) {
                ballRadius = Double.parseDouble(line.split(" ")[1]);
            } else if (line.startsWith("drag")) {
                drag = Double.parseDouble(line.split(" ")[1]);
            } else if (line.startsWith("target")) {
                String[] tokens = line.split(" ");
                targetX = Double.parseDouble(tokens[1]);
                targetY = Double.parseDouble(tokens[2]);
                targetRad = Double.parseDouble(tokens[3]);
            } else if (line.startsWith("start")) {
                String[] tokens = line.split(" ");
                startX = Double.parseDouble(tokens[1]);
                startY = Double.parseDouble(tokens[2]);
            }
        }
        if (ballRadius < 0.) {
            throw new RuntimeException("Ball not specified in " + filename + ". " + ballRadius);
        }
        if (targetRad < 0. || targetX < 0. || targetY < 0.) {
            throw new RuntimeException("Target not specified in " + filename + ". " + targetRad
                    + " " + targetX + " " + targetY);
        }
        if (startX < 0. || startY < 0.) {
            throw new RuntimeException("Start not specified in " + filename + ". " + startX + " "
                    + startY);
        }

        return new PinBallParams(ballRadius,
                startX,
                startY,
                obstacles,
                targetX,
                targetY,
                targetRad,
                drag);
    }

    private static List<String> readFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            List<String> ret = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                ret.add(line.trim());
                line = reader.readLine();
            }
            return ret;
        } catch (IOException e) {
            System.err.println("Error reading pinball configs: " + filename);
            throw new RuntimeException(e);
        }
    }

}

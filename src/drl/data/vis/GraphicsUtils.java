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

/**
 * A static utility class providing convenience methods for creating
 * visualizations.
 * 
 * @author Dawit
 * 
 */
public class GraphicsUtils {

    private GraphicsUtils() {
    }

    /**
     * Draw a line from (x1, y1) to (x2, y2).
     */
    public static void drawLine(Graphics g, double x1, double y1, double x2, double y2) {
        g.drawLine((int) (.5 + x1), (int) (.5 + y1), (int) (.5 + x2), (int) (.5 + y2));
    }

    /**
     * Draw a line from (xLeft, y) to (xLeft + width, y).
     */
    public static void drawHorizontalLine(Graphics g, double xLeft, double y, double width) {
        drawLine(g, xLeft, y, xLeft + width, y);
    }

    /**
     * Draw a line from (x, yTop) to (x, yTop + height).
     */
    public static void drawVerticalLine(Graphics g, double x, double yTop, double height) {
        drawLine(g, x, yTop, x, yTop + height);
    }

    /**
     * Draw a circle with the given radius and center.
     */
    public static void drawCircle(Graphics g, double centerX, double centerY, double radius) {
        g.drawOval((int) (.5 + centerX - radius),
                (int) (.5 + centerY - radius),
                (int) (.5 + 2 * radius),
                (int) (.5 + 2 * radius));
    }

    /**
     * Fill a circle with the given radius and center.
     */
    public static void fillCircle(Graphics g, double centerX, double centerY, double radius) {
        g.fillOval((int) (.5 + centerX - radius),
                (int) (.5 + centerY - radius),
                (int) (.5 + 2 * radius),
                (int) (.5 + 2 * radius));
    }

    /**
     * Fill a square at the given coordinates
     * 
     * @param g
     * @param cornerX
     *            x-coordinate of the top left corner
     * @param cornerY
     *            y-coordinate of the top left corner
     * @param width
     *            the width of the square
     */
    public static void fillSquare(Graphics g, double cornerX, double cornerY, double width) {
        g.fillRect((int) (.5 + cornerX),
                (int) (.5 + cornerY),
                (int) (.5 + width),
                (int) (.5 + width));
    }

    /**
     * Draw a square at the given coordinates
     * 
     * @param g
     * @param cornerX
     *            x-coordinate of the top left corner
     * @param cornerY
     *            y-coordinate of the top left corner
     * @param width
     *            the width of the square
     */
    public static void drawSquare(Graphics g, double cornerX, double cornerY, double width) {
        g.drawRect((int) (.5 + cornerX),
                (int) (.5 + cornerY),
                (int) (.5 + width),
                (int) (.5 + width));
    }

}

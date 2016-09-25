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

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Obstacle class for polygonal obstacles. This is the only type of obstacle
 * implemented at the moment.
 * 
 * @author George Konidaris (gdk at cs dot umass dot edu) with modifications by
 *         Dawit.
 */
public class PolygonObstacle implements Obstacle {
    /**
     * Constructor.
     * 
     * @param points
     *            ArrayList of Points to create the obstacle with.
     */
    PolygonObstacle(ArrayList<Point> points) {
        Points = points;
        double max_x = 0;
        double max_y = 0;
        double min_x = 1;
        double min_y = 1;

        for (Point p : Points) {
            max_x = Math.max(max_x, p.getX());
            max_y = Math.max(max_y, p.getY());
            min_x = Math.min(min_x, p.getX());
            min_y = Math.min(min_y, p.getY());
        }

        this.max_x = max_x;
        this.max_y = max_y;
        this.min_x = min_x;
        this.min_y = min_y;
    }

    /**
     * Selected between two edges if the ball hits an edge point.
     * 
     * @param a
     *            the first edge number
     * @param b
     *            the second edge number
     * @param ball
     *            the ball
     * @return the selected edge number
     */
    protected int selectIntercept(int a, int b, Ball ball) {
        int anext = a + 1;
        if (anext == Points.size())
            anext = 0;

        int bnext = b + 1;
        if (bnext == Points.size())
            bnext = 0;

        Point a_edge = Points.get(a).minus(Points.get(anext));
        Point b_edge = Points.get(b).minus(Points.get(bnext));

        Point ball_v = new Point(ball.getXDot(), ball.getYDot());

        double th_a = ball_v.angleBetween(a_edge);
        if (th_a > Math.PI)
            th_a -= Math.PI;

        double th_b = ball_v.angleBetween(b_edge);
        if (th_b > Math.PI)
            th_b -= Math.PI;

        double a_dist = Math.abs(th_a - (Math.PI / 2.0));
        double b_dist = Math.abs(th_b - (Math.PI / 2.0));

        if (a_dist < b_dist)
            return a;
        return b;
    }

    /**
     * Determine whether or not the ball intersects the line between two given
     * points.
     * 
     * @param ball
     *            the ball
     * @param p1
     *            point 1
     * @param p2
     *            point 2
     * @return the intersection point if there is a line intersection, null
     *         otherwise.
     */
    protected Point lineIntersect(Ball ball, Point p1, Point p2) {
        Point dir = p2.minus(p1);
        Point diff = ball.getCenter().minus(p1);

        double t = diff.dot(dir) / dir.dot(dir);

        if (t < 0.0)
            t = 0.0;
        if (t > 1.0)
            t = 1.0;

        Point closest = p1.add(dir.times(t));
        Point d = ball.getCenter().minus(closest);

        Point intercept = closest;

        Point rev = ball.getCenter().minus(intercept);
        rev = rev.normalize();
        rev = rev.times(ball.getVelocity());

        double distsqrt = d.dot(d);

        if (distsqrt <= ball.getRadius() * ball.getRadius()) {
            Point direction = new Point(ball.getXDot(), ball.getYDot());
            Point dd = closest.minus(ball.getCenter());
            double thet = dd.angleBetween(direction);

            if (thet > Math.PI)
                thet = 2 * Math.PI - thet;

            // Make sure the ball is not already heading away
            // from the obstacle
            if (thet > Math.PI / 1.99)
                return null;
            else
                return intercept;
        } else
            return null;
    }

    /**
     * Determine whether a given config input text line matches a polygon
     * obstacle.
     * 
     * @param line
     *            the input line
     * @return <code>true</code> if there's a match, <code>false</code> if not
     */
    public static boolean matchTag(String line) {
        if (line.startsWith("polygon"))
            return true;
        return false;
    }

    /**
     * Create a polygon obstacle from a line of configuration file text.
     * 
     * @param line
     *            the line
     * @return the resulting polygon obstacle
     */
    public static PolygonObstacle create(String line) {
        StringTokenizer toks = new StringTokenizer(line);
        toks.nextToken();
        ArrayList<Point> p = new ArrayList<Point>();

        while (toks.hasMoreTokens()) {
            double x = Double.parseDouble(toks.nextToken());
            double y = Double.parseDouble(toks.nextToken());

            p.add(new Point(x, y));
        }

        return new PolygonObstacle(p);
    }

    /**
     * Get the Point variables making up the obstacle.
     * 
     * @return an ArrayList of Points
     */
    public ArrayList<Point> getPoints() {
        return Points;
    }

    /**
     * Determine whether a point lies within the polygon.
     * 
     * @param p
     *            the test point
     * @return <code>true</code> if <code>p</code> lies within the obstacle,
     *         <code>false</code> otherwise
     */
    public boolean inside(Point p) {
        double testx = p.getX();
        double testy = p.getY();

        int nvert = Points.size();

        int i, j;
        boolean c = false;

        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            if (((Points.get(i).getY() > testy) != (Points.get(j).getY() > testy))
                    && (testx < (Points.get(j).getX() - Points.get(i).getX())
                            * (testy - Points.get(i).getY())
                            / (Points.get(j).getY() - Points.get(i).getY()) + Points.get(i).getX()))

                c = !c;
        }

        return c;
    }

    final double max_x, max_y, min_x, min_y;

    ArrayList<Point> Points;

    @Override
    public Point checkCollision(Ball b) {
        boolean found = false;
        boolean double_collision = false;
        int intercept_edge = -1;

        if (b.getX() - b.getRadius() > max_x)
            return null;
        if (b.getY() - b.getRadius() > max_y)
            return null;
        if (b.getX() + b.getRadius() < min_x)
            return null;
        if (b.getY() + b.getRadius() < min_y)
            return null;

        for (int j = 0; j < Points.size(); j++) {
            int next = j + 1;
            if (next == Points.size())
                next = 0;

            Point intersect = lineIntersect(b, Points.get(j), Points.get(next));
            if (intersect != null) {
                if (found) {
                    intercept_edge = selectIntercept(intercept_edge, j, b);
                    double_collision = true;
                } else {
                    intercept_edge = j;
                    found = true;
                }
            }
        }
        if (!found) {
            return null;
        }

        if (double_collision) {
            return new Point(-b.getXDot(), -b.getYDot());
        }

        int edge2 = intercept_edge + 1;
        if (edge2 == Points.size())
            edge2 = 0;

        // Edge_dir is from left to right.
        Point edge_dir = Points.get(intercept_edge).minus(Points.get(edge2));
        if (edge_dir.getX() < 0)
            edge_dir = Points.get(edge2).minus(Points.get(intercept_edge));

        Point ball_v = new Point(b.getXDot(), b.getYDot());

        double theta = ball_v.angleBetween(edge_dir);

        // Rotate 180 degrees
        theta -= Math.PI;
        if (theta < 0)
            theta += (Math.PI * 2.0);

        // Adjust for the rotation of the obstacle line
        double edge_theta = (new Point(-1, 0)).angleBetween(edge_dir);
        theta += edge_theta;
        if (theta > Math.PI * 2.0)
            theta -= Math.PI * 2.0;

        return new Point(b.getVelocity() * Math.cos(theta), b.getVelocity() * Math.sin(theta));

    }
}

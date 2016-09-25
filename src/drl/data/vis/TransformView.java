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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import drl.math.geom.Vector;
import drl.math.tfs.DistanceFunction;
import drl.math.tfs.EuclideanDF;

/**
 * A class for visualizing a transform. It takes a List of Vectors, an adjacency
 * list representation of the graph and a metric then shows the transform. To
 * see the visualization, place the TransformView in a JFrame.
 * 
 * @author Dawit
 * 
 */
public class TransformView extends JPanel {

    private static final long serialVersionUID = 1L;
    private final List<Vector> vecs;
    private final List<Set<Integer>> graph;
    private final DistanceFunction df;

    public TransformView(List<Vector> vecs, List<Set<Integer>> graph, DistanceFunction df) {
        this.vecs = vecs;
        this.graph = graph;
        this.df = df;
    }

    @Override
    public void paintComponent(Graphics g) {
        int width = 400;// g.getClipBounds().width;
        int height = 400;// g.getClipBounds().height;
        g.drawLine(1, 1, 3, 3);
        ((Graphics2D) g).setStroke(new BasicStroke(4.4f));
        for (int i = 0; i < vecs.size(); i++) {
            Vector vi = vecs.get(i);
            for (int j : graph.get(i)) {
                if (j < i) {
                    continue;
                }
                Vector vj = vecs.get(j);
                double diff = df.distance(vi, vj) / EuclideanDF.instance.distance(vi, vj);
                if (diff > 1) {
                    diff = 1. / diff;
                    int r = (int) (255. - 255 * Math.pow((diff), 3.));
                    g.setColor(new Color(r, 0, 0));
                } else {
                    int b = (int) (255. - 255 * Math.pow((diff), 3.));
                    g.setColor(new Color(0, 0, b));
                }
                GraphicsUtils.drawLine(g,
                        vi.get(0) * width,
                        vi.get(1) * width,
                        vj.get(0) * height,
                        vj.get(1) * height);
            }
        }
    }

}

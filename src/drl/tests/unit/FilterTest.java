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

package drl.tests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import drl.math.MathUtils;
import drl.math.algs.GridFilter;
import drl.math.geom.Cell;
import drl.math.geom.Interval;
import drl.math.geom.Vector;
import drl.mdp.instance.mtncar.MountainCarMdp;
import drl.mdp.instance.mtncar.MtnCarParams;

public class FilterTest {

    private void addCorners(Collection<Vector> v, Cell domain) {
        double[] start = new double[domain.getDimensions()];
        double[] end = new double[domain.getDimensions()];
        for (int i = 0; i < start.length; i++) {
            start[i] = domain.getInterval(i).getStart();
            end[i] = domain.getInterval(i).getEnd();
        }
        v.add(Vector.asVector(start));
        v.add(Vector.asVector(end));
    }

    @Test
    public void testIndexing() {
        final MountainCarMdp mdp = new MountainCarMdp(MtnCarParams.defaultMtnCar());
        final GridFilter filter = new GridFilter(mdp.getStateSpace(), 5);
        System.out.println();
        Interval i1 = mdp.getStateSpace().getInterval(0);
        Interval i2 = mdp.getStateSpace().getInterval(1);
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / 30) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / 30) {
                System.out.print(filter.getIndex(Vector.asVector(pos, vel)) + "  ");
            }
            System.out.println();
        }
    }

    @Test
    public void testGetNbr() {
        Cell domain = Cell.of(new Interval(5, 2.3), new Interval(-.8, 2.1), new Interval(9, 2.4));
        GridFilter filter = new GridFilter(domain, 10);
        List<Vector> added = new ArrayList<Vector>();

        for (int i = 0; i < 12000; i++) {
            Vector v = MathUtils.sampleUniformly(domain);
            if (v.get(1) < 0) {
                continue;
            }
            filter.add(v);
            added.add(v);
        }
        System.out.println("Num added: " + added.size());
        for (int i = 0; i < 10; i++) {
            Vector v = Vector.asVector(7, 0, 8.9);// MathUtils.sampleUniformly(domain);
            Set<Vector> a = new HashSet<Vector>();
            a.addAll(filter.getNeighbors(v, .2));
            int num = 0;
            for (Vector v2 : added) {
                if (MathUtils.squaredDistance(v, v2) < .04) {
                    num++;
                    if (!a.contains(v2)) {
                        System.out.println(v + "  " + filter.getIndex(v) + v2 + "  "
                                + filter.getIndex(v2) + " " + MathUtils.squaredDistance(v, v2));
                    }
                    assertTrue(a.contains(v2));
                }
            }
            System.out.println(num + "   " + a.size());
            assertEquals(num, a.size());
        }
    }

    @Test
    public void testCenter() {
        final MountainCarMdp mdp = new MountainCarMdp(MtnCarParams.defaultMtnCar());
        final GridFilter filter = new GridFilter(mdp.getStateSpace(), 5);
        System.out.println();
        Interval i1 = mdp.getStateSpace().getInterval(0);
        Interval i2 = mdp.getStateSpace().getInterval(1);
        List<Vector> cents = new ArrayList<Vector>();
        for (double pos = i1.getStart(); pos < i1.getEnd(); pos += i1.getWidth() / 30) {
            for (double vel = i2.getStart(); vel < i2.getEnd(); vel += i2.getWidth() / 30) {
                int index = filter.getIndex(Vector.asVector(pos, vel));
                Vector v = filter.getBinCenter(index);
                cents.add(v);
                if (index != filter.getIndex(v)) {
                    System.out.print(index + ", " + filter.getIndex(v) + " - ");
                }
                assertEquals(index, filter.getIndex(v));
            }
        }
        addCorners(cents, mdp.getStateSpace());
        for (Vector v : cents) {
            System.out.print(String.format("(%.3f,%.3f), ", v.get(0), v.get(1)));
        }
    }

}

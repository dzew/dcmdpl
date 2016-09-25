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

import java.util.ArrayList;
import java.util.List;

import drl.math.geom.Vector;

import org.junit.Test;

import drl.data.print.Serializer;

public class SerializerTest {

    @Test
    public void testVectorSerialization() {
        double[] ds = new double[5 + (int) (3 * Math.random())];
        List<Vector> vecs = new ArrayList<Vector>();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < ds.length; j++) {
                ds[j] = Math.random();
            }
            vecs.add(Vector.asVector(ds));
        }
        System.out.println(vecs);
        Serializer.writeToFile("data/myvecs", vecs);

        List<Vector> v2 = Serializer.vectorsFromFile("data/myvecs");
        System.out.println(v2);
        assertEquals(vecs, v2);
    }
}

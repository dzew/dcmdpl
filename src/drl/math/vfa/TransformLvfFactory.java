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

package drl.math.vfa;

import org.ejml.simple.SimpleMatrix;

import drl.math.geom.Vector;
import drl.math.tfs.Transform;

/**
 * A factory for TransformVfs.
 * 
 * @author Dawit
 * 
 */
public class TransformLvfFactory implements LvfFactory {

    private final LvfFactory factory;
    private final Transform tf;

    public TransformLvfFactory(Transform tf, LvfFactory factory) {
        this.factory = factory;
        this.tf = tf;
    }

    @Override
    public ValueFunction construct(SimpleMatrix coeffVector) {
        return new TransformVf(factory.construct(coeffVector), tf);
    }

    @Override
    public double[] generateBases(Vector vector) {
        return factory.generateBases(tf.transform(vector));
    }

    @Override
    public SimpleMatrix generateBases(Vector[] vectors) {
        Vector[] tfVecs = new Vector[vectors.length];
        for (int i = 0; i < tfVecs.length; i++) {
            tfVecs[i] = tf.transform(vectors[i]);
        }
        return factory.generateBases(tfVecs);
    }

}

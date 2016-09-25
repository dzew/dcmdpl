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

package drl.data.print;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import drl.math.geom.Vector;

/**
 * Static utility class for storing objects as files. Currently, the only
 * supported object type is a List of {@code drl.math.geom.Vector}.
 * 
 * @author Dawit
 * 
 */
public class Serializer {

    /**
     * Stores vecs on disk as fileName. No checks are done to see if a file by
     * that name already exists.
     * 
     */
    public static void writeToFile(String fileName, List<Vector> vecs) {
        try {
            FileWriter writer = new FileWriter(fileName);
            String delim = "";
            for (Vector v : vecs) {
                writer.write(delim + v.untruncated());
                delim = "\n";
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a list of vectors from the file at fileName
     * 
     */
    public static List<Vector> vectorsFromFile(String fileName) {
        try {
            Scanner scanner = new Scanner(new File(fileName));
            List<Vector> ret = new ArrayList<Vector>();
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();
                s = s.substring(1, s.length() - 1);
                String[] ss = s.split("\\s*,\\s*");
                double[] ds = new double[ss.length];
                for (int i = 0; i < ds.length; i++) {
                    ds[i] = Double.parseDouble(ss[i]);
                }
                ret.add(Vector.asVector(ds));
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

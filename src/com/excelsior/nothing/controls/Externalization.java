/*
 * Copyright (c) 2012, Nikita Lipsky, Excelsior LLC.
 *
 *  The Nothing System is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The Nothing System is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.excelsior.nothing.controls;

import com.excelsior.nothing.Kernel;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Common part for controls serialization.
 *
 * @author kit
 */
public class Externalization {

    public static void writeString(ObjectOutput out, String s) throws IOException {
        out.writeBoolean(s != null);
        if (s != null) {
            out.writeUTF(s);
        }
    }

    public static String readString(ObjectInput in) throws IOException {
        return in.readBoolean() ? in.readUTF() : null;
    }

    public static void writeExternal(JComponent c, ObjectOutput out) throws IOException {
        out.write(c.getX());
        out.write(c.getY());
        out.write(c.getWidth());
        out.write(c.getHeight());
        writeString(out, c.getName());
    }

    public static void readExternal(JComponent c, ObjectInput in) throws IOException, ClassNotFoundException {
        int x = in.read();
        int y = in.read();
        int w = in.read();
        int h = in.read();
        c.setBounds(x, y, w, h);
        String name = readString(in);
        if (name != null) {
            c.setName(name);
            Kernel.addToRegistry(name, c);
        }
    }
}

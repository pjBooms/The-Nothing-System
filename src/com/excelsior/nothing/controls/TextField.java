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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kit
 * @author hedjuo
 */
public class TextField extends JTextField implements Externalizable {

    private static final long serialVersionUID = 1030230214076481436l;

    private static final int SERIAL_VERSION = 1;

    public TextField() {
    }

    public TextField(String name) {
        this.setName(name);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(SERIAL_VERSION);
        Externalization.writeExternal(this, out);
        out.writeUTF(getText());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int v = in.read();
        if (v != SERIAL_VERSION) {
            throw new IOException("Unexpected TextField version: " + v);
        }
        Externalization.readExternal(this, in);
        setText(in.readUTF());
    }

}

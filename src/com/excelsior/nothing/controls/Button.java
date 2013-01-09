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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kit
 * @author hedjuo
 */
public class Button extends JButton implements Externalizable {

    private static final long serialVersionUID = 1030230214076481435l;

    private static final int SERIAL_VERSION = 1;

    private String cmd;

    private class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Kernel.executeCommand(cmd);
        }
    }

    public Button(){
    }

    public Button(final String cmd, String name) {
        this.cmd = cmd;
        this.setName(name);

        addActionListener(new MyActionListener());
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(SERIAL_VERSION);
        Externalization.writeExternal(this, out);
        out.writeUTF(getText());
        out.writeUTF(cmd);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int v = in.read();
        if (v != SERIAL_VERSION) {
            throw new IOException("Unexpected Button version: " + v);
        }
        Externalization.readExternal(this, in);
        setText(in.readUTF());
        cmd = in.readUTF();
        addActionListener(new MyActionListener());
    }

}

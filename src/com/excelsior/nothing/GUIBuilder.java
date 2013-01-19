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
package com.excelsior.nothing;

import com.excelsior.nothing.controls.Button;
import com.excelsior.nothing.controls.TextField;

import javax.swing.*;
import java.io.*;

/**
 * @author kit
 */
public class GUIBuilder {

    public static void newPanel() {
        Main.system.createPanel();
    }

    public static void addButton(String name, String text, String cmd, int x, int y, int w, int h) {
        JButton button = new Button(cmd, name);
        Kernel.addToRegistry(name, button);
        button.setText(text);
        Main.curPanel.add(name, button);
        button.setBounds(x, y, w, h);
    }

    public static void addTextField(String name, int x, int y, int w, int h) {
        JTextField textField = new TextField(name);

        Kernel.addToRegistry(name, textField);
        Main.curPanel.add(name, textField);
        textField.setBounds(x, y, w, h);
    }

    public static void save(String file) {
        try {
            FileOutputStream fstrm = new FileOutputStream(file);
            ObjectOutput ostrm = new ObjectOutputStream(fstrm);
            ostrm.writeObject(Main.curPanel.getPanel());
            ostrm.flush();
            fstrm.close();
            System.out.println("File " + file + " saved");
            Main.curPanel.setTitle(file);
        } catch (IOException io) {
            // should put in status panel
            System.err.println("IOException: " + io.getMessage());
        }
    }

    public static void open(String file) {
        try {
            InputStream in = Kernel.getInputStream(file = Kernel.checkThreeDots(file));
            if (in == null) return;
            ObjectInputStream istrm = new ObjectInputStream(in);
            JPanel panel = (JPanel) istrm.readObject();

            Main.Frame p = Main.system.createPanel(panel);
            p.setTitle(file);
        } catch (IOException io) {
            // should put in status panel
            System.err.println("IOException: " + io.getMessage());
        } catch (ClassNotFoundException cnf) {
            // should put in status panel
            System.err.println("Class not found: " + cnf.getMessage());
        }
    }


}

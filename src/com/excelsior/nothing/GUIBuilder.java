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
            ObjectInputStream istrm = new ObjectInputStream(Kernel.getInputStream(file));
            JPanel panel = (JPanel) istrm.readObject();

            Main.Panel p = Main.system.createPanel(panel);
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

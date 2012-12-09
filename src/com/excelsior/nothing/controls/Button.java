package com.excelsior.nothing.controls;

import com.excelsior.nothing.Kernel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author kit
 * @author hedjuo
 */
public class Button extends JButton {

    private static final long serialVersionUID = 1030230214076481435l;

    private String cmd;

    private class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Kernel.executeCommand(cmd);
        }
    }

    public Button(final String cmd, String name) {
        this.cmd = cmd;
        this.setName(name);

        addActionListener(new MyActionListener());
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Kernel.addToRegistry(getName(), this);
        addActionListener(new MyActionListener());
    }
}

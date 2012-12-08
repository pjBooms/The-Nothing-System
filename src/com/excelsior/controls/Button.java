package com.excelsior.controls;

import com.excelsior.nothing.Kernel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author kit
 * @author hedjuo
 */
public class Button extends JButton {
    private String cmd;

    private class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Kernel.executeCommand(cmd);
        }
    }

    public Button(final String cmd) {
        this.cmd = cmd;

        addActionListener(new MyActionListener());
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.write(getX());
        out.write(getY());
        out.write(getWidth());
        out.write(getHeight());
        out.writeUTF(getText());
        out.writeUTF(cmd);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int x = in.read();
        int y = in.read();
        int w = in.read();
        int h = in.read();
        setBounds(x, y, w, h);
        setText(in.readUTF());
        cmd = in.readUTF();
        addActionListener(new MyActionListener());
    }
}

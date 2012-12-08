package com.excelsior.controls;

import com.excelsior.nothing.Kernel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: slava
 * Date: 08.12.12
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class Button extends JButton {
    private String cmd = "System.out.println Default behavior for button";

    public Button(final String cmd) {
        this.cmd = cmd;

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Kernel.executeCommand(cmd);
            }
        });
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
//
//    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException
//    {
//        ois.defaultReadObject();
//        int a = 1;
////        addActionListener(new ActionListener() {
////            public void actionPerformed(ActionEvent e) {
////                Kernel.executeCommand(cmd);
////            }
////        });
//    }
}

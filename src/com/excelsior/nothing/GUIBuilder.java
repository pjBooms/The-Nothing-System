package com.excelsior.nothing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kit
 */
public class GUIBuilder {

    public static void newPanel() {
        Main.system.createPanel();
    }

    public static void newPanel(String title) {
        Main.Window w = Main.system.createPanel();
        w.setTitle(title);
    }

    static class Button extends JButton {
        private String cmd;

        Button(final String cmd) {
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
    }

    public static void addButton(String name, String text, String cmd, int x, int y, int w, int h) {
        JButton button = new Button(cmd);
        button.setText(text);
        Main.curPanel.add(name, button);
        button.setBounds(x, y, w, h);
    }

}

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

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author kit
 * Date: 05.10.12
 */
public class Main extends JPanel {
    int windowCount = 0;
    JDesktopPane desktop = null;

    public Integer FIRST_FRAME_LAYER = new Integer(1);
    public Integer DEMO_FRAME_LAYER = new Integer(2);
    public Integer PALETTE_LAYER = new Integer(3);

    public int FRAME0_X = 15;
    public int FRAME0_Y = 280;

    public int FRAME0_WIDTH = 620;
    public int FRAME0_HEIGHT = 430;

    public int FRAMEOUT_X = 1020;
    public int FRAMEOUT_Y = 0;

    public int FRAMEOUT_WIDTH = 260;
    public int FRAMEOUT_HEIGHT = 330;

    public int FRAME_WIDTH = 600;
    public int FRAME_HEIGHT = 450;

    public int PALETTE_X = 1020;
    public int PALETTE_Y = 330;

    public int PALETTE_WIDTH = 260;
    public int PALETTE_HEIGHT = 470;

    // The preferred size of the demo
    private int PREFERRED_WIDTH = 1280;
    private int PREFERRED_HEIGHT = 800;
    private JPanel panel = null;
    public static Main demo;


    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
        demo = new Main();
        JFrame frame = new JFrame(demo.getName());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(demo.getDemoPanel(), BorderLayout.CENTER);
        demo.getDemoPanel().setPreferredSize(new Dimension(demo.PREFERRED_WIDTH, demo.PREFERRED_HEIGHT));
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.show();
    }

    static TextWindow commands;
    static TextWindow frameOut;

    public Main() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        desktop = new JDesktopPane();
        getDemoPanel().add(desktop, BorderLayout.CENTER);

        commands = createInternalFrame(PALETTE_LAYER, 1, 1);
        commands.setBounds(PALETTE_X, PALETTE_Y, PALETTE_WIDTH, PALETTE_HEIGHT);
        String text;
        try {
            text = Sys.readText("commands.txt");
        } catch (IOException e){
            text = "Sys.createWindow\nSys.save\nSys.open\nSys.compile";
        }

        commands.getPad().getEditor().setText(text);
        commands.setTitle("System commands");

        TextWindow frame1 = createInternalFrame(FIRST_FRAME_LAYER, 1, 1);
        frame1.setBounds(FRAME0_X, FRAME0_Y, FRAME0_WIDTH, FRAME0_HEIGHT);
        frame1.getPad().getEditor().setText("System.out.println hello\nAnother text");


        frameOut = createInternalFrame(FIRST_FRAME_LAYER, 1, 1);
        frameOut.setBounds(FRAMEOUT_X, FRAMEOUT_Y, FRAMEOUT_WIDTH, FRAMEOUT_HEIGHT);
        frameOut.getPad().getEditor().setText("");
        frameOut.setTitle("Output");

        PrintStream stream = new PrintStream(new OutputStream(){
                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        JTextComponent editor = frameOut.getPad().getEditor();
                        editor.setText(editor.getText() + new String(b, off, len));
                    }

                    public void write(int b) throws IOException {
                        JTextComponent editor = frameOut.getPad().getEditor();
                        editor.setText(editor.getText() + new String(new byte[]{(byte)b}));
                    }
                });

        System.setOut(stream);
        System.setErr(stream);
    }

    public static TextWindow curWindow;
    public static JTextPane curSelection;

    public static JTextPane getCurEditor() {
        return (curWindow != null)? curWindow.getPad().getEditor(): null;
    }

    static class TextWindow extends JInternalFrame {
        private Stylepad pad;

        private TextWindow(Stylepad pad) {
            this.pad = pad;
            pad.getEditor().addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if ((TextWindow.this != commands) && (TextWindow.this != frameOut)) {
                        curWindow = TextWindow.this;
                    }
                }

                public void focusLost(FocusEvent e) {
                }

            });
        }

        public Stylepad getPad() {
            return pad;
        }
    }


    /**
     * Create an internal frame and add a scrollable imageicon to it
     */
    public TextWindow createInternalFrame(Integer layer, int width, int height) {
        TextWindow jif = new TextWindow(new Stylepad());

        jif.setTitle("Frame " + windowCount + "  ");

        jif.setClosable(true);
        jif.setMaximizable(true);
        jif.setIconifiable(true);
        jif.setResizable(true);

        jif.setBounds(20 * (windowCount % 10), 20 * (windowCount % 10), width, height);
        jif.setContentPane(jif.getPad());

        windowCount++;

        desktop.add(jif, layer);

        try {
            jif.setSelected(true);
        } catch (java.beans.PropertyVetoException e2) {
        }

        jif.show();

        return jif;
    }

    public TextWindow createInternalFrame() {
        return createInternalFrame(getDemoFrameLayer(),
                            getFrameWidth(),
                            getFrameHeight()
        );
    }

    public JPanel getDemoPanel() {
        return panel;
    }

    public String getName() {
        return "The Nothing System";
    }

    public int getFrameWidth() {
        return FRAME_WIDTH;
    }

    public int getFrameHeight() {
        return FRAME_HEIGHT;
    }

    public Integer getDemoFrameLayer() {
        return DEMO_FRAME_LAYER;
    }

}

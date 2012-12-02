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
        frame.setVisible(true);
    }

    public Main() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        configureDemoPanel();
        desktop = buildSplitedDesktopPane(userPane, systemPane);
        getDemoPanel().add(desktop, BorderLayout.CENTER);
        createDefaultFrame();
        createOutputFrame();
        createCommandsFrame();

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

    public static JTextPane getCurEditor() {
        return (curWindow != null)? curWindow.getPad().getEditor(): null;
    }

    static class TextWindow extends JInternalFrame {
        private Stylepad pad;

        private TextWindow(Stylepad pad) {
            this.pad = pad;
            pad.getEditor().addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if ((TextWindow.this != commandsFrame) && (TextWindow.this != frameOut)) {
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
     * Creates a new frame in given pane.
     *
     * @return TextWindow
     */
    public TextWindow createInternalFrame(JDesktopPane pane,int width, int height) {
        TextWindow window = new TextWindow(new Stylepad());

        window.setTitle("Frame " + windowCount + "  ");

        window.setClosable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        window.setResizable(true);

        window.setBounds(20 * (windowCount % 10), 20 * (windowCount % 10), width, height);
        window.setContentPane(window.getPad());

        windowCount++;

        pane.add(window, BorderLayout.CENTER);

        try {
            window.setSelected(true);
        } catch (java.beans.PropertyVetoException e2) {
        }

        window.show();

        return window;
    }

    public TextWindow createInternalFrame() {
        return createInternalFrame(userPane, getFrameWidth(), getFrameHeight());
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

    /**
     * Configures main window panel
     *
     * return void
     */
    private void configureDemoPanel()
    {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
    }

    /**
     * Builds splitted horizontally pane.
     *
     * @param JDesktopPane userPane
     * @param JDesktopPane systemPane
     * @return JSplitPane
     */
    private JSplitPane buildSplitedDesktopPane (JDesktopPane userPane, JDesktopPane systemPane)
    {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userPane, systemPane);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setDividerLocation(WINDOW_DIVIDER);
        return pane;
    }

    /**
     * Creates default frame in users pane.
     * @return void
     */
    private void createDefaultFrame()
    {
        TextWindow frame1 = createInternalFrame(userPane, 1, 1);
        frame1.setBounds(DEFAULT_FRAME_X, DEFAULT_FRAME_Y, FRAME_WIDTH, FRAME_HEIGHT);
        frame1.getPad().getEditor().setText("System.out.println hello\nAnother text");
    }

    /**
     * Creates command frame.
     *
     * @return void
     */
    private void createCommandsFrame()
    {
        commandsFrame = createInternalFrame(systemPane, 1, 1);
        commandsFrame.setBounds(0, 0, FRAMESYSTEM_WIDTH, FRAMESYSTEM_HEIGHT);
        String text;
        try {
            text = Sys.readText("commands.txt");
        } catch (IOException e){
            text = "Sys.createWindow\nSys.save\nSys.open\nSys.compile";
        }

        commandsFrame.getPad().getEditor().setText(text);
        commandsFrame.setTitle("System commands");
    }

    /**
     * Creates output frame.
     *
     * @return void
     */
    private void createOutputFrame()
    {
        frameOut = createInternalFrame(systemPane, 1, 1);
        frameOut.setBounds(640, 0, FRAMEOUT_WIDTH, FRAMEOUT_HEIGHT);
        frameOut.getPad().getEditor().setText("");
        frameOut.setTitle("Output");
    }

    int windowCount = 0;

    private JSplitPane desktop = null;

    public int DEFAULT_FRAME_X = 15;
    public int DEFAULT_FRAME_Y = 15;

    public int FRAMEOUT_WIDTH = 640;
    public int FRAMEOUT_HEIGHT = 190;

    public int FRAMESYSTEM_WIDTH = 640;
    public int FRAMESYSTEM_HEIGHT = 190;

    public int FRAME_WIDTH = 600;
    public int FRAME_HEIGHT = 450;

    public int PALETTE_X = 1020;
    public int PALETTE_Y = 330;

    public int PALETTE_WIDTH = 260;
    public int PALETTE_HEIGHT = 470;

    public static Main demo;
    public static TextWindow curWindow;
    public static JTextPane curSelection;

    static TextWindow commandsFrame;
    static TextWindow frameOut;

    // The preferred size of the demo
    private int PREFERRED_WIDTH = 1280;
    private int PREFERRED_HEIGHT = 800;
    private final int WINDOW_DIVIDER = 600;

    private JPanel panel = null;

    private JDesktopPane userPane = new JDesktopPane();
    private JDesktopPane systemPane = new JDesktopPane();
}
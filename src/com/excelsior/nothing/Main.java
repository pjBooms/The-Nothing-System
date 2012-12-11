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
import java.awt.event.*;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * @author kit
 * @author hedjuo
 * Date: 05.10.12
 */
public class Main extends JPanel {

    int windowCount = 0;

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 450;
    private static final double MAIN_WINDOW_DIVIDER_FRACTION = 0.8;
    private static final double SYSTEM_PANE_DIVIDER_FRACTION = 0.5;
    private static final double INITIAL_MAIN_TO_SCREEN_SIZE_FRACTION = 0.8;

    private JSplitPane desktop = null;
    public static Main system;
    public static TextWindow curTextWindow;
    public static Panel curPanel;
    public static JTextPane curSelection;

    private static TextWindow commandsFrame;
    private static TextWindow frameOut;

    private JPanel panel = null;

    private JDesktopPane userPane = new JDesktopPane();
    private JSplitPane systemPane = null;

    public static void main(String[] args) {
        system = new Main();
        JFrame frame = new JFrame(system.getName());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(system.getSystemPanel(), BorderLayout.CENTER);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int frameHeight = (int) (screenSize.getHeight()*INITIAL_MAIN_TO_SCREEN_SIZE_FRACTION);
        int frameWidth = (int) (screenSize.getWidth()*INITIAL_MAIN_TO_SCREEN_SIZE_FRACTION);

        system.getSystemPanel().setPreferredSize(new Dimension(frameWidth, frameHeight));
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
        configureSystemPanel();
        systemPane = createSystemPane();
        desktop = createSplitPane(userPane, systemPane);
        getSystemPanel().add(desktop, BorderLayout.CENTER);
        createDefaultFrame();

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
        return (curTextWindow != null)? curTextWindow.getPad().getEditor(): null;
    }

    static abstract class Window extends JInternalFrame {

        abstract JComponent getContent();
    }

    static class TextWindow extends Window {
        private Stylepad pad;

        private TextWindow(Stylepad pad) {
            this.pad = pad;
            pad.getEditor().addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if ((TextWindow.this != commandsFrame) && (TextWindow.this != frameOut)) {
                        curTextWindow = TextWindow.this;
                    }
                }
                public void focusLost(FocusEvent e) {}
            });
        }

        public Stylepad getPad() {
            return pad;
        }

        @Override
        JComponent getContent() {
            return pad;
        }
    }

    static class Panel extends Window {

        private JPanel panel;

        private Panel(JPanel panel) {
            this.panel = panel;
            panel.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    curPanel = Panel.this;
                }
            });
        }

        public JPanel getPanel() {
            return panel;
        }

        @Override
        JComponent getContent() {
            return panel;
        }
    }

    public Window createInternalFrame(JDesktopPane pane, int width, int height, Window window) {
        window.setTitle("Frame " + windowCount + "  ");

        window.setClosable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        window.setResizable(true);

        window.setBounds(20 * (windowCount % 10), 20 * (windowCount % 10), width, height);
        window.setContentPane(window.getContent());

        windowCount++;

        pane.add(window, BorderLayout.CENTER);

        try {
            window.setSelected(true);
        } catch (java.beans.PropertyVetoException e2) {
        }

        window.show();

        return window;
    }

    /**
     * Creates a new frame in a given pane.
     *
     * @return TextWindow
     */
    public Window createInternalFrame(JDesktopPane pane, int width, int height, boolean text) {
        Window window = text ? new TextWindow(new Stylepad()) : new Panel(new JPanel(null));
        return createInternalFrame(pane, width, height, window);
    }

    public TextWindow createText() {
        return (TextWindow) createInternalFrame(userPane, getFrameWidth(), getFrameHeight(), true);
    }

    public Panel createPanel() {
        curPanel = (Panel) createInternalFrame(userPane, getFrameWidth(), getFrameHeight(), false);
        return curPanel;
    }

    public Panel createPanel(JPanel panel) {
        return (Panel) createInternalFrame(userPane, getFrameWidth(), getFrameHeight(), new Panel(panel));
    }

    public JPanel getSystemPanel() {
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
     * Configures system pane
     */
    private JSplitPane createSystemPane() {
        JDesktopPane outputPane = new JDesktopPane();
        createOutputFrame(outputPane);

        JDesktopPane commandsPane = new JDesktopPane();
        createCommandsFrame(commandsPane);

        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputPane, commandsPane);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setDividerLocation(0.5);
        return pane;
    }

    /**
     * Configures main window panel.
     */
    private void configureSystemPanel()
    {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                desktop.setDividerLocation(MAIN_WINDOW_DIVIDER_FRACTION);
                systemPane.setDividerLocation(SYSTEM_PANE_DIVIDER_FRACTION);
            }
        });
    }

    /**
     * Builds vertically split pane.
     *
     * @param userPane - pane with user windows
     * @param systemPane -- pane containing command windows and standard output
     * @return split pane
     */
    private JSplitPane createSplitPane(JComponent userPane, JComponent systemPane)
    {
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPane, systemPane);
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setDividerLocation(MAIN_WINDOW_DIVIDER_FRACTION);
        return pane;
    }

    /**
     * Creates default frame in users pane.
     */
    private void createDefaultFrame()
    {
        TextWindow frame1 = (TextWindow) createInternalFrame(userPane, 1, 1, true);
        frame1.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        frame1.getPad().getEditor().setText("System.out.println hello\nAnother text");
    }

    /**
     * Creates a commands frame.
     */
    private void createCommandsFrame(JDesktopPane commandsPane)
    {
        commandsFrame = (TextWindow) createInternalFrame(commandsPane, 50, 50, true);
        String text;
        try {
            text = Sys.readText("commands.txt");
        } catch (IOException e){
            text = "Sys.createWindow\nSys.save\nSys.open\nSys.compile";
        }

        commandsFrame.getPad().getEditor().setText(text);
        commandsFrame.setTitle("System commands");
        commandsFrame.isForegroundSet();
        try {
            commandsFrame.setMaximum(true);
        } catch(PropertyVetoException e) {
        }


    }

    /**
     * Creates a standard output frame.
     */
    private void createOutputFrame(JDesktopPane outputPane)
    {
        frameOut = (TextWindow) createInternalFrame(outputPane, 50, 50, true);
        frameOut.getPad().getEditor().setText("");
        frameOut.setTitle("Output");
        frameOut.setClosable(false);
        frameOut.setIconifiable(false);
        frameOut.setMaximizable(false);

        try {
            frameOut.setMaximum(true);
        } catch(PropertyVetoException e) {
        }
    }

}
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import javax.swing.*;

/**
 * @author kit
 * Date: 05.10.12
 */
public class Stylepad extends JPanel {

    private JTextPane editor;

    public JTextPane getEditor() {
        return editor;
    }

    public Stylepad() {
        super(true);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exc) {
            System.err.println("Error loading L&F: " + exc);
        }

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());

        // create the embedded JTextComponent
        editor = createEditor();
        JScrollPane scroller = new JScrollPane();
        JViewport port = scroller.getViewport();
        port.add(editor);
        port.setBackingStoreEnabled(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", scroller);
        add("Center", panel);
    }

    private String retrieveCommand(JTextPane p, int x, int y) {
        int pos = p.viewToModel(new Point(x, y));
        String text = p.getText();
        int start = text.lastIndexOf('\n', pos);
        int end = text.indexOf('\n', pos);
        if (end == -1) end = text.length();
        if (start < end) {
            String cmd = text.substring(start+1, end);
            int spPos = cmd.indexOf(" ");
            if ((spPos<0) || (spPos >= (pos-start))) {
                return cmd;
            }
        }
        return null;
    }

    protected JTextPane createEditor() {
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
        final JTextPane p = new JTextPane(doc);

        final MyMouseMotionAdapter motion = new MyMouseMotionAdapter(p);
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String cmd = retrieveCommand(p, e.getX(), e.getY());
                if ((cmd != null) && Kernel.isCommand(cmd)) {
                    Kernel.executeCommand(cmd);
                }
            }

            public void mouseExited(MouseEvent e) {
                motion.underline(false);
            }
        });

        p.addMouseMotionListener(motion);
        p.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                Main.curSelection = editor;
            }
        });
        p.setDragEnabled(true);

        //p.getCaret().setBlinkRate(0);

        return p;
    }

    private class MyMouseMotionAdapter extends MouseMotionAdapter {
        private int lastStart;
        private int lastEnd;
        private final JTextPane p;

        public MyMouseMotionAdapter(JTextPane p) {
            this.p = p;
            lastStart = -1;
            lastEnd = -1;
        }

        public void mouseMoved(MouseEvent e) {
            String cmd = retrieveCommand(p, e.getX(), e.getY());
            if ((cmd != null) && Kernel.isCommand(cmd)) {
                int pos = p.viewToModel(new Point(e.getX(), e.getY()));
                String text = p.getText();
                int start = text.lastIndexOf('\n', pos) + 1;
                if (start != lastStart) {
                    underline(false);
                    lastStart = start;
                    int end = text.indexOf("\n", start);
                    if (end == -1) {
                        end = text.length();
                    }
                    int space = text.indexOf(" ", start);
                    if ((space >= 0) && (space < end)) {
                        lastEnd = space;
                    } else {
                        lastEnd = end;
                    }
                    underline(true);
                }
            } else {
                underline(false);
            }
        }

        public void underline(boolean b) {
            if ((lastStart == -1) || (lastEnd == -1)) return;
            Editor.underline(p, lastStart, lastEnd, b);
            if (!b) {
                lastStart = -1;
                lastEnd = -1;
            }
        }

    }
}

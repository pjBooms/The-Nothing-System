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
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import com.excelsior.nothing.controls.Button;

/**
 * @author kit
 * Date: 06.10.12
 */
public class Editor {

    private static StyledEditorKit getStyledEditorKit(JEditorPane e) {
        EditorKit k = e.getEditorKit();
        if (k instanceof StyledEditorKit) {
            return (StyledEditorKit) k;
        }
        throw new IllegalArgumentException("EditorKit must be StyledEditorKit");
    }

    private static StyledDocument getStyledDocument(JEditorPane e) {
        Document d = e.getDocument();
        if (d instanceof StyledDocument) {
            return (StyledDocument) d;
        }
        throw new IllegalArgumentException("document must be StyledDocument");
    }

    private static void setCharacterAttributes(JEditorPane editor, int p0, int p1,
                                               AttributeSet attr, boolean replace) {
        if (p0 != p1) {
            StyledDocument doc = getStyledDocument(editor);
            doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
        }
        StyledEditorKit k = getStyledEditorKit(editor);
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        if (replace) {
            inputAttributes.removeAttributes(inputAttributes);
        }
        inputAttributes.addAttributes(attr);
    }

    private static void setCharacterAttributes(JEditorPane editor,
                                               AttributeSet attr, boolean replace) {
        int p0 = editor.getSelectionStart();
        int p1 = editor.getSelectionEnd();
        setCharacterAttributes(editor, p0, p1, attr, replace);
    }

    public static void bold() {
        JEditorPane editor = Main.getCurEditor();
        if (editor != null) {
            StyledEditorKit kit = getStyledEditorKit(editor);
            MutableAttributeSet attr = kit.getInputAttributes();
            boolean bold = !StyleConstants.isBold(attr);
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setBold(sas, bold);
            setCharacterAttributes(editor, sas, false);
        }
    }

    public static void italic() {
        JEditorPane editor = Main.getCurEditor();
        if (editor != null) {
            StyledEditorKit kit = getStyledEditorKit(editor);
            MutableAttributeSet attr = kit.getInputAttributes();
            boolean italic = !StyleConstants.isItalic(attr);
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setItalic(sas, italic);
            setCharacterAttributes(editor, sas, false);
        }
    }

    public static void underline() {
        JEditorPane editor = Main.getCurEditor();
        if (editor != null) {
            StyledEditorKit kit = getStyledEditorKit(editor);
            MutableAttributeSet attr = kit.getInputAttributes();
            boolean underline = !StyleConstants.isUnderline(attr);
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setUnderline(sas, underline);
            setCharacterAttributes(editor, sas, false);
        }
    }

    public static void underline(JEditorPane editor, int start, int end, boolean underline) {
        StyledEditorKit kit = getStyledEditorKit(editor);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setUnderline(sas, underline);
        setCharacterAttributes(editor, start, end, sas, false);
    }

    public static void setColor(String color) {
        JEditorPane editor = Main.getCurEditor();
        if (editor == null) return;
        Color fg = null;
        try {
            Field f = Color.class.getDeclaredField(color.toUpperCase());
            f.setAccessible(true);
            fg = (Color) f.get(null);
        } catch (NoSuchFieldException e) {
            System.out.println("Unknown color: " + color);
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, fg);
        setCharacterAttributes(editor, attr, false);
    }

    public static void setFontSize(int size) {
        JEditorPane editor = Main.getCurEditor();
        if (editor != null) {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setFontSize(sas, size);
            setCharacterAttributes(editor, sas, false);
        }
    }


    public static void addButton(String name, String text, String cmd)
    {
        JTextPane textPane = Main.curTextWindow.getPad().getEditor();

        Document doc = textPane.getDocument();

        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        JButton button = new Button(cmd, name);
        button.setText(text);

        String styleName = "button-"+name;
        Style s = textPane.addStyle(styleName, def);

        StyleConstants.setComponent(s, button);

        try {
            doc.insertString(textPane.getCaretPosition(), " ", textPane.getStyle(styleName));
        } catch (BadLocationException ble) {
            System.out.println("Couldn't insert button.");
        }
    }

//    public static void insertPicture(String file) {
//        JEditorPane editor = Main.getCurEditor();
//        if (editor == null) return;
//        StyledDocument doc = (StyledDocument) editor.getDocument();
//        doc.getAtt
//        Icon image = new ImageIcon(file);
//
//    }

    public static void writeDocument(String fileName, JTextPane text) throws IOException {
            File file = new File(fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutput oos = new ObjectOutputStream(fos);
                oos.writeObject(text.getDocument());
                oos.close();
                Main.curTextWindow.setTitle(file.getName());
            } catch (IOException io) {
                // should put in status panel
                System.err.println("IOException: " + io.getMessage());
            }
        }

    static Document readDocument(InputStream in) throws IOException {
            Document doc = null;
            try {
                ObjectInput ois = new ObjectInputStream(new BufferedInputStream(in));
                doc = (Document) ois.readObject();
                ois.close();
                System.out.println("Document successfully loaded.");

            } catch (IOException io) {
                // should put in status panel
                System.err.println("IOException: " + io.getMessage());
            } catch (ClassNotFoundException cnf) {
                System.err.println("IOException: " + cnf.getMessage());
            }
            return doc;
        }

    public static void open(String file) throws IOException {
        Document doc;

        InputStream in = Kernel.getInputStream(file);
        if (in == null) {
            return;
        }

        Main.TextWindow w = Main.system.createText();

        doc = Editor.readDocument(in);
        w.setTitle(file);
        w.getPad().getEditor().setDocument(doc);
    }

    public static void save(String file) throws IOException {
        JTextPane cur = Main.getCurEditor();
        Editor.writeDocument(file, cur);

        Main.curTextWindow.setTitle(file);
        System.out.println("File " + file + " saved");
    }



}
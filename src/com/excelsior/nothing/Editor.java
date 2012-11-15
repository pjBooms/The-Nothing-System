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
import java.awt.event.ActionEvent;

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


}
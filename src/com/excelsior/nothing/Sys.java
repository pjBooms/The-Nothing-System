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

import javax.swing.text.JTextComponent;
import java.io.*;

/**
 * @author kit
 * Date: 05.10.12
 */
public class Sys {

    public static void newText() {
        Main.demo.createInternalFrame();
    }

    public static void newText(String title) {
        Main.TextWindow w = Main.demo.createInternalFrame();
        w.setTitle(title);
    }

    public static void writeText(String file, String text) throws IOException {
        OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
        os.write(text);
        os.close();
    }

    public static void save(String file) throws IOException {
        JTextComponent cur = Main.getCurEditor();
        if (cur == null) return;
        writeText(file, cur.getText());
        Main.curWindow.setTitle(file);
        System.out.println("File " + file + " saved");
    }

    public static void save() throws IOException {
        if (Main.curWindow == null) return;
        String file = Main.curWindow.getTitle();
        if (!new File(file).exists()) {
            System.out.println("Please specify file name");
            return;
        }
        writeText(file, Main.getCurEditor().getText());
        System.out.println("File " + file + " saved");
    }

    public static String readText(String file) throws IOException {
        LineNumberReader is = new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
        String text = "";
        String line;
        while ((line = is.readLine()) != null) {
            text += line + '\n';
        }
        is.close();
        return text;
    }

    public static void open(String file) throws IOException {
        String text = readText(file);
        Main.TextWindow w = Main.demo.createInternalFrame();
        w.setTitle(file);
        w.getPad().getEditor().setText(text);
    }

    public static void compile() throws Exception {
        if (Main.curWindow == null) return;
        String file = Main.curWindow.getTitle();
        if (!new File(file).exists()) {
            System.out.println("Please save file before compile");
            return;
        }
        save(file);
        if (com.sun.tools.javac.Main.compile(new String[]{file}) == 0) {
            System.out.println("Successfully compiled");
            String clazz = file.substring(0, file.lastIndexOf('.'));
//            FileInputStream fis = new FileInputStream (clazz + ".class");
//            byte buf [] = new byte [fis.available()];
//            fis.read (buf);
//            fis.close();
//            MyPreMain.instrument.redefineClasses(new ClassDefinition(Class.forName(clazz), buf));
            MethodHandle.addClass(clazz);
        }
    }

}

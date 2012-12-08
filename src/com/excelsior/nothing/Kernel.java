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

import com.excelsior.common.QuotedStringTokenizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author kit
 * Date: 05.10.12
 */
public class Kernel {

    private static HashMap<String, Object> registry = new HashMap<String, Object>();

    private static HashMap<String, Boolean> cache = new HashMap<String, Boolean>();

    public static boolean isCommand(String line) {
        QuotedStringTokenizer st = new QuotedStringTokenizer(line);
        String cmd = st.nextToken();
        if (cmd == null) {
            return false;
        }
        if (cache.containsKey(cmd)) return cache.get(cmd);
        MethodHandle m = MethodHandle.getMethodHandle(cmd);
        if (m != null)
            cache.put(cmd, m != null);
        return m != null;
    }

    public static void executeCommand(String line) {
        cache.clear();
        QuotedStringTokenizer st = new QuotedStringTokenizer(line);
        String cmd = st.nextToken();
        if (cmd == null) {
            return;
        }
        MethodHandle m = MethodHandle.getMethodHandle(cmd);
        if (m == null) return;

        String firstArg = st.nextToken();
        ArrayList<String> args = new ArrayList<String>();

        if (firstArg != null) {
            if (firstArg.equals("^") && (Main.curSelection != null) && (Main.curSelection.getSelectedText() != null)) {
                st = new QuotedStringTokenizer(Main.curSelection.getSelectedText());
            } else {
                args.add(firstArg);
            }

            while (true) {
                String nextToken = st.nextToken();
                if (nextToken == null) break;
                args.add(nextToken);
            }
        }

        try {
            m.invoke(args.toArray(new String[args.size()]));
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public static void addToRegistry(String key, Object o) {
        registry.put(key, o);
    }

    public static Object getFromRegistry(String key) {
        return registry.get(key);
    }

    private static URL getBaseUrlFrom(URL url)
    {
        try {
            return new URL(url.getProtocol() + "://" + url.getHost());
        } catch (MalformedURLException e) {
            assert false: "Bad base URL built";
            return null;
        }
    }

    public static InputStream getInputStream(String file) throws IOException {
        try {
            URL url = new URL(file);
            MethodHandle.addBaseURL(getBaseUrlFrom(url));
            return url.openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(file);
        }
    }
}

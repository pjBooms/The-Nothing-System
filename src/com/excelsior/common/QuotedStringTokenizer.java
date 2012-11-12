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

package com.excelsior.common;

/**
 * @author kit
 */
public class QuotedStringTokenizer {

    private String line;
    private int length;
    private int curPos;

    public QuotedStringTokenizer() {
    }

    public QuotedStringTokenizer(String line) {
        this();
        init(line);
    }

    public void init(String line) {
        this.line = line;
        this.length = line.length();
        curPos = 0;
    }

    /** null if eof */
    public String nextToken() {
        while(curPos < length && Character.isWhitespace(line.charAt(curPos)))
            curPos++;
        if(curPos >= length)
            return null;
        if(line.charAt(curPos) == '"') {
            curPos++;
            if(curPos >= length)
                return "\"";
            int startToken = curPos;
            while(curPos < length && line.charAt(curPos) != '"')
                curPos++;
            if(curPos >= length)
                return line.substring(startToken);
            curPos++;
            return line.substring(startToken, curPos-1);
        } else {
            int startToken = curPos;
            while(curPos < length && !Character.isWhitespace(line.charAt(curPos)))
                curPos++;
            if(curPos >= length)
                return line.substring(startToken);
            return line.substring(startToken, curPos);
        }
    }
}

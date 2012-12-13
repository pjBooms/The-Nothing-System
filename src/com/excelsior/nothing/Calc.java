package com.excelsior.nothing;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author hedjuo
 */
public class Calc
{
    public static Integer eval(String expression)
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");

        try {
            int val = ((Double) engine.eval(expression)).intValue();
            return val;
        } catch(ScriptException se) {
            System.out.println("Error: "+se.getMessage());
            return 0;
        }
    }
}

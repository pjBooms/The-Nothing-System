import com.excelsior.nothing.*;

/**
 * Calculator sample — works with the Compose GUIBuilder.
 *
 * Text fields are registered as GUIBuilder.TextFieldProxy objects.
 * Use @fieldName in commands to pass the proxy as an argument, e.g.:
 *
 *   Calculator.appendText @display 7
 *   Calculator.appendText @display +
 *   Calculator.eval @display
 *   Calculator.clear @display
 */
class Calculator {

    public static void appendText(GUIBuilder.TextFieldProxy f, String val) {
        f.setText(f.getText() + val);
    }

    public static void eval(GUIBuilder.TextFieldProxy f) {
        f.setText(String.valueOf(Calc.INSTANCE.eval(f.getText())));
    }

    public static void clear(GUIBuilder.TextFieldProxy f) {
        f.setText("");
    }
}

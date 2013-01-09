import com.excelsior.nothing.*;
import javax.swing.*;
import com.excelsior.nothing.Calc;

class Calculator {

    public static void appendText(JTextField f, String val) {
           f.setText(f.getText() + val);
    }

    public static void eval(JTextField f) {
           f.setText(Calc.eval(f.getText()).toString());
    }
}

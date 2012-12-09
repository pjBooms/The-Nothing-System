package com.excelsior.nothing.controls;

import com.excelsior.nothing.Kernel;

import javax.swing.*;
import java.io.IOException;

/**
 * @author kit
 * @author hedjuo
 */
public class TextField extends JTextField {

    public TextField(String name) {
        this.setName(name);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Kernel.addToRegistry(getName(), this);
    }

}

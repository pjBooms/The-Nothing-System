/*
 * Copyright (c) 2012-2013, Nikita Lipsky, Excelsior LLC.
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
package com.excelsior.nothing.persistance;

import com.sun.java.util.jar.pack.*;

import java.io.*;
import java.lang.reflect.Field;

/**
 * This class overrides standard versioning of Java serialization based on class SUIDs.
 * If a class implements {@link Persistent} interface its Class descriptor will be written in the form
 * $ClassName
 * $serialVersionUID
 *
 * and there will be no ClassDescriptors  written for super classes of the class.
 *
 * It is assumed that Class itself will provide proper deserialization of outdated objects format versions.
 *
 * @author kit
 */
public class PersistentObjectOutputStream extends ObjectOutputStream {

    public PersistentObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    private void setSuperToNull(ObjectStreamClass desc) throws IOException {
        try {
            Field superDesc = ObjectStreamClass.class.getDeclaredField("superDesc");
            superDesc.setAccessible(true);
            superDesc.set(desc, null);
        } catch (NoSuchFieldException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class target = desc.forClass();
        boolean persistant = Persistent.class.isAssignableFrom(target);
        writeBoolean(persistant);
        if (!persistant) {
            super.writeClassDescriptor(desc);
            return;
        }
        setSuperToNull(desc);
        writeUTF(desc.getName());
        writeLong(desc.getSerialVersionUID());
    }

}

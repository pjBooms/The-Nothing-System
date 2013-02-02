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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Deserialization of persistent objects.
 *
 * If a class for persistent object changes its externalization method it must provide the way it deserialize old format
 * of it.
 *
 * @author kit
 * @version 27.01.13
 */
public class PersistentObjectInputStream extends ObjectInputStream {

    private HashMap<Class, Long> classVersions = new HashMap<Class, Long>();

    public PersistentObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    private ObjectStreamClass createDesc(String name) throws IOException {
        try {
            Constructor<ObjectStreamClass> constr = ObjectStreamClass.class.getDeclaredConstructor();
            constr.setAccessible(true);
            Object desc = constr.newInstance();
            Field nameField = ObjectStreamClass.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(desc, name);
            return (ObjectStreamClass) desc;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        boolean persistent = readBoolean();
        if (!persistent) {
            return super.readClassDescriptor();
        } else {
            String name = readUTF();
            long version = readLong();
            ObjectStreamClass desc = createDesc(name);
            Class c = resolveClass(desc);
            classVersions.put(c, version);
            return ObjectStreamClass.lookup(c);
        }
    }

    public static long getSerializedVersion(Class c, ObjectInput in) throws IOException {
        if (!(in instanceof PersistentObjectInputStream)) {
            throw new IOException("Cannot read from non-persistent stream");
        }
        return ((PersistentObjectInputStream)in).classVersions.get(c);
    }

}

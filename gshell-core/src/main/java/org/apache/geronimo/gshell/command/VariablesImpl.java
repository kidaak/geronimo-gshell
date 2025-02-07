/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gshell.command;

import org.apache.commons.lang.NullArgumentException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Variables backed up by a map.
 *
 * @version $Rev$ $Date$
 */
public class VariablesImpl
    implements Variables
{
    private final Map<String,Object> map;

    private final Variables parent;

    private final Set<String> immutables = new HashSet<String>();

    public VariablesImpl(final Map<String,Object> map, final Variables parent) {
        if (map == null) {
            throw new NullArgumentException("map");
        }
        if (parent == null) {
            throw new NullArgumentException("parent");
        }

        this.map = map;
        this.parent = parent;
    }

    public VariablesImpl(final Variables parent) {
        if (parent == null) {
            throw new NullArgumentException("parent");
        }

        this.map = new HashMap<String,Object>();
        this.parent = parent;
    }

    public VariablesImpl(final Map<String,Object> map) {
        if (map == null) {
            throw new NullArgumentException("map");
        }

        this.map = map;
        this.parent = null;
    }

    public VariablesImpl() {
        this(new HashMap<String,Object>());
    }

    public void set(final String name, final Object value) {
        set(name, value, true);
    }

    public void set(final String name, final Object value, boolean mutable) {
        assert name != null;

        ensureMutable(name);

        map.put(name, value);

        if (!mutable) {
            immutables.add(name);
        }
    }

    public Object get(final String name) {
        assert name != null;

        Object value = map.get(name);
        if (value == null && parent != null) {
            value = parent.get(name);
        }

        return value;
    }

    public Object get(final String name, final Object _default) {
        Object value = get(name);
        if (value == null) {
            return _default;
        }

        return value;
    }

    public void unset(final String name) {
        assert name != null;

        ensureMutable(name);

        map.remove(name);
    }

    public boolean contains(final String name) {
        assert name != null;

        return map.containsKey(name);
    }

    public boolean isMutable(final String name) {
        assert name != null;

        boolean mutable = true;

        // First ask out parent if there is one, if they are immutable, then so are we
        if (parent != null) {
            mutable = parent.isMutable(name);
        }

        if (mutable) {
            mutable = !immutables.contains(name);
        }

        return mutable;
    }

    private void ensureMutable(final String name) {
        assert name != null;

        if (!isMutable(name)) {
            throw new ImmutableVariableException(name);
        }
    }

    public boolean isCloaked(final String name) {
        assert name != null;

        int count = 0;

        Variables vars = this;
        while (vars != null && count < 2) {
            if (vars.contains(name)) {
                count++;
            }

            vars = vars.parent();
        }

        return count > 1;
    }

    public Iterator<String> names() {
        // Chain to parent iterator if we have a parent
        return new Iterator<String>() {
            Iterator<String> iter = map.keySet().iterator();
            boolean more = parent() != null;

            public boolean hasNext() {
                boolean next = iter.hasNext();
                if (!next && more) {
                    iter = parent().names();
                    more = false;
                    next = hasNext();
                }

                return next;
            }

            public String next() {
                return iter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Variables parent() {
        return parent;
    }

    public static boolean isIdentifier(final String name) {
        if (name == null || name.length() == 0) {
            return false;
        }

        char[] chars = name.toCharArray();

        if (!Character.isJavaIdentifierStart(chars[0])) {
            return false;
        }

        /*

        FIXME: This fails for stuff like 'gshell.prompt' which we should allow
               Eventually need to get this fixed, for now just skip part checking

        for (int i=1; i<chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i])) {
                return false;
            }
        }
        */

        return true;
    }
}

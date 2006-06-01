/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.gshell.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * ???
 *
 * @version $Id$
 */
public class VariablesMap
    implements Variables
{
    private final Map<String,Object> map;

    private final Variables parent;

    private final Set<String> immutables = new HashSet<String>();

    public VariablesMap(final Map<String,Object> map, final Variables parent) {
        if (map == null) {
            throw new IllegalArgumentException("Map is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }

        this.map = map;
        this.parent = parent;
    }

    public VariablesMap(final Variables parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }

        this.map = new HashMap<String,Object>();
        this.parent = parent;
    }

    public VariablesMap(final Map<String,Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map is null");
        }

        this.map = map;
        this.parent = null;
    }

    public VariablesMap() {
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
        return Collections.unmodifiableSet(map.keySet()).iterator();
    }

    public Variables parent() {
        return parent;
    }
}

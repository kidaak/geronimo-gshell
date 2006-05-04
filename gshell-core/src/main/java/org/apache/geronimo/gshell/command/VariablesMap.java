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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * ???
 *
 * @version $Id$
 */
public class VariablesMap
    implements Variables
{
    private final Map map;
    
    public VariablesMap(final Map map) {
        assert map != null;
        
        this.map = map;
    }
    
    public VariablesMap() {
        this(new HashMap());
    }
    
    public void set(final String name, final Object value) {
        assert name != null;
        
        map.put(name, value);
    }
    
    public Object get(final String name) {
        assert name != null;
        
        return map.get(name);
    }
    
    public void remove(final String name) {
        assert name != null;
        
        map.remove(name);
    }
    
    public boolean isSet(final String name) {
        assert name != null;
        
        return map.containsKey(name);
    }
    
    public Iterator names() {
        return map.keySet().iterator();
    }
}

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

package org.apache.geronimo.gshell;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.command.CommandNotFoundException;

/**
 * Unit tests for the {@link Shell} class.
 *
 * @version $Rev$ $Date$
 */
public class ShellTest
    extends TestCase
{
    public void testConstructorArgs() throws Exception {
        try {
            new Shell(null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        new Shell();

        new Shell(new IO());
    }

    public void testExecuteVarargs() throws Exception {
        Shell shell = new Shell();

        try {
            shell.execute("foo", "bar", "baz");
        }
        catch (CommandNotFoundException expected) {
            // ignore
        }
    }

    public void testExecuteArray() throws Exception {
        Shell shell = new Shell();

        try {
            shell.execute(new Object[]{ "foo", "bar", "baz" });
        }
        catch (CommandNotFoundException expected) {
            // ignore
        }
    }
}

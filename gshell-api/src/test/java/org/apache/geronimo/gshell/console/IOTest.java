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

package org.apache.geronimo.gshell.console;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link IO} class.
 *
 * @version $Rev$ $Date$
 */
public class IOTest
    extends TestCase
{
    public void testConstructorArgs() throws Exception {
        try {
            new IO(null, null, null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        try {
            new IO(System.in, null, null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        try {
            new IO(System.in, System.out, null);
            fail("Accepted null value");
        }
        catch (IllegalArgumentException expected) {
            // ignore
        }

        // Happy day...
        new IO(System.in, System.out, System.err);
    }
}

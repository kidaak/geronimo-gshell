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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * A simple console implementation using a buffered reader.
 *
 * @version $Rev$ $Date$
 */
public class SimpleConsole
    implements Console
{
    private static final Log log = LogFactory.getLog(SimpleConsole.class);

    private final IO io;

    private final BufferedReader reader;

    public SimpleConsole(final IO io) {
        if (io == null) {
            throw new NullArgumentException("io");
        }

        this.io = io;
        this.reader = new BufferedReader(io.in);
    }

    public String readLine(final String prompt) throws IOException {
        assert prompt != null;
        
        if (log.isDebugEnabled()) {
            log.debug("Reading line, w/prompt: " + prompt);
        }
        
        io.out.print(prompt);
        io.out.flush();

        return reader.readLine();
    }

    public IO getIO() {
        return io;
    }

    public BufferedReader getReader() {
        return reader;
    }
}

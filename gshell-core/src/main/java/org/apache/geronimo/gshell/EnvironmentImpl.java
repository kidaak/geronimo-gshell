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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default/standard/whatever implementation of {@link Environment}.
 *
 * @version $Rev$ $Date$
 */
public class EnvironmentImpl
    implements Environment, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private File homeDir;

    public File getHomeDir() {
        if (homeDir == null) {
            throw new IllegalStateException();
        }

        return homeDir;
    }

    public void initialize() throws InitializationException {
        homeDir = detectHomeDir();
        log.debug("Using home directory: {}", homeDir);
    }
    
    private File detectHomeDir() throws InitializationException {
        // For right now we require that a "gshell.home" property be set, which should have been set by the Bootstrapper.
        // Will eventually allow this to be changed for shell branding mucko

        String homePath = System.getProperty("gshell.home");

        if (homePath == null) {
            throw new InitializationException("The 'gsell.home' property must be set for the shell to function correctly");
        }

        // And now lets resolve this sucker
        File dir;
        try {
            dir = new File(homePath).getCanonicalFile();
        }
        catch (IOException e) {
            throw new InitializationException("Failed to resolve home directory: " + homePath, e);
        }

        // And some basic sanity too
        if (!dir.exists() || !dir.isDirectory()) {
            throw new InitializationException("Home directory configured but is not a valid directory: " + dir);
        }

        return dir;
    }

    //
    // TODO: Merge in the other command-context and variable access muck here.  The Environment for the shell should be the one place
    //       were all state is stored, so we can use this to multiplex shells and provide commands with a simple/single interface
    //       for interacting with the user.
    //
    //       IO
    //       Variables
    //       Shell information
    //       Current command descriptor
    //
}

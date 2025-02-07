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

package org.apache.geronimo.gshell.builtins;

import org.apache.commons.cli.CommandLine;

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.util.Arguments;

/**
 * Exit the current shell.
 *
 * @version $Rev$ $Date$
 */
public class ExitCommand
    extends CommandSupport
{
    private int exitCode = 0;

    public ExitCommand() {
        super("exit");
    }

    protected String getUsage() {
        return super.getUsage() + " [code]";
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        String[] args = line.getArgs();

        IO io = getIO();
        MessageSource messages = getMessageSource();

        if (args.length > 1) {
            io.err.println(messages.getMessage("info.unexpected_args", Arguments.asString(args)));
            io.err.println();
            return true;
        }
        if (args.length == 1) {
            exitCode = Integer.parseInt(args[0]);
        }

        return false;
    }

    protected Object doExecute(Object[] args) throws Exception {
        assert args != null;

        log.info("Exiting w/code: " + exitCode);

        //
        // DO NOT Call System.exit() !!!
        //

        throw new ExitNotification(exitCode);
    }
}

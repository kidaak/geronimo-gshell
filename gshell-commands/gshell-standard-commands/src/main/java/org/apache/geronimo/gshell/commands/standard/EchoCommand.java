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

package org.apache.geronimo.gshell.commands.standard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.MessageSource;
import org.apache.geronimo.gshell.command.CommandException;
import org.apache.geronimo.gshell.console.IO;

/**
 * A simple command to <em>echo</em> all given arguments to the commands standard output.
 *
 * @version $Rev$ $Date$
 */
public class EchoCommand
    extends CommandSupport
{
    private boolean trailingNewline = true;
    
    public EchoCommand() {
        super("echo");
    }

    protected Options getOptions() {
        MessageSource messages = getMessageSource();

        Options options = super.getOptions();

        options.addOption(OptionBuilder
            .withDescription(messages.getMessage("cli.option.n"))
            .create('n'));

        return options;
    }

    protected boolean processCommandLine(final CommandLine line) throws CommandException {
        assert line != null;

        if (line.hasOption('n')) {
            trailingNewline = false;
        }

        return false;
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        IO io = getIO();

        for (int i=0; i < args.length; i++) {
            io.out.print(args[i]);
            if (i + 1 < args.length) {
                io.out.print(" ");
            }
        }

        if (trailingNewline) {
            io.out.println();
        }
        
        return Command.SUCCESS;
    }
}

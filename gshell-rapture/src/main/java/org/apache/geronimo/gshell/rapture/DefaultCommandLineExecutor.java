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

package org.apache.geronimo.gshell.rapture;

import org.apache.geronimo.gshell.application.ApplicationManager;
import org.apache.geronimo.gshell.application.DefaultVariables;
import org.apache.geronimo.gshell.chronos.StopWatch;
import org.apache.geronimo.gshell.command.Command;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandInfo;
import org.apache.geronimo.gshell.command.CommandResolver;
import org.apache.geronimo.gshell.command.CommandResult;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.commandline.CommandLine;
import org.apache.geronimo.gshell.commandline.CommandLineBuilder;
import org.apache.geronimo.gshell.commandline.CommandLineExecutionFailied;
import org.apache.geronimo.gshell.commandline.CommandLineExecutor;
import org.apache.geronimo.gshell.io.IO;
import org.apache.geronimo.gshell.io.SystemOutputHijacker;
import org.apache.geronimo.gshell.notification.ErrorNotification;
import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.shell.ShellContext;
import org.apache.geronimo.gshell.util.Arguments;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The default {@link CommandLineExecutor} component.
 *
 * @version $Rev$ $Date$
 */
@Component(role= CommandLineExecutor.class)
public class DefaultCommandLineExecutor
    implements CommandLineExecutor, Initializable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Requirement
    private ApplicationManager applicationManager;

    @Requirement
    private CommandResolver commandResolver;

    @Requirement
    private CommandLineBuilder commandLineBuilder;

    private ShellContext shellContext;

    public DefaultCommandLineExecutor() {}
    
    public DefaultCommandLineExecutor(final ApplicationManager applicationManager, final CommandLineBuilder commandLineBuilder) {
        assert applicationManager != null;
        assert commandLineBuilder != null;

        this.applicationManager = applicationManager;
        this.commandLineBuilder = commandLineBuilder;
    }

    public void initialize() throws InitializationException {
        assert applicationManager != null;
        
        this.shellContext = applicationManager.getContext().getEnvironment();
    }

    public Object execute(final String line) throws Exception {
        assert line != null;

        log.info("Executing (String): {}", line);

        try {
            CommandLine cl = commandLineBuilder.create(line);

            return cl.execute();
        }
        catch (ErrorNotification n) {
            // Decode the error notifiation
            Throwable cause = n.getCause();

            if (cause instanceof Exception) {
                throw (Exception)cause;
            }
            else if (cause instanceof Error) {
                throw (Error)cause;
            }
            else {
                // Um, if we get this far, which we probably never will, then just re-toss the notifcation
                throw n;
            }
        }
    }

    public Object execute(final Object... args) throws Exception {
        assert args != null;
        assert args.length > 1;

        log.info("Executing (Object...): [{}]", Arguments.asString(args));

        return execute(String.valueOf(args[0]), Arguments.shift(args), shellContext.getIo());
    }

    public Object execute(final String path, final Object[] args) throws Exception {
        assert path != null;
        assert args != null;

        log.info("Executing ({}): [{}]", path, Arguments.asString(args));

        return execute(path, args, shellContext.getIo());
    }

    public Object execute(final Object[][] commands) throws Exception {
        assert commands != null;

        // Prepare IOs
        final IO[] ios = new IO[commands.length];
        PipedOutputStream pos = null;

        for (int i = 0; i < ios.length; i++) {
            InputStream is = (i == 0) ? shellContext.getIo().inputStream : new PipedInputStream(pos);
            OutputStream os;

            if (i == ios.length - 1) {
                os = shellContext.getIo().outputStream;
            }
            else {
                os = pos = new PipedOutputStream();
            }

            ios[i] = new IO(is, new PrintStream(os), shellContext.getIo().errorStream);
        }

        Thread[] threads = new Thread[commands.length];
        final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
        final AtomicReference<Object> ref = new AtomicReference<Object>();

        for (int i = 0; i < commands.length; i++) {
            final int idx = i;

            threads[i] = createThread(new Runnable() {
                public void run() {
                    try {
                        Object o = execute(String.valueOf(commands[idx][0]), Arguments.shift(commands[idx]), ios[idx]);

                        if (idx == commands.length - 1) {
                            ref.set(o);
                        }
                    }
                    catch (Throwable t) {
                        errors.add(t);
                    }
                    finally {
                        if (idx > 0) {
                            IOUtil.close(ios[idx].inputStream);
                        }
                        if (idx < commands.length - 1) {
                            IOUtil.close(ios[idx].outputStream);
                        }
                    }
                }
            });

            threads[i].start();
        }

        for (int i = 0; i < commands.length; i++) {
            threads[i].join();
        }

        if (!errors.isEmpty()) {
            Throwable t = errors.get(0);

            // Always preserve the type of notication throwables, reguardless of the trace
            if (t instanceof Notification) {
                throw (Notification)t;
            }

            // Otherwise wrap to preserve the trace
            throw new CommandLineExecutionFailied(t);
        }

        return ref.get();
    }

    protected Thread createThread(Runnable run) {
        return new Thread(run);
    }

    //
    // TODO: Let the CommandContext creation happen in the child, pass in the ShellContext here...
    //

    protected Object execute(final String path, final Object[] args, final IO io) throws Exception {
        log.debug("Executing");

        final Command command = commandResolver.resolve(shellContext, path);

        // Setup the command context and pass it to the command instance
        CommandContext context = new CommandContext()
        {
            // Command instances get their own namespace with defaults from the current
            final Variables vars = new DefaultVariables(shellContext.getVariables());

            public Object[] getArguments() {
                return args;
            }
            
            public IO getIo() {
                return io;
            }

            public Variables getVariables() {
                return vars;
            }

            public CommandInfo getInfo() {
                return command.getInfo();
            }

            public ShellContext getShellContext() {
                return shellContext;
            }
        };
        
        // Setup command timings
        StopWatch watch = new StopWatch(true);

        // Hijack the system streams in the current thread's context
        SystemOutputHijacker.register(io.outputStream, io.errorStream);

        CommandResult result;
        try {
            result = command.execute(context);

            log.debug("Command completed with result: {}, after: {}", result, watch);
        }
        finally {
            // Restore hijacked streams
            SystemOutputHijacker.deregister();

            // Make sure that the commands output has been flushed
            try {
                io.flush();
            }
            catch (Exception ignore) {}
        }

        // Decode the command result
        if (result.hasNotified()) {
            throw result.getNotification();
        }
        else if (result.hasFailed()) {
            throw result.getFailure();
        }
        else {
            return result.getValue();
        }
    }
}
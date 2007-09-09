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

package org.apache.geronimo.gshell.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jline.History;
import jline.Terminal;
import org.apache.geronimo.gshell.ErrorNotification;
import org.apache.geronimo.gshell.ExitNotification;
import org.apache.geronimo.gshell.Shell;
import org.apache.geronimo.gshell.ansi.ANSI;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.clp.Printer;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.common.StopWatch;
import org.apache.geronimo.gshell.console.Console;
import org.apache.geronimo.gshell.console.JLineConsole;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line interface to bootstrap Shell.
 *
 * @version $Rev$ $Date$
 */
public class Main
{
    ///CLOVER:OFF
    
    //
    // NOTE: Do not use logging from this class, as it is used to configure
    //       the logging level with System properties, which will only get
    //       picked up on the initial loading of Log4j
    //

    private final ClassWorld classWorld;

    private final IO io = new IO();

    private final StopWatch watch = new StopWatch(true);

    // Late initialized
    private Logger log;

    public Main(final ClassWorld classWorld) {
        assert classWorld != null;

        this.classWorld = classWorld;
    }

    @Option(name="-h", aliases={"--help"}, description="Display this help message")
    private boolean help;

    @Option(name="-V", aliases={"--version"}, description="Display GShell version")
    private boolean version;

    @Option(name="-i", aliases={"--interactive"}, description="Run in interactive mode")
    private boolean interactive = true;

    private void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }

    @Option(name="-d", aliases={"--debug"}, description="Enable DEBUG logging output")
    private void setDebug(boolean flag) {
        if (flag) {
            setConsoleLogLevel("DEBUG");
            io.setVerbosity(IO.Verbosity.DEBUG);
        }
    }

    @Option(name="-v", aliases={"--verbose"}, description="Enable INFO logging output")
    private void setVerbose(boolean flag) {
        if (flag) {
            setConsoleLogLevel("INFO");
            io.setVerbosity(IO.Verbosity.VERBOSE);
        }
    }

    @Option(name="-q", aliases={"--quiet"}, description="Limit logging output to ERROR")
    private void setQuiet(boolean flag) {
        if (flag) {
            setConsoleLogLevel("ERROR");
            io.setVerbosity(IO.Verbosity.QUIET);
        }
    }

    @Option(name="-c", aliases={"--commands"}, description="Read commands from string")
    private String commands;

    @Argument(description="Command")
    private List<String> args = new ArrayList<String>(0);

    @Option(name="-D", aliases={"--define"}, metaVar="NAME=VALUE", description="Define system properties")
    private void setSystemProperty(final String nameValue) {
        assert nameValue != null;

        String name, value;
        int i = nameValue.indexOf("=");

        if (i == -1) {
            name = nameValue;
            value = Boolean.TRUE.toString();
        }
        else {
            name = nameValue.substring(0, i);
            value = nameValue.substring(i + 1, nameValue.length());
        }
        name = name.trim();

        System.setProperty(name, value);
    }

    @Option(name="-C", aliases={"--color"}, argumentRequired=false, description="Enable or disable use of ANSI colors")
    private void enableAnsiColors(final boolean flag) {
        ANSI.setEnabled(flag);
    }

    @Option(name="-T", aliases={"--terminal"}, metaVar="TYPE", argumentRequired=true, description="Specify the terminal TYPE to use")
    private void setTerminalType(String type) {
        type = type.toLowerCase();

        if ("unix".equals(type)) {
            type = jline.UnixTerminal.class.getName();
        }
        else if ("win".equals(type) || "windows".equals("type")) {
            type = jline.WindowsTerminal.class.getName();
        }
        else if ("false".equals(type) || "off".equals(type) || "none".equals(type)) {
            type = jline.UnsupportedTerminal.class.getName();

            //
            // HACK: Disable ANSI, for some reason UnsupportedTerminal reports ANSI as enabled, when it shouldn't
            //
            ANSI.setEnabled(false);
        }

        System.setProperty("jline.terminal", type);
    }

    private File getUserStateDirectory() {
        File userHome = new File(System.getProperty("user.home"));
        File dir = new File(userHome, ".gshell");

        try {
            return dir.getCanonicalFile();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadUserScript(final Shell shell, final String fileName) throws Exception {
        assert fileName != null;

        File file = new File(getUserStateDirectory(), fileName);

        if (file.exists()) {
            log.debug("Loading user-script: {}", file);

            shell.execute("source", file.toURI().toURL());
        }
    }

    private void displayError(final Throwable error) {
        assert error != null;

        // Decode any error notifications
        Throwable cause = error;
        if (error instanceof ErrorNotification) {
            cause = error.getCause();
        }

        // Spit out the terse reason why we've failed
        io.err.print("@|bold,red ERROR| ");
        io.err.print(cause.getClass().getSimpleName());
        io.err.println(": @|bold,red " + cause.getMessage() + "|");
        
        if (io.isDebug()) {
            // If we have debug enabled then skip the fancy bits below, and log the full error, don't decode shit
            log.debug(error.toString(), error);
        }
        else if (io.isVerbose()) {
            // Render a fancy ansi colored stack trace
            StackTraceElement[] trace = cause.getStackTrace();
            StringBuffer buff = new StringBuffer();

            for (StackTraceElement e : trace) {
                buff.append("        @|bold at| ").
                        append(e.getClassName()).
                        append(".").
                        append(e.getMethodName()).
                        append(" (@|bold ");

                buff.append(e.isNativeMethod() ? "Native Method" :
                            (e.getFileName() != null && e.getLineNumber() != -1 ? e.getFileName() + ":" + e.getLineNumber() :
                                (e.getFileName() != null ? e.getFileName() : "Unknown Source")));

                buff.append("|)");

                io.err.println(buff);

                buff.setLength(0);
            }
        }
    }

    private String getBanner() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        out.println("   ____ ____  _          _ _ ");
        out.println("  / ___/ ___|| |__   ___| | |");
        out.println(" | |  _\\___ \\| '_ \\ / _ \\ | |");
        out.println(" | |_| |___) | | | |  __/ | |");
        out.println("  \\____|____/|_| |_|\\___|_|_|");
        out.println();
        out.println(" @|bold GShell| (" + Version.getInstance() + ")");
        out.println();
        out.println("Type '@|bold help|' for help.");
        out.flush();

        return writer.toString();
    }

    private int execute(final String[] args) throws Exception {
        // Its okay to use logging now
        log = LoggerFactory.getLogger(getClass());

        // Boot up the container
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setName("gshell.core");
        config.setClassWorld(classWorld);

        PlexusContainer container = new DefaultPlexusContainer(config);

        //
        // TODO: We need to pass in our I/O context to the container directly
        //
        
        // Load the GShell instance
        final Shell shell = (Shell) container.lookup(Shell.class);

        // Log some information about our terminal
        Terminal term = Terminal.getTerminal();

        log.debug("Using terminal: {}", term);
        log.debug("  Supported: {}", term.isSupported());
        log.debug("  H x W: {} x {}", term.getTerminalHeight(), term.getTerminalWidth());
        log.debug("  Echo: {}", term.getEcho());
        log.debug("  ANSI: {} ", term.isANSISupported());

        if (term instanceof jline.WindowsTerminal) {
            log.debug("  Direct: {}", ((jline.WindowsTerminal)term).getDirectConsole());
        }
        
        log.debug("Started in {}", watch);

        int code = 0;

        try {
            //
            // TODO: Get the name from the branding theme
            //

            loadUserScript(shell, "gshell.profile");

            //
            // TODO: Pass interactive flags (maybe as property) so gshell knows what modfooe it is
            //

            Object result = null;
            
            if (commands != null) {
                shell.execute(commands);
            }
            else if (interactive) {
                log.debug("Starting interactive console");

                //
                // TODO: Get the name from the branding theme
                //

                loadUserScript(shell, "gshell.rc");

                IO io = shell.getIO();

                Console.Executor executor = new Console.Executor() {
                    public Result execute(String line) throws Exception {
                        try {
                            /* Object result =*/ shell.execute(line);
                        }
                        catch (ExitNotification n) {
                            //
                            // FIXME: This eats up the exit code we are to use...
                            //

                            return Result.STOP;
                        }

                        return Result.CONTINUE;
                    }
                };

                JLineConsole runner = new JLineConsole(executor, io);

                runner.setPrompter(new Console.Prompter() {
                    Renderer renderer = new Renderer();

                    public String prompt() {
                        return renderer.render("@|bold gsh| > ");
                    }
                });

                runner.setErrorHandler(new Console.ErrorHandler() {
                    public Result handleError(final Throwable error) {
                        displayError(error);
                        return Result.CONTINUE;
                    }
                });

                //
                // TODO: Get the name from the branding theme
                //
                
                runner.setHistory(new History());
                runner.setHistoryFile(new File(getUserStateDirectory(), "gshell.history"));


                // Check if there are args, and run them and then enter interactive
                if (args.length != 0) {
                    shell.execute(args);
                }

                if (!io.isQuiet()) {
                    //
                    // TODO: Use a plugable branding theme object here to get the welcome banner text...
                    //
                    
                    io.out.println(getBanner());

                    int width = term.getTerminalWidth();

                    // If we can't tell, or have something bogus then use a reasonable default
                    if (width < 1) {
                        width = 80;
                    }

                    io.out.println(StringUtils.repeat("-", width - 1));
                }

                runner.run();
            }
            else {
                result = shell.execute(args);
            }

            // If the result is a number, then pass that back to the calling shell
            if (result instanceof Number) {
                code = ((Number)result).intValue();
            }
        }
        catch (ExitNotification n) {
            log.debug("Exiting w/code: {}", n.code);

            code = n.code;
        }
        catch (Throwable t) {
            io.err.println("FATAL: " + t);
            t.printStackTrace(io.err);
            
            code = 1;
        }

        log.debug("Exiting with code: {}, after running for: {}", code, watch);

        return code;
    }

    //
    // Bootstrap
    //

    public void run(final String[] args) throws Exception {
        assert args != null;

        // Default is to be quiet
        setConsoleLogLevel("WARN");

        CommandLineProcessor clp = new CommandLineProcessor(this);
        clp.setStopAtNonOption(true);
        clp.process(args);

        //
        // TODO: Use methods to handle these...
        //
        
        if (help) {
            io.out.println(System.getProperty("program.name", "gshell") + " [options] <command> [args]");
            io.out.println();

            Printer printer = new Printer(clp);
            printer.printUsage(io.out);

            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        if (version) {
            io.out.println(Version.getInstance());
            io.out.println();
            io.out.flush();

            System.exit(0);
        }

        final AtomicReference<Integer> codeRef = new AtomicReference<Integer>();

        Runtime.getRuntime().addShutdownHook(new Thread("GShell Shutdown Hook") {
            public void run() {
                if (codeRef.get() == null) {
                    // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                    // will set an exit code through the proper channels

                    io.err.println();
                    io.err.println("@|red WARNING:| Abnormal JVM shutdown detected");
                }

                io.flush();
            }
        });

        int code = execute(this.args.toArray(new String[this.args.size()]));
        codeRef.set(code);

        System.exit(code);
    }
    
    public static void main(final String[] args, final ClassWorld world) throws Exception {
        Main main = new Main(world);
        main.run(args);
    }

    public static void main(final String[] args) throws Exception {
        main(args, new ClassWorld("gshell.legacy", Thread.currentThread().getContextClassLoader()));
    }
}


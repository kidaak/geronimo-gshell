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

package org.apache.geronimo.gshell.cli;

import org.codehaus.classworlds.ClassWorld;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gshell.GShell;
import org.apache.geronimo.gshell.console.IO;

import org.apache.geronimo.gshell.util.Version;
import org.apache.geronimo.gshell.util.Banner;

/**
 * ???
 *
 * @version $Id$
 */
public class Main
{
    private static void setConsoleLogLevel(final String level) {
        System.setProperty("gshell.log.console.level", level);
    }
    
    private static void setPropertyFrom(final String namevalue) {
        String name, value;
        int j = namevalue.indexOf("=");
        
        if (j == -1) {
            name = namevalue;
            value = "true";
        }
        else {
            name = namevalue.substring(0, j);
            value = namevalue.substring(j + 1, namevalue.length());
        }
        name = name.trim();
        
        System.setProperty(name, value);
    }
    
    public static void main(final String[] args, final ClassWorld world) throws Exception {
        assert args != null;
        assert world != null;
        
        // Default is to be quiet
        setConsoleLogLevel("WARN");
        boolean interactive = false;
        
        IO io = new IO();
        
        Options options = new Options();
        
        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription("Display this help message")
            .create('h'));
        
        options.addOption(OptionBuilder.withLongOpt("version")
            .withDescription("Display GShell version")
            .create('V'));
        
        options.addOption(OptionBuilder.withLongOpt("define")
            .withDescription("Define a system property")
            .hasArg()
            .withArgName("name=value")
            .create('D'));
        
        options.addOption(OptionBuilder.withLongOpt("interactive")
            .withDescription("Run in interactive mode")
            .create('i'));
        
        options.addOption(OptionBuilder.withLongOpt("debug")
            .withDescription("Enable DEBUG output")
            .create());
        
        options.addOption(OptionBuilder.withLongOpt("verbose")
            .withDescription("Enable INFO output")
            .create());
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args, true);
        
        if (line.hasOption('h')) {
            io.out.println(Banner.getBanner());
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                io.out,
                80, // width
                "gshell [options] <command> [args]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage
            
            io.out.println();
            io.out.flush();
            
            System.exit(0);
        }
        
        if (line.hasOption('V')) {
            io.out.println(Banner.getBanner());
            io.out.println(Version.getInstance());
            io.out.println();
            io.out.flush();
            
            System.exit(0);
        }
        
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');
            
            for (int i=0; i<values.length; i++) {
                setPropertyFrom(values[i]);
            }
        }
        
        // If --debug is set it wins over --verbose
        if (line.hasOption("debug")) {
            setConsoleLogLevel("DEBUG");
        }
        else if (line.hasOption("verbose")) {
            setConsoleLogLevel("INFO");
        }
        
        if (line.hasOption('i')) {
            interactive = true;
        }
        
        //
        // TODO: Need to pass GShell the ClassWorld, so that the application can add to it if needed
        //
        
        // Startup the shell
        GShell gshell = new GShell(io);
        String[] _args = line.getArgs();
        
        // Force interactive if there are no args
        if (_args.length == 0) {
            interactive = true;
        }
        
        if (interactive) {
            //
            // TODO: Need to check if there are args, and run them and then enter interactive
            //
            throw new Error("Interative mode not implemented... yet");
        }
        else {
            int status = gshell.execute(_args);
            System.exit(status);
        }
        
        //
        // TODO: Run interactive
        //
    }
    
    public static void main(final String[] args) throws Exception {
        assert args != null;
        
        ClassWorld world = new ClassWorld();
        main(args, world);
    }
}

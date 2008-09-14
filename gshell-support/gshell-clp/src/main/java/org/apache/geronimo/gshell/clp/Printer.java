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

package org.apache.geronimo.gshell.clp;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.geronimo.gshell.clp.handler.Handler;
import org.apache.geronimo.gshell.i18n.MessageSource;
import org.apache.geronimo.gshell.i18n.ResourceNotFoundException;

/**
 * Helper to print formatted help and usage text.
 *
 * @version $Rev$ $Date$
 */
public class Printer
{
    private CommandLineProcessor processor;

    private MessageSource messages;

    public Printer(final CommandLineProcessor processor) {
        assert processor != null;
        
        this.processor = processor;
    }

    public void setMessageSource(final MessageSource messages) {
        assert messages != null;

        this.messages = messages;
    }

    /**
     * Get the help text for the given descriptor, using any configured messages for i18n support.
     */
    private String getHelpText(final Descriptor descriptor) {
        assert descriptor != null;

        String message = descriptor.getDescription();

        // If we have i18n messages for the command, then try to resolve the message further using the message as the code
        if (messages != null) {
            String code = message;

            // If there is no code, then generate one
            if (code == null) {
                if (descriptor instanceof ArgumentDescriptor) {
                    code = "argument." + descriptor.getId();
                }
                else {
                    code = "option." + descriptor.getId();
                }
            }

            // Resolve the text in the message source
            try {
                message = messages.getMessage(code);
            }
            catch (ResourceNotFoundException e) {
                // Just use the code as the message
            }
        }

        return message;
    }

    private String getMetaVariable(final Handler handler) {
        assert handler != null;

        String token = handler.descriptor.getMetaVar();
        if (token == null) {
            token = handler.getDefaultMetaVariable();
        }

        if (token == null) {
            return null;
        }

        return token;
    }

    private String getNameAndMeta(final Handler handler) {
        assert handler != null;

        String str = (handler.descriptor instanceof ArgumentDescriptor) ? "" : handler.descriptor.toString();
    	String meta = getMetaVariable(handler);

        if (meta != null) {
            if (str.length() > 0) {
                str += " ";
            }
            str += meta;
    	}
        
        return str;
    }

    private int getPrefixLen(final Handler handler) {
        assert handler != null;

        String helpText = getHelpText(handler.descriptor);
        if (helpText == null) {
            return 0;
        }

        return getNameAndMeta(handler).length();
    }

    public void printUsage(final Writer writer, final String name) {
        assert writer != null;

        PrintWriter out = new PrintWriter(writer);

        List<Handler> argumentHandlers = new ArrayList<Handler>();
        argumentHandlers.addAll(processor.getArgumentHandlers());

        List<Handler> optionHandlers = new ArrayList<Handler>();
        optionHandlers.addAll(processor.getOptionHandlers());

        // For display purposes, we like the argument handlers in argument order, but the option handlers in alphabetical order
        Collections.sort(optionHandlers, new Comparator<Handler>() {
            public int compare(Handler a, Handler b) {
                return a.descriptor.toString().compareTo(b.descriptor.toString());
            }
        });

        //
        // TODO: i18n, pull for standard messages, not from command's messages
        //
        
        if (name != null) {
        	String syntax = "syntax: " + name;
        	if (!optionHandlers.isEmpty()) {
        		syntax += " [options]";
        	}
        	if (!argumentHandlers.isEmpty()) {
        		syntax += " [arguments]";
        	}
        	out.println(syntax);
        	out.println();
        }

        // Compute the maximum length of the syntax column
        int len = 0;

        for (Handler handler : optionHandlers) {
            int curLen = getPrefixLen(handler);
            len = Math.max(len, curLen);
        }

        for (Handler handler : argumentHandlers) {
            int curLen = getPrefixLen(handler);
            len = Math.max(len, curLen);
        }

        // And then render the handler usage
        if (!argumentHandlers.isEmpty()) {
        	out.println("arguments:");
        }
        for (Handler handler : argumentHandlers) {
            printHandler(out, handler, len);
        }

        if (!optionHandlers.isEmpty()) {
        	out.println();
        	out.println("options:");
        }
        for (Handler handler : optionHandlers) {
            printHandler(out, handler, len);
        }

        out.flush();
    }

    public void printUsage(final Writer writer) {
        printUsage(writer, null);
    }

    private void printHandler(final PrintWriter out, final Handler handler, final int len) {
        assert out != null;
        assert handler != null;

        //
        // TODO: Expose these as configurables
        //
        
        int terminalWidth = 80;
        String prefix = "  ";
        String separator = "    ";
        int prefixSeperatorWidth = prefix.length() + separator.length();
        int descriptionWidth = terminalWidth - len - prefixSeperatorWidth;

        // Only render if there is help-text, else its hidden
        String desc = getHelpText(handler.descriptor);
        if (desc == null) {
            return;
        }

        // Render the prefix and syntax
        String nameAndMeta = getNameAndMeta(handler);
        out.print(prefix);
        out.print(nameAndMeta);

        // Render the seperator
        for (int i = nameAndMeta.length(); i < len; ++i) {
            out.print(' ');
       	}
        out.print(separator);

        // Render the description splitting it over multipule lines if its longer than column size
        while (desc != null && desc.length() > 0) {
            //
            // FIXME: Only split on words
            //

            int i = desc.indexOf('\n');

            if (i >= 0 && i <= descriptionWidth) {
                out.println(desc.substring(0, i));
                desc = desc.substring(i + 1);

                if (desc.length() > 0) {
                    indent(out, len + prefixSeperatorWidth);
                }

                continue;
            }

            if (desc.length() <= descriptionWidth) {
                out.println(desc);
                break;
            }

            out.println(desc.substring(0, descriptionWidth));
            desc = desc.substring(descriptionWidth);
            indent(out, len + prefixSeperatorWidth);
        }
    }

    private void indent(final PrintWriter out, int i) {
        assert out != null;

        for (; i>0; i--) {
            out.print(' ');
        }
    }
}

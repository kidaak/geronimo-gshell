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

package org.apache.geronimo.gshell.container;

import org.apache.geronimo.gshell.command.CommandContainer;
import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.Executable;
import org.apache.geronimo.gshell.plexus.GShellPlexusContainer;
import org.apache.geronimo.gshell.common.Arguments;
import org.apache.geronimo.gshell.clp.CommandLineProcessor;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Component(role=CommandContainer.class)
public class DefaultCommandContainer
    implements CommandContainer, Contextualizable
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GShellPlexusContainer container;

    @Configuration("invalid") // Just to mark what this is used for, since we have to configure a default value
    private String commandId;

    // Contextualizable
    
    public void contextualize(final Context context) throws ContextException {
        assert context != null;

        container = (GShellPlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        assert container != null;
        
        log.debug("Container: {}", container);
    }

    // CommandContainer

    public String getId() {
        return commandId;
    }

    public String getDescription() {
        assert container != null;
        
        return container.getComponentDescriptor(CommandContainer.class.getName(), commandId).getDescription();
    }

    public Executable getExecutable() {
        assert container != null;

        try {
            return container.lookupComponent(Executable.class, commandId);
        }
        catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(final CommandContext context, final Object... args) throws Exception {
        assert context != null;
        assert args != null;

        log.trace("Executing; context={}, args={}", context, args);

        Executable executable = getExecutable();

        // TODO: Handle logging muck
        
        // TODO: Bind context, io and variables

        // Process command line options/arguments
        CommandLineProcessor clp = new CommandLineProcessor(executable);
        clp.process(Arguments.toStringArray(args));

        //
        // TODO: Need to augment the clp to allow it to handle a set of objects, so we can use a nested object here to inject --help support automatically
        //

        Object result = executable.execute(context, args);

        log.trace("Result: {}", result);

        return result;
    }
}
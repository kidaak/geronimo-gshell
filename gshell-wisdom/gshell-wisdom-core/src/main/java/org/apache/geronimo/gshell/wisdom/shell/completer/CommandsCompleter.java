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

package org.apache.geronimo.gshell.wisdom.shell.completer;

import org.apache.geronimo.gshell.console.completer.Completer;
import org.apache.geronimo.gshell.event.Event;
import org.apache.geronimo.gshell.event.EventListener;
import org.apache.geronimo.gshell.event.EventManager;
import org.apache.geronimo.gshell.registry.CommandRegistry;
import org.apache.geronimo.gshell.wisdom.registry.CommandRegisteredEvent;
import org.apache.geronimo.gshell.wisdom.registry.CommandRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * {@link Completer} for commands.
 *
 * @version $Rev$ $Date$
 */
public class CommandsCompleter
    implements Completer
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventManager eventManager;

    @Autowired
    private CommandRegistry commandRegistry;

    @PostConstruct
    public void init() {
        log.debug("Initializing");

        // Populate the initial list of command names
        Collection<String> names = commandRegistry.getCommandNames();
        // TODO:

        // Register for updates to command registrations
        eventManager.addListener(new EventListener() {
            public void onEvent(final Event event) throws Exception {
                if (event instanceof CommandRegisteredEvent) {
                    CommandRegisteredEvent targetEvent = (CommandRegisteredEvent)event;
                    // TODO:
                }
                else if (event instanceof CommandRemovedEvent) {
                    CommandRemovedEvent targetEvent = (CommandRemovedEvent)event;
                    // TODO:
                }
            }
        });
    }

    public int complete(final String buffer, final int cursor, final List candidates) {
        // TODO:
        return -1;
    }
}
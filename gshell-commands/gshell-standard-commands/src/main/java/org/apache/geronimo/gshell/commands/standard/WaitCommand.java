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

import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.Command;

//
// HACK: This is a temporary to handle shells which need to keep around after running
//       commands that return.  Need to have better jobs support to get rid of this.
//

/**
 * Wait... just blocks command execution.
 *
 * @version $Rev$ $Date$
 */
public class WaitCommand
    extends CommandSupport
{
    public WaitCommand() {
        super("wait");
    }

    protected Object doExecute(final Object[] args) throws Exception {
        assert args != null;

        log.info("Waiting...");

        synchronized (this) {
            wait();
        }
        
        return Command.SUCCESS;
    }
}

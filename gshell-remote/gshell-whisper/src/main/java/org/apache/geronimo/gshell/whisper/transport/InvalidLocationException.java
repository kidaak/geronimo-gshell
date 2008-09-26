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

package org.apache.geronimo.gshell.whisper.transport;

import java.net.URI;

/**
 * Thrown to indicate an invalid transport location was configured.
 *
 * @version $Rev$ $Date$
 */
public class InvalidLocationException
    extends TransportException
{
    private static final long serialVersionUID = 1;
    
    public InvalidLocationException(final URI location) {
        super("Invalid location: " + location);
    }

    public InvalidLocationException(final URI location, final String message) {
        super("Invalid location: " + location + "; " + message);
    }
}

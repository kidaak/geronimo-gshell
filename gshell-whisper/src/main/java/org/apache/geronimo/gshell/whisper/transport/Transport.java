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

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.EventListener;

import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.common.WriteFuture;

/**
 * Provides the client-side protocol interface.
 *
 * @version $Rev$ $Date$
 */
public interface Transport<T extends IoConnector>
    extends Closeable
{
    URI getRemote();

    URI getLocal();

    T getConnector();

    IoSession getSession();

    void close();

    // Messages

    WriteFuture send(Object msg) throws Exception;

    Message request(Message msg, Duration timeout) throws Exception;

    Message request(Message msg) throws Exception;

    //
    // Streams
    //

    InputStream getInputStream();

    OutputStream getOutputStream();

    OutputStream getErrorStream();

    //
    // Listeners
    //

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener
        extends EventListener
    {
        //
        // TODO:
        //
    }
    
    //
    // Configuration
    //

    void setConfiguration(Configuration config);

    Configuration getConfiguration();

    interface Configuration
    {
        IoHandler getHandler();

        void setHandler(IoHandler handler);

        ThreadModel getThreadModel();

        void setThreadModel(ThreadModel threadModel);
    }
}
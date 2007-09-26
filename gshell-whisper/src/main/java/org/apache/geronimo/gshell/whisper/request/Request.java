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

package org.apache.geronimo.gshell.whisper.request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.geronimo.gshell.common.Duration;
import org.apache.geronimo.gshell.common.tostring.ToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.whisper.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request message.
 *
 * @version $Rev$ $Date$
 */
public class Request
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    final Lock lock = new ReentrantLock();

    private final BlockingQueue<Object> responses = new LinkedBlockingQueue<Object>();

    private final Message message;

    private final Duration timeout;

    private volatile boolean endOfResponses;

    private volatile boolean signaled;

    public Request(final Message message, final Duration timeout) {
        assert message != null;
        assert timeout != null;

        this.message = message;
        this.timeout = timeout;
    }

    public Request(final Message message, long timeout, final TimeUnit timeoutUnit) {
        this(message, new Duration(timeout, timeoutUnit));
    }

    public int hashCode() {
        return getId().hashCode();
    }

    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (!(obj instanceof Request)) {
            return false;
        }

        Request request = (Request) obj;

        return getId().equals(request.getId());
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("signaled", signaled)
                .append("message", message)
                .toString();
    }

    public Message getMessage() {
        return message;
    }

    public Message.ID getId() {
        return getMessage().getId();
    }

    public Duration getTimeout() {
        return timeout;
    }

    public boolean hasResponse() {
        return !responses.isEmpty();
    }

    public Response awaitResponse() throws RequestTimeoutException, InterruptedException {
        chechEndOfResponses();

        log.debug("Waiting for response");

        Response resp = decodeResponse(responses.take());

        log.trace("Received response: {}", resp);

        return resp;
    }

    public Response awaitResponse(final long timeout, final TimeUnit unit) throws RequestTimeoutException, InterruptedException {
        chechEndOfResponses();

        log.debug("Polling for response");

        Response resp = decodeResponse(responses.poll(timeout, unit));

        if (log.isTraceEnabled()) {
            if (resp != null) {
                log.trace("Received response: {}", resp);
            }
            else {
                log.trace("Operation timed out before the response was signaled");
            }
        }

        return resp;
    }

    public Response awaitResponseUninterruptibly() throws RequestTimeoutException {
        while (true) {
            try {
                return awaitResponse();
            }
            catch (InterruptedException ignore) {}
        }
    }

    private Response decodeResponse(final Object obj) {
        if (obj instanceof Response) {
            return (Response) obj;
        }
        else if (obj == null) {
            return null;
        }
        else if (obj == RequestTimeoutException.class) {
            throw new RequestTimeoutException(getId());
        }

        // This should never happen
        throw new InternalError();
    }

    private void chechEndOfResponses() {
        if (endOfResponses && responses.isEmpty()) {
            throw new IllegalStateException("All responses has been retrieved already");
        }
    }

    private void queueResponse(final Object answer) {
        signaled = true;

        responses.add(answer);
    }

    void signal(final Response response) {
        assert response != null;

        lock.lock();

        try {
            if (log.isTraceEnabled()) {
                log.debug("Signal response: {}", response);
            }
            else {
                log.debug("Signal response: {}", response.getRequest().getId());
            }

            queueResponse(response);

            if (response.getType() != Response.Type.PARTIAL) {
                endOfResponses = true;
            }
        }
        finally {
            lock.unlock();
        }
    }

    void timeout() {
        lock.lock();

        try {
            log.debug("Timeout");

            queueResponse(RequestTimeoutException.class);

            endOfResponses = true;
        }
        finally {
            lock.unlock();
        }
    }

    boolean isSignaled() {
        lock.lock();

        try {
            return signaled;
        }
        finally {
            lock.unlock();
        }
    }
}

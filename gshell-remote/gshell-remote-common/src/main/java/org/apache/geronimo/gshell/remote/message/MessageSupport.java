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

package org.apache.geronimo.gshell.remote.message;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;
import org.apache.geronimo.gshell.remote.marshall.Marshaller;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

/**
 * Support for {@link Message} implementations.
 *
 * @version $Rev$ $Date$
 */
public class MessageSupport
    implements Message
{
    private static final IDGenerator ID_GENERATOR = /*new UuidMessageID.Generator();*/ new LongMessageID.Generator();

    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    private MessageType type;

    private ID id;

    private ID cid;

    private Long sequence;

    private long timestamp;
    
    private transient IoSession session;

    private transient boolean frozen;

    protected MessageSupport(final MessageType type) {
        assert type != null;
        
        this.type = type;

        this.timestamp = System.currentTimeMillis();

        // late init id and sequence
    }

    public int hashCode() {
        return getId().hashCode();
    }
    
    //
    // TODO: Add equals()
    //
    
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public MessageType getType() throws IOException {
        return type;
    }

    public ID getId() {
        if (id == null) {
            id = ID_GENERATOR.generate();
        }

        return id;
    }
    
    public ID getCorrelationId() {
        return cid;
    }

    public void setCorrelationId(final ID id) {
        ensureWritable();

        assert id != null;

        this.cid = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSequence() {
        if (sequence == null) {
            sequence = SEQUENCE_COUNTER.getAndIncrement();
        }

        return sequence;
    }

    public void setSession(final IoSession session) {
        ensureWritable();
        
        this.session = session;
    }

    public IoSession getSession() {
        if (session == null) {
            throw new IllegalStateException("Session has not been attached");
        }
        
        return session;
    }

    protected void ensureWritable() {
        if (frozen) {
            throw new IllegalStateException("Message is frozen");
        }
    }

    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void process(final MessageVisitor visitor) throws Exception {
        // Non-operation
    }

    public WriteFuture reply(final Message msg) {
        assert msg != null;

        IoSession session = getSession();

        msg.setCorrelationId(getId());
        msg.freeze();

        return session.write(msg);
    }

    public void readExternal(final ByteBuffer in) throws Exception {
        assert in != null;

        id = (ID) Marshaller.unmarshal(in);

        cid = (ID) Marshaller.unmarshal(in);

        timestamp = in.getLong();

        sequence = in.getLong();
    }

    public void writeExternal(final ByteBuffer out) throws Exception {
        assert out != null;

        Marshaller.marshal(out, getId());

        Marshaller.marshal(out, getCorrelationId());

        out.putLong(getTimestamp());

        out.putLong(getSequence());
    }
}
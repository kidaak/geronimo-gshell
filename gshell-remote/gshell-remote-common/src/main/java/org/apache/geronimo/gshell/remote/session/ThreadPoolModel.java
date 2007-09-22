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

package org.apache.geronimo.gshell.remote.session;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.gshell.remote.util.NamedThreadFactory;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class ThreadPoolModel
    implements ThreadModel
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ThreadPoolExecutor threadPool;

    private final ExecutorFilter filter;

    public ThreadPoolModel(final String name) {
        assert name != null;

        ThreadGroup group = new ThreadGroup(name);

        ThreadFactory tf = new NamedThreadFactory(name, group);

        //
        // TODO: See which is better SynchronousQueue<Runnable> or LinkedBlockingQueue<Runnable>
        //

        threadPool = new ThreadPoolExecutor(
                1,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                tf,
                new ThreadPoolExecutor.AbortPolicy());

        filter = new ExecutorFilter(threadPool);
    }

    public void close() {
        //
        // FIXME: This causes some problems when a rsh client closes, like:
        //
        //        java.security.AccessControlException: access denied (java.lang.RuntimePermission modifyThread)
        //

        /*
        List<Runnable> pending = threadPool.shutdownNow();

        if (!pending.isEmpty()) {
            log.warn("There were {} pending tasks which have not been run", pending.size());
        }
        */
    }
    
    //
    // ThreadModel
    //

    public void buildFilterChain(final IoFilterChain chain) throws Exception {
        assert chain != null;
        
        chain.addFirst(getClass().getSimpleName(), filter);
    }
}
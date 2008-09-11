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


package org.apache.geronimo.gshell.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;

import java.net.URL;
import java.util.List;

/**
 * An abstraction of a container of beans.
 *
 * @version $Rev$ $Date$
 */
public interface BeanContainer
{
    BeanContainer getParent();
    
    <T> T getBean(Class<T> type) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    void publish(ApplicationEvent event);

    void addListener(ApplicationListener listener);

    BeanContainer createChild(String id, List<URL> classPath) throws DuplicateRealmException;
}

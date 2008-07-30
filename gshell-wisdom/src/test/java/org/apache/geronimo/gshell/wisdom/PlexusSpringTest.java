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

package org.apache.geronimo.gshell.wisdom;

import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.spring.PlexusToSpringUtils;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class PlexusSpringTest
    extends PlexusInSpringTestCase
{
    public void testIdConvert() throws Exception {
        System.out.println("ID: " + PlexusToSpringUtils.buildSpringId(MyPlexusComponent.class, "foo"));
    }

    public void testListBeans() throws Exception {
        for (String name : applicationContext.getBeanDefinitionNames()) {
            System.out.println(name);
        }
    }

    public void testSpringBean() throws Exception {
        MySpringComponent springComponent = (MySpringComponent) applicationContext.getBean("springComponent");
        assertNotNull(springComponent);
        assertNotNull(springComponent.getPlexusComponent());
    }
}
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

package org.apache.geronimo.gshell.model.application;

import junit.framework.TestCase;
import org.apache.geronimo.gshell.model.common.RemoteRepository;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Test for the {@link ApplicationModelMarshaller} class.
 *
 * @version $Rev$ $Date$
 */
public class ApplicationMarshallerTest
    extends TestCase
{
    private ApplicationModelMarshaller marshaller = new ApplicationModelMarshaller();
    
    public void testMarshal1() throws Exception {
        ApplicationModel root = new ApplicationModel();

        root.setGroupId("app.test");
        root.setArtifactId("app-test");
        root.setVersion("1.0");
        root.setName("App Test");
        root.setDescription("A test app descriptor.");

        Properties props = new Properties();
        props.setProperty("a", "b");
        root.setProperties(props);

        RemoteRepository sr1 = new RemoteRepository();
        sr1.setLocation("foo:bar");
        root.add(sr1);

        DependencyArtifact d1 = new DependencyArtifact();
        d1.setGroupId("a");
        d1.setArtifactId("b");
        d1.setVersion("c");
        root.add(d1);

        DependencyGroup g1 = new DependencyGroup();
        g1.setGroupId("d");
        g1.setVersion("e");
        root.add(g1);
        
        DependencyArtifact d2 = new DependencyArtifact();
        d2.setArtifactId("f");
        g1.add(d2);

        DependencyArtifact d3 = new DependencyArtifact();
        d3.setArtifactId("g");
        d3.setClassifier("h");
        g1.add(d3);

        String xml = marshaller.marshal(root);
        assertNotNull(xml);

        System.out.println(xml);
    }

    public void testUnmarshal1_FromStream() throws Exception {
        InputStream input = getClass().getResourceAsStream("application1.xml");

        ApplicationModel root = marshaller.unmarshal(input);

        root.getDependencies(true);
        
        System.out.println(root);
    }

    public void testUnmarshal1_FromURL() throws Exception {
        URL url = getClass().getResource("application1.xml");

        ApplicationModel root = marshaller.unmarshal(url);

        root.getDependencies(true);

        System.out.println(root);
    }
}
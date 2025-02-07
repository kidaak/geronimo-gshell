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

package org.apache.geronimo.gshell.commandline.parser;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;

/**
 * Unit tests for the {@link CommandLineParserVisitor} usage.
 *
 * @version $Rev$ $Date$
 */
public class CommandLineParserVisitorTest
    extends TestCase
{
    private ASTCommandLine parse(final String input) throws ParseException {
        assert input != null;

        Reader reader = new StringReader(input);
        CommandLineParser parser = new CommandLineParser();
        ASTCommandLine cl = parser.parse(reader);

        assertNotNull(cl);

        return cl;
    }

    public void testVisitor1() throws Exception {
        String input = "a \"b\" 'c' d";

        ASTCommandLine cl = parse(input);

        MockCommandLineVisitor v = new MockCommandLineVisitor();

        Object result = cl.jjtAccept(v, null);

        assertNull(v.simpleNode);
        assertNotNull(v.commandLine);
        assertNotNull(v.expression);
        assertNotNull(v.quotedString);
        assertNotNull(v.opaqueString);
        assertNotNull(v.plainString);
    }

    private static class MockCommandLineVisitor
        implements CommandLineParserVisitor
    {
        private SimpleNode simpleNode;
        private ASTCommandLine commandLine;
        private ASTExpression expression;
        private ASTQuotedString quotedString;
        private ASTOpaqueString opaqueString;
        private ASTPlainString plainString;

        public Object visit(SimpleNode node, Object data) {
            this.simpleNode = node;

            return node.childrenAccept(this, data);
        }

        public Object visit(ASTCommandLine node, Object data) {
            this.commandLine = node;

            return node.childrenAccept(this, data);
        }

        public Object visit(ASTExpression node, Object data) {
            this.expression = node;

            return node.childrenAccept(this, data);
        }

        public Object visit(ASTQuotedString node, Object data) {
            this.quotedString = node;

            return node.childrenAccept(this, data);
        }

        public Object visit(ASTOpaqueString node, Object data) {
            this.opaqueString = node;

            return node.childrenAccept(this, data);
        }

        public Object visit(ASTPlainString node, Object data) {
            this.plainString = node;

            return node.childrenAccept(this, data);
        }
    }
}

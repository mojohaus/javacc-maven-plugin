package org.codehaus.mojo.javacc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests <code>GrammarInfo</code>.
 *
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class GrammarInfoTest {

    @Test
    void testInvalidFile() {
        try {
            new GrammarInfo(new File("").getAbsoluteFile(), "bad");
            fail("Missing IO exception");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    void testGetGrammarFile() throws Exception {
        File grammarFile = getGrammar("Parser1.jj");
        GrammarInfo info = new GrammarInfo(grammarFile.getParentFile(), grammarFile.getName());
        assertEquals(grammarFile, info.getGrammarFile());
    }

    @Test
    void testGetRelativeGrammarFile() throws Exception {
        File grammarFile = getGrammar("Parser1.jj");
        GrammarInfo info = new GrammarInfo(grammarFile.getParentFile(), grammarFile.getName());
        assertEquals(grammarFile.getName(), info.getRelativeGrammarFile());
    }

    @Test
    void testGetPackageNameDeclaredPackageOverwrite() throws Exception {
        File grammarFile = getGrammar("Parser1.jj");
        GrammarInfo info = new GrammarInfo(grammarFile.getParentFile(), grammarFile.getName(), "org.test");
        assertEquals("org.test", info.getParserPackage());
    }

    @Test
    void testGetPackageNameDeclaredPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser1.jj");
        assertEquals("org.codehaus.mojo.javacc.test", info.getParserPackage());
    }

    @Test
    void testGetPackageNameDefaultPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser2.jj");
        assertEquals("", info.getParserPackage());
    }

    @Test
    void testGetPackageDirectoryDeclaredPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser1.jj");
        assertEquals(new File("org/codehaus/mojo/javacc/test").getPath(), info.getParserDirectory());
    }

    @Test
    void testGetPackageDirectoryDefaultPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser2.jj");
        assertEquals(new File("").getPath(), info.getParserDirectory());
    }

    @Test
    void testGetParserName() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser1.jj");
        assertEquals("BasicParser", info.getParserName());
    }

    @Test
    void testGetParserFileDeclaredPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser1.jj");
        assertEquals(new File("org/codehaus/mojo/javacc/test/BasicParser.java").getPath(), info.getParserFile());
    }

    @Test
    void testGetParserFileDefaultPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser2.jj");
        assertEquals(new File("SimpleParser.java").getPath(), info.getParserFile());
    }

    @Test
    void testResolvePackageNameDeclaredPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser1.jj");
        assertEquals("org.codehaus.mojo.javacc.test.node", info.resolvePackageName("*.node"));
        assertEquals("org.codehaus.mojo.javacc.testnode", info.resolvePackageName("*node"));
        assertEquals("node", info.resolvePackageName("node"));
    }

    @Test
    void testResolvePackageNameDefaultPackage() throws Exception {
        GrammarInfo info = newGrammarInfo("Parser2.jj");
        assertEquals("node", info.resolvePackageName("*.node"));
        assertEquals("node", info.resolvePackageName("*node"));
        assertEquals("node", info.resolvePackageName("node"));
    }

    private GrammarInfo newGrammarInfo(String resource) throws Exception {
        File grammarFile = getGrammar(resource);
        File sourceDir = grammarFile.getParentFile();
        return new GrammarInfo(sourceDir, grammarFile.getName());
    }

    private File getGrammar(String resource) throws Exception {
        URL res = getClass().getResource('/' + resource);
        Objects.requireNonNull(res);
        return new File(new URI(res.toString()));
    }
}

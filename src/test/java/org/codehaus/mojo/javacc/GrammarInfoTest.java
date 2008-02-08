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

import junit.framework.TestCase;

/**
 * Tests <code>GrammarInfo</code>.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class GrammarInfoTest
    extends TestCase
{

    public void testInvalidFile()
        throws Exception
    {
        File grammarFile = new File( "" );
        try
        {
            new GrammarInfo( grammarFile );
            fail( "Missing IO exception" );
        }
        catch ( IOException e )
        {
            // expected
        }
    }

    public void testGetGrammarFile()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( grammarFile, info.getGrammarFile() );
    }

    public void testGetPackageNameDeclaredPackageOverwrite()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile, new File( "org/test" ) );
        assertEquals( "org.test", info.getPackageName() );
    }

    public void testGetPackageNameDeclaredPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( "org.codehaus.mojo.javacc.test", info.getPackageName() );
    }

    public void testGetPackageNameDefaultPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser2.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( "", info.getPackageName() );
    }

    public void testGetPackageDirectoryDeclaredPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( new File( "org/codehaus/mojo/javacc/test" ), info.getPackageDirectory() );
    }

    public void testGetPackageDirectoryDefaultPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser2.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( new File( "" ), info.getPackageDirectory() );
    }

    public void testGetParserName()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( "BasicParser", info.getParserName() );
    }

    public void testGetParserFileDeclaredPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( new File( "org/codehaus/mojo/javacc/test/BasicParser.java" ), info.getParserFile() );
    }

    public void testGetParserFileDefaultPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser2.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( new File( "SimpleParser.java" ), info.getParserFile() );
    }

    public void testResolvePackageNameDeclaredPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser1.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( "org.codehaus.mojo.javacc.test.node", info.resolvePackageName( "*.node" ) );
        assertEquals( "org.codehaus.mojo.javacc.testnode", info.resolvePackageName( "*node" ) );
        assertEquals( "node", info.resolvePackageName( "node" ) );
    }

    public void testResolvePackageNameDefaultPackage()
        throws Exception
    {
        File grammarFile = getGrammar( "Parser2.jj" );
        GrammarInfo info = new GrammarInfo( grammarFile );
        assertEquals( "node", info.resolvePackageName( "*.node" ) );
        assertEquals( "node", info.resolvePackageName( "*node" ) );
        assertEquals( "node", info.resolvePackageName( "node" ) );
    }

    private File getGrammar( String resource )
        throws Exception
    {
        return new File( new URI( getClass().getResource( '/' + resource ).toString() ) );
    }

}

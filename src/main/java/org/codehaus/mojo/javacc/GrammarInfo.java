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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.FileUtils;

/**
 * This bean holds some output related information about a JavaCC grammar file. It assists in determining the exact
 * output location for the generated parser file.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
class GrammarInfo
{

    /**
     * The absolute path to the grammar file.
     */
    private final File grammarFile;

    /**
     * The declared package for the generated parser (e.g. "org.apache").
     */
    private final String packageName;

    /**
     * The path to the package directory (relative to the source root directory, e.g. "org/apache").
     */
    private final File packageDirectory;

    /**
     * The simple name of the generated parser (e.g. "MyParser").
     */
    private final String parserName;

    /**
     * The path to the parser file (relative to the source root directory, e.g. "org/apache/MyParser.java")
     */
    private final File parserFile;

    /**
     * Creates a new info from the specified grammar file.
     * 
     * @param inputFile The absolute path to the grammar file, must not be <code>null</code>.
     * @throws IOException If reading the grammar file failed.
     */
    public GrammarInfo( File inputFile )
        throws IOException
    {
        this( inputFile, (File) null );
    }

    /**
     * Creates a new info from the specified grammar file.
     * 
     * @param inputFile The absolute path to the grammar file, must not be <code>null</code>.
     * @param packageDir The relative directory path for the generated parser, may be <code>null</code> to use the
     *            package declaration from the grammar file.
     * @throws IOException If reading the grammar file failed.
     */
    public GrammarInfo( File inputFile, String packageDir )
        throws IOException
    {
        this( inputFile, ( packageDir != null ) ? new File( packageDir ) : null );
    }

    /**
     * Creates a new info from the specified grammar file.
     * 
     * @param inputFile The absolute path to the grammar file, must not be <code>null</code>.
     * @param packageDir The relative directory path for the generated parser, may be <code>null</code> to use the
     *            package declaration from the grammar file.
     * @throws IOException If reading the grammar file failed.
     */
    public GrammarInfo( File inputFile, File packageDir )
        throws IOException
    {
        this.grammarFile = inputFile;

        // NOTE: JavaCC uses the platform default encoding to read files, so must we
        String grammar = FileUtils.fileRead( this.grammarFile );

        if ( packageDir == null )
        {
            this.packageName = findPackageName( grammar );
            this.packageDirectory = new File( this.packageName.replace( '.', File.separatorChar ) );
        }
        else if ( packageDir.isAbsolute() )
        {
            throw new IllegalArgumentException( "package directory must be relative to source root" );
        }
        else
        {
            this.packageName = packageDir.getPath().replace( File.separatorChar, '.' );
            this.packageDirectory = packageDir;
        }

        String name = findParserName( grammar );
        if ( name.length() <= 0 )
        {
            this.parserName = FileUtils.removeExtension( this.grammarFile.getName() );
        }
        else
        {
            this.parserName = name;
        }

        if ( this.packageDirectory.getPath().length() > 0 )
        {
            this.parserFile = new File( this.packageDirectory, this.parserName + ".java" );
        }
        else
        {
            this.parserFile = new File( this.parserName + ".java" );
        }
    }

    /**
     * Extracts the declared package name from the specified grammar file.
     * 
     * @param grammar The contents of the grammar file, must not be <code>null</code>.
     * @return The declared package name or an empty string if not found.
     */
    private String findPackageName( String grammar )
    {
        final String packageDeclaration = "package\\s+([^\\s.;]+(\\.[^\\s.;]+)*)\\s*;";
        Matcher matcher = Pattern.compile( packageDeclaration ).matcher( grammar );
        if ( matcher.find() )
        {
            return matcher.group( 1 );
        }
        return "";
    }

    /**
     * Extracts the simple parser name from the specified grammar file.
     * 
     * @param grammar The contents of the grammar file, must not be <code>null</code>.
     * @return The parser name or an empty string if not found.
     */
    private String findParserName( String grammar )
    {
        final String parserBegin = "PARSER_BEGIN\\s*\\(\\s*([^\\s\\)]+)\\s*\\)";
        Matcher matcher = Pattern.compile( parserBegin ).matcher( grammar );
        if ( matcher.find() )
        {
            return matcher.group( 1 );
        }
        return "";
    }

    /**
     * Gets the absolute path to the grammar file.
     * 
     * @return The absolute path to the grammar file.
     */
    public File getGrammarFile()
    {
        return this.grammarFile;
    }

    /**
     * Gets the declared package for the generated parser (e.g. "org.apache"). This value will be an empty string if no
     * package declaration was found, it is never <code>null</code>.
     * 
     * @return The declared package for the generated parser (e.g. "org.apache").
     */
    public String getPackageName()
    {
        return this.packageName;
    }

    /**
     * Gets the path to the package directory (relative to the source root directory, e.g. "org/apache"). This value
     * will be an empty path if no package declaration was found, it is never <code>null</code>.
     * 
     * @return The relative path for the directory corresponding to the declared package, e.g. "org/apache".
     */
    public File getPackageDirectory()
    {
        return this.packageDirectory;
    }

    /**
     * Gets the simple name of the generated parser (e.g. "MyParser"). This value is never <code>null</code>.
     * 
     * @return The simple name of the generated parser (e.g. "MyParser").
     */
    public String getParserName()
    {
        return this.parserName;
    }

    /**
     * Gets the path to the parser file (relative to the source root directory, e.g. "org/apache/MyParser.java"). This
     * value is never <code>null</code>.
     * 
     * @return The path to the parser file (relative to the source root directory, e.g. "org/apache/MyParser.java")
     */
    public File getParserFile()
    {
        return this.parserFile;
    }

    /**
     * Gets a string representation of this bean. This value is for debugging purposes only.
     * 
     * @return A string representation of this bean.
     */
    public String toString()
    {
        return getGrammarFile() + " -> " + getParserFile();
    }

}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

/**
 * Provides a facade for the mojos to invoke JavaCC.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 * @see <a href="https://javacc.dev.java.net/doc/commandline.html">JavaCC Command Line Syntax</a>
 */
class JavaCC
    extends ToolFacade
{

    /**
     * The input grammar.
     */
    private File inputFile;

    /**
     * The option OUTPUT_DIRECTORY.
     */
    private File outputDirectory;

    /**
     * The option GRAMMAR_ENCODING.
     */
    private String grammarEncoding;

    /**
     * The option JDK_VERSION.
     */
    private String jdkVersion;

    /**
     * The option STATIC.
     */
    private Boolean isStatic;

    /**
     * The option LOOK_AHEAD.
     */
    private Integer lookAhead;

    /**
     * The option CHOICE_AMBIGUITY_CHECK.
     */
    private Integer choiceAmbiguityCheck;

    /**
     * The option OTHER_AMBIGUITY_CHECK.
     */
    private Integer otherAmbiguityCheck;

    /**
     * The option DEBUG_PARSER.
     */
    private Boolean debugParser;

    /**
     * The option DEBUG_LOOK_AHEAD.
     */
    private Boolean debugLookAhead;

    /**
     * The option DEBUG_TOKEN_MANAGER.
     */
    private Boolean debugTokenManager;

    /**
     * The option ERROR_REPORTING.
     */
    private Boolean errorReporting;

    /**
     * The option JAVA_UNICODE_ESCAPE.
     */
    private Boolean javaUnicodeEscape;

    /**
     * The option UNICODE_INPUT.
     */
    private Boolean unicodeInput;

    /**
     * The option IGNORE_CASE.
     */
    private Boolean ignoreCase;

    /**
     * The option COMMON_TOKEN_ACTION.
     */
    private Boolean commonTokenAction;

    /**
     * The option USER_TOKEN_MANAGER.
     */
    private Boolean userTokenManager;

    /**
     * The option USER_CHAR_STREAM.
     */
    private Boolean userCharStream;

    /**
     * The option BUILD_PARSER.
     */
    private Boolean buildParser;

    /**
     * The option BUILD_TOKEN_MANAGER.
     */
    private Boolean buildTokenManager;

    /**
     * The option TOKEN_MANAGER_USES_PARSER.
     */
    private Boolean tokenManagerUsesParser;

    /**
     * The option TOKEN_EXTENDS.
     */
    private String tokenExtends;

    /**
     * The option TOKEN_FACTORY.
     */
    private String tokenFactory;

    /**
     * The option SANITY_CHECK.
     */
    private Boolean sanityCheck;

    /**
     * The option FORCE_LA_CHECK.
     */
    private Boolean forceLaCheck;

    /**
     * The option CACHE_TOKENS.
     */
    private Boolean cacheTokens;

    /**
     * The option KEEP_LINE_COLUMN.
     */
    private Boolean keepLineColumn;

    /**
     * The option SUPPORT_CLASS_VISIBILITY_PUBLIC.
     */
    private Boolean supportClassVisibilityPublic;

    /**
     * Sets the absolute path to the grammar file to pass into JavaCC for compilation.
     * 
     * @param value The absolute path to the grammar file to pass into JavaCC for compilation.
     */
    public void setInputFile( File value )
    {
        if ( value != null && !value.isAbsolute() )
        {
            throw new IllegalArgumentException( "path is not absolute: " + value );
        }
        this.inputFile = value;
    }

    /**
     * Sets the absolute path to the output directory.
     * 
     * @param value The absolute path to the output directory for the generated parser file. If this directory does not
     *            exist yet, it is created. Note that this path should already include the desired package hierarchy
     *            because JavaCC will not append the required sub directories automatically.
     */
    public void setOutputDirectory( File value )
    {
        if ( value != null && !value.isAbsolute() )
        {
            throw new IllegalArgumentException( "path is not absolute: " + value );
        }
        this.outputDirectory = value;
    }

    /**
     * Sets the option GRAMMAR_ENCODING.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setGrammarEncoding( String value )
    {
        this.grammarEncoding = value;
    }

    /**
     * Sets the option JDK_VERSION.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setJdkVersion( String value )
    {
        this.jdkVersion = value;
    }

    /**
     * Sets the option STATIC.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setStatic( Boolean value )
    {
        this.isStatic = value;
    }

    /**
     * Sets the option LOOK_AHEAD.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setLookAhead( Integer value )
    {
        this.lookAhead = value;
    }

    /**
     * Sets the option CHOICE_AMBIGUITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setChoiceAmbiguityCheck( Integer value )
    {
        this.choiceAmbiguityCheck = value;
    }

    /**
     * Sets the option OTHER_AMBIGUITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setOtherAmbiguityCheck( Integer value )
    {
        this.otherAmbiguityCheck = value;
    }

    /**
     * Sets the option DEBUG_PARSER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setDebugParser( Boolean value )
    {
        this.debugParser = value;
    }

    /**
     * Sets the option DEBUG_LOOK_AHEAD.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setDebugLookAhead( Boolean value )
    {
        this.debugLookAhead = value;
    }

    /**
     * Sets the option DEBUG_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setDebugTokenManager( Boolean value )
    {
        this.debugTokenManager = value;
    }

    /**
     * Sets the option ERROR_REPORTING.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setErrorReporting( Boolean value )
    {
        this.errorReporting = value;
    }

    /**
     * Sets the option JAVA_UNICODE_ESCAPE.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setJavaUnicodeEscape( Boolean value )
    {
        this.javaUnicodeEscape = value;
    }

    /**
     * Sets the option UNICODE_INPUT.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setUnicodeInput( Boolean value )
    {
        this.unicodeInput = value;
    }

    /**
     * Sets the option IGNORE_CASE.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setIgnoreCase( Boolean value )
    {
        this.ignoreCase = value;
    }

    /**
     * Sets the option COMMON_TOKEN_ACTION.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setCommonTokenAction( Boolean value )
    {
        this.commonTokenAction = value;
    }

    /**
     * Sets the option USER_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setUserTokenManager( Boolean value )
    {
        this.userTokenManager = value;
    }

    /**
     * Sets the option USER_CHAR_STREAM.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setUserCharStream( Boolean value )
    {
        this.userCharStream = value;
    }

    /**
     * Sets the option BUILD_PARSER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setBuildParser( Boolean value )
    {
        this.buildParser = value;
    }

    /**
     * Sets the option BUILD_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setBuildTokenManager( Boolean value )
    {
        this.buildTokenManager = value;
    }

    /**
     * Sets the option TOKEN_MANAGER_USES_PARSER.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setTokenManagerUsesParser( Boolean value )
    {
        this.tokenManagerUsesParser = value;
    }

    /**
     * Sets the option TOKEN_EXTENDS.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setTokenExtends( String value )
    {
        this.tokenExtends = value;
    }

    /**
     * Sets the option TOKEN_FACTORY.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setTokenFactory( String value )
    {
        this.tokenFactory = value;
    }

    /**
     * Sets the option SANITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setSanityCheck( Boolean value )
    {
        this.sanityCheck = value;
    }

    /**
     * Sets the option FORCE_LA_CHECK.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setForceLaCheck( Boolean value )
    {
        this.forceLaCheck = value;
    }

    /**
     * Sets the option CACHE_TOKENS.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setCacheTokens( Boolean value )
    {
        this.cacheTokens = value;
    }

    /**
     * Sets the option KEEP_LINE_COLUMN.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setKeepLineColumn( Boolean value )
    {
        this.keepLineColumn = value;
    }

    /**
     * Sets the option SUPPORT_CLASS_VISIBILITY_PUBLIC.
     * 
     * @param value The option value, may be <code>null</code> to use the value provided in the grammar or the default.
     */
    public void setSupportClassVisibilityPublic( Boolean value )
    {
        this.supportClassVisibilityPublic = value;
    }

    /**
     * {@inheritDoc}
     */
    protected int execute()
        throws Exception
    {
        String[] args = generateArguments();

        if ( this.outputDirectory != null && !this.outputDirectory.exists() )
        {
            this.outputDirectory.mkdirs();
        }

        return org.javacc.parser.Main.mainProgram( args );
    }

    /**
     * <p>Assembles the command line arguments for the invocation of JavaCC according to the configuration.</p>
     * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in the grammar file,
     * only those parameters that have been explicitly set are passed on the command line.
     * 
     * @return A string array that represents the command line arguments to use for JavaCC.
     */
    private String[] generateArguments()
    {
        List argsList = new ArrayList();

        if ( StringUtils.isNotEmpty( this.grammarEncoding ) )
        {
            argsList.add( "-GRAMMAR_ENCODING=" + this.grammarEncoding );
        }

        if ( StringUtils.isNotEmpty( this.jdkVersion ) )
        {
            argsList.add( "-JDK_VERSION=" + this.jdkVersion );
        }

        if ( this.lookAhead != null )
        {
            argsList.add( "-LOOKAHEAD=" + this.lookAhead );
        }

        if ( this.choiceAmbiguityCheck != null )
        {
            argsList.add( "-CHOICE_AMBIGUITY_CHECK=" + this.choiceAmbiguityCheck );
        }

        if ( this.otherAmbiguityCheck != null )
        {
            argsList.add( "-OTHER_AMBIGUITY_CHECK=" + this.otherAmbiguityCheck );
        }

        if ( this.isStatic != null )
        {
            argsList.add( "-STATIC=" + this.isStatic );
        }

        if ( this.debugParser != null )
        {
            argsList.add( "-DEBUG_PARSER=" + this.debugParser );
        }

        if ( this.debugLookAhead != null )
        {
            argsList.add( "-DEBUG_LOOKAHEAD=" + this.debugLookAhead );
        }

        if ( this.debugTokenManager != null )
        {
            argsList.add( "-DEBUG_TOKEN_MANAGER=" + this.debugTokenManager );
        }

        if ( this.errorReporting != null )
        {
            argsList.add( "-ERROR_REPORTING=" + this.errorReporting );
        }

        if ( this.javaUnicodeEscape != null )
        {
            argsList.add( "-JAVA_UNICODE_ESCAPE=" + this.javaUnicodeEscape );
        }

        if ( this.unicodeInput != null )
        {
            argsList.add( "-UNICODE_INPUT=" + this.unicodeInput );
        }

        if ( this.ignoreCase != null )
        {
            argsList.add( "-IGNORE_CASE=" + this.ignoreCase );
        }

        if ( this.commonTokenAction != null )
        {
            argsList.add( "-COMMON_TOKEN_ACTION=" + this.commonTokenAction );
        }

        if ( this.userTokenManager != null )
        {
            argsList.add( "-USER_TOKEN_MANAGER=" + this.userTokenManager );
        }

        if ( this.userCharStream != null )
        {
            argsList.add( "-USER_CHAR_STREAM=" + this.userCharStream );
        }

        if ( this.buildParser != null )
        {
            argsList.add( "-BUILD_PARSER=" + this.buildParser );
        }

        if ( this.buildTokenManager != null )
        {
            argsList.add( "-BUILD_TOKEN_MANAGER=" + this.buildTokenManager );
        }

        if ( this.tokenManagerUsesParser != null )
        {
            argsList.add( "-TOKEN_MANAGER_USES_PARSER=" + this.tokenManagerUsesParser );
        }

        if ( StringUtils.isNotEmpty( this.tokenExtends ) )
        {
            argsList.add( "-TOKEN_EXTENDS=" + this.tokenExtends );
        }

        if ( StringUtils.isNotEmpty( this.tokenFactory ) )
        {
            argsList.add( "-TOKEN_FACTORY=" + this.tokenFactory );
        }

        if ( this.sanityCheck != null )
        {
            argsList.add( "-SANITY_CHECK=" + this.sanityCheck );
        }

        if ( this.forceLaCheck != null )
        {
            argsList.add( "-FORCE_LA_CHECK=" + this.forceLaCheck );
        }

        if ( this.cacheTokens != null )
        {
            argsList.add( "-CACHE_TOKENS=" + this.cacheTokens );
        }

        if ( this.keepLineColumn != null )
        {
            argsList.add( "-KEEP_LINE_COLUMN=" + this.keepLineColumn );
        }

        if ( this.supportClassVisibilityPublic != null )
        {
            argsList.add( "-SUPPORT_CLASS_VISIBILITY_PUBLIC=" + this.supportClassVisibilityPublic );
        }

        if ( this.outputDirectory != null )
        {
            argsList.add( "-OUTPUT_DIRECTORY=" + this.outputDirectory.getAbsolutePath() );
        }

        if ( this.inputFile != null )
        {
            argsList.add( this.inputFile.getAbsolutePath() );
        }

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * Gets a string representation of the command line arguments.
     * 
     * @return A string representation of the command line arguments.
     */
    public String toString()
    {
        return Arrays.asList( generateArguments() ).toString();
    }

}

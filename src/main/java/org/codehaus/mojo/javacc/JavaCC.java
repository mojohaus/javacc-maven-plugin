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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.javacc.parser.Main;

/**
 * Provides a facade for the mojos to invoke JavaCC.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 * @see <a href="https://javacc.dev.java.net/doc/commandline.html">JavaCC Command Line Syntax</a>
 */
public class JavaCC

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
     * Sets The absolute path to the grammar file to pass into JavaCC for compilation.
     * 
     * @param value The absolute path to the grammar file to pass into JavaCC for compilation.
     */
    public void setInputFile( File value )
    {
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
        this.outputDirectory = value;
    }

    /**
     * Sets the option JDK_VERSION.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setJdkVersion( String value )
    {
        this.jdkVersion = value;
    }

    /**
     * Sets the option STATIC.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setStatic( Boolean value )
    {
        this.isStatic = value;
    }

    /**
     * Sets the option LOOK_AHEAD.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setLookAhead( Integer value )
    {
        this.lookAhead = value;
    }

    /**
     * Sets the option CHOICE_AMBIGUITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setChoiceAmbiguityCheck( Integer value )
    {
        this.choiceAmbiguityCheck = value;
    }

    /**
     * Sets the option OTHER_AMBIGUITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setOtherAmbiguityCheck( Integer value )
    {
        this.otherAmbiguityCheck = value;
    }

    /**
     * Sets the option DEBUG_PARSER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setDebugParser( Boolean value )
    {
        this.debugParser = value;
    }

    /**
     * Sets the option DEBUG_LOOK_AHEAD.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setDebugLookAhead( Boolean value )
    {
        this.debugLookAhead = value;
    }

    /**
     * Sets the option DEBUG_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setDebugTokenManager( Boolean value )
    {
        this.debugTokenManager = value;
    }

    /**
     * Sets the option ERROR_REPORTING.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setErrorReporting( Boolean value )
    {
        this.errorReporting = value;
    }

    /**
     * Sets the option JAVA_UNICODE_ESCAPE.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setJavaUnicodeEscape( Boolean value )
    {
        this.javaUnicodeEscape = value;
    }

    /**
     * Sets the option UNICODE_INPUT.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setUnicodeInput( Boolean value )
    {
        this.unicodeInput = value;
    }

    /**
     * Sets the option IGNORE_CASE.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setIgnoreCase( Boolean value )
    {
        this.ignoreCase = value;
    }

    /**
     * Sets the option COMMON_TOKEN_ACTION.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setCommonTokenAction( Boolean value )
    {
        this.commonTokenAction = value;
    }

    /**
     * Sets the option USER_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setUserTokenManager( Boolean value )
    {
        this.userTokenManager = value;
    }

    /**
     * Sets the option USER_CHAR_STREAM.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setUserCharStream( Boolean value )
    {
        this.userCharStream = value;
    }

    /**
     * Sets the option BUILD_PARSER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setBuildParser( Boolean value )
    {
        this.buildParser = value;
    }

    /**
     * Sets the option BUILD_TOKEN_MANAGER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setBuildTokenManager( Boolean value )
    {
        this.buildTokenManager = value;
    }

    /**
     * Sets the option TOKEN_MANAGER_USES_PARSER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setTokenManagerUsesParser( Boolean value )
    {
        this.tokenManagerUsesParser = value;
    }

    /**
     * Sets the option SANITY_CHECK.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setSanityCheck( Boolean value )
    {
        this.sanityCheck = value;
    }

    /**
     * Sets the option FORCE_LA_CHECK.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setForceLaCheck( Boolean value )
    {
        this.forceLaCheck = value;
    }

    /**
     * Sets the option CACHE_TOKENS.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setCacheTokens( Boolean value )
    {
        this.cacheTokens = value;
    }

    /**
     * Sets the option KEEP_LINE_COLUMN.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setKeepLineColumn( Boolean value )
    {
        this.keepLineColumn = value;
    }

    /**
     * Runs JavaCC using the previously set parameters.
     * 
     * @param log A logger used to output diagnostic messages, may be <code>null</code>.
     * @throws MojoExecutionException If JavaCC could not be invoked.
     * @throws MojoFailureException If JavaCC reported a non-zero exit code.
     */
    public void run( Log log )
        throws MojoExecutionException, MojoFailureException
    {
        if ( this.inputFile == null )
        {
            throw new IllegalStateException( "input grammar not specified" );
        }
        if ( this.outputDirectory == null )
        {
            throw new IllegalStateException( "output directory not specified" );
        }

        int exitCode;
        try
        {
            String[] args = generateArguments();
            if ( log != null && log.isDebugEnabled() )
            {
                log.debug( "Running JavaCC: " + Arrays.asList( args ) );
            }
            if ( !this.outputDirectory.exists() )
            {
                this.outputDirectory.mkdirs();
            }
            exitCode = Main.mainProgram( args );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to execute JavaCC", e );
        }
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "JavaCC reported exit code " + exitCode + ": " + this.inputFile );
        }
    }

    /**
     * Assembles the command line arguments for the invocation of JavaCC according to the configuration.<br/><br/>
     * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in the grammar file,
     * only those parameters that have been explicitly set are passed on the command line.
     * 
     * @return A string array that represents the command line arguments to use for JavaCC.
     */
    private String[] generateArguments()
    {
        List argsList = new ArrayList();

        if ( this.jdkVersion != null )
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

        argsList.add( "-OUTPUT_DIRECTORY:" + this.outputDirectory.getAbsolutePath() );

        argsList.add( this.inputFile.getAbsolutePath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

}
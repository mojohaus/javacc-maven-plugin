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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Exposes all JavaCC options as mojo parameters such that subclasses can share this boilerplate code.
 * 
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public abstract class AbstractJavaCCMojo
    extends AbstractMojo
{

    /**
     * The Java version for which to generate source code. Default value is <code>"1.4"</code>.
     * 
     * @parameter expression="${jdkVersion}"
     * @since 2.4
     */
    private String jdkVersion;

    /**
     * The number of tokens to look ahead before making a decision at a choice point during parsing. The default value
     * is <code>1</code>.
     * 
     * @parameter expression=${lookAhead}"
     */
    private Integer lookAhead;

    /**
     * This is the number of tokens considered in checking choices of the form "A | B | ..." for ambiguity. Default
     * value is <code>2</code>.
     * 
     * @parameter expression="${choiceAmbiguityCheck}"
     */
    private Integer choiceAmbiguityCheck;

    /**
     * This is the number of tokens considered in checking all other kinds of choices (i.e., of the forms "(A)*",
     * "(A)+", and "(A)?") for ambiguity. Default value is <code>1</code>.
     * 
     * @parameter expression=${otherAmbiguityCheck}"
     */
    private Integer otherAmbiguityCheck;

    /**
     * If <code>true</code>, all methods and class variables are specified as static in the generated parser and
     * token manager. This allows only one parser object to be present, but it improves the performance of the parser.
     * Default value is <code>true</code>.
     * 
     * @parameter expression=${isStatic}"
     */
    private Boolean isStatic;

    /**
     * This option is used to obtain debugging information from the generated parser. Setting this option to
     * <code>true</code> causes the parser to generate a trace of its actions. Default value is <code>false</code>.
     * 
     * @parameter expression="${debugParser}"
     */
    private Boolean debugParser;

    /**
     * This is a boolean option whose default value is <code>false</code>. Setting this option to <code>true</code>
     * causes the parser to generate all the tracing information it does when the option <code>debugParser</code> is
     * <code>true</code>, and in addition, also causes it to generated a trace of actions performed during lookahead
     * operation.
     * 
     * @parameter expression="${debugLookAhead}"
     */
    private Boolean debugLookAhead;

    /**
     * This option is used to obtain debugging information from the generated token manager. Default value is
     * <code>false</code>.
     * 
     * @parameter expression="${debugTokenManager}"
     */
    private Boolean debugTokenManager;

    /**
     * Setting it to <code>false</code> causes errors due to parse errors to be reported in somewhat less detail.
     * Default value is <code>true</code>.
     * 
     * @parameter expression="${errorReporting}"
     */
    private Boolean errorReporting;

    /**
     * When set to <code>true</code>, the generated parser uses an input stream object that processes Java Unicode
     * escapes (<code>\</code><code>u</code><i>xxxx</i>) before sending characters to the token manager. Default
     * value is <code>false</code>.
     * 
     * @parameter expression="${javaUnicodeEscape}"
     */
    private Boolean javaUnicodeEscape;

    /**
     * When set to <code>true</code>, the generated parser uses uses an input stream object that reads Unicode files.
     * By default, ASCII files are assumed. Default value is <code>false</code>.
     * 
     * @parameter expression="${unicodeInput}"
     */
    private Boolean unicodeInput;

    /**
     * Setting this option to <code>true</code> causes the generated token manager to ignore case in the token
     * specifications and the input files. Default value is <code>false</code>.
     * 
     * @parameter expression="${ignoreCase}"
     */
    private Boolean ignoreCase;

    /**
     * When set to <code>true</code>, every call to the token manager's method <code>getNextToken()</code> (see the
     * description of the <a href="https://javacc.dev.java.net/doc/apiroutines.html">Java Compiler Compiler API</a>)
     * will cause a call to a user-defined method <code>CommonTokenAction()</code> after the token has been scanned in
     * by the token manager. Default value is <code>false</code>.
     * 
     * @parameter expression="${commonTokenAction}"
     */
    private Boolean commonTokenAction;

    /**
     * The default action is to generate a token manager that works on the specified grammar tokens. If this option is
     * set to <code>true</code>, then the parser is generated to accept tokens from any token manager of type
     * <code>TokenManager</code> - this interface is generated into the generated parser directory. Default value is
     * <code>false</code>.
     * 
     * @parameter expression="${userTokenManager}"
     */
    private Boolean userTokenManager;

    /**
     * This flag controls whether the token manager will read characters from a character stream reader as defined by
     * the options <code>javaUnicodeEscape</code> and <code>unicodeInput</code> or whether the token manager reads
     * from a user-supplied implementation of <code>CharStream</code>. Default value is <code>false</code>.
     * 
     * @parameter expression="${userCharStream}"
     */
    private Boolean userCharStream;

    /**
     * A flag that controls whether the parser file (<code>*Parser.java</code>) should be generated or not. If set
     * to <code>false</code>, only the token manager is generated. Default value is <code>true</code>.
     * 
     * @parameter expression="${buildParser}"
     */
    private Boolean buildParser;

    /**
     * A flag that controls whether the token manager file (<code>*TokenManager.java</code>) should be generated or
     * not. Setting this to <code>false</code> can speed up the generation process if only the parser part of the
     * grammar changed. Default value is <code>true</code>.
     * 
     * @parameter expression="${buildTokenManager}"
     */
    private Boolean buildTokenManager;

    /**
     * When set to <code>true</code>, the generated token manager will include a field called <code>parser</code>
     * that references the instantiating parser instance. Default value is <code>false</code>.
     * 
     * @parameter
     */
    private Boolean tokenManagerUsesParser;

    /**
     * Enables/disables many syntactic and semantic checks on the grammar file during parser generation. Default value
     * is <code>true</code>.
     * 
     * @parameter expression="${sanityCheck}"
     */
    private Boolean sanityCheck;

    /**
     * This option setting controls lookahead ambiguity checking performed by JavaCC. Default value is
     * <code>false</code>.
     * 
     * @parameter expression="${forceLaCheck}"
     */
    private Boolean forceLaCheck;

    /**
     * Setting this option to <code>true</code> causes the generated parser to lookahead for extra tokens ahead of
     * time. Default value is <code>false</code>.
     * 
     * @parameter expression="${cacheTokens}"
     */
    private Boolean cacheTokens;

    /**
     * A flag whether to keep line and column information along with a token. Default value is <code>true</code>.
     * 
     * @parameter expression="${keepLineColumn}"
     */
    private Boolean keepLineColumn;

    /**
     * Gets the Java version for which to generate source code.
     * 
     * @return The Java version for which to generate source code, will be <code>null</code> if the user did not
     *         specify this mojo parameter.
     */
    protected String getJdkVersion()
    {
        return this.jdkVersion;
    }

    /**
     * Gets the flag whether to generate static parser.
     * 
     * @return The flag whether to generate static parser, will be <code>null</code> if the user did not specify this
     *         mojo parameter.
     */
    protected Boolean getIsStatic()
    {
        return this.isStatic;
    }

    /**
     * Gets the grammar information for the specified file.
     * 
     * @param grammarFile The absolute path to the grammar file, must not be <code>null</code>.
     * @return The grammar information for the specified file.
     * @throws MojoExecutionException If the grammar file could not be read.
     */
    private GrammarInfo getGrammarInfo( File grammarFile )
        throws MojoExecutionException
    {
        try
        {
            return new GrammarInfo( grammarFile );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to extract metadata from grammar: " + grammarFile, e );
        }
    }

    /**
     * Scans the specified source directory recursively for grammar files.
     * 
     * @param sourceDirectory The absolute path to the source directory to scan, must not be <code>null</code>.
     * @param outputDirectory The absolute path to the output directory used to detect stale target files by timestamp
     *            checking, may be <code>null</code> if no stale detection should be performed.
     * @param includes The set of Ant-like inclusion patterns, may be <code>null</code> to include all grammar files
     *            (*.jj, *.jjt).
     * @param excludes The set of Ant-like exclusion patterns, may be <code>null</code> to exclude no files.
     * @param staleMillis The granularity in milliseconds of the last modification date for testing whether a grammar
     *            file needs recompilation because its corresponding target file is stale.
     * @return An array of grammar infos describing the found grammar files, never <code>null</code>.
     * @throws MojoExecutionException If the source directory could not be scanned.
     */
    protected GrammarInfo[] scanForGrammarFiles( File sourceDirectory, File outputDirectory, String[] includes,
                                                 String[] excludes, int staleMillis )
        throws MojoExecutionException
    {
        getLog().debug( "Scanning for grammars: " + sourceDirectory );

        if ( !sourceDirectory.isDirectory() )
        {
            getLog().debug( "Ignored invalid source directory: " + sourceDirectory );
            return new GrammarInfo[0];
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( sourceDirectory );
        if ( includes != null )
        {
            scanner.setIncludes( includes );
        }
        else
        {
            scanner.setIncludes( new String[] { "**/*.jj", "**/*.JJ", "**/*.jjt", "**/*.JJT" } );
        }
        if ( excludes != null )
        {
            scanner.setExcludes( excludes );
        }
        scanner.addDefaultExcludes();
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        Set grammarInfos = new LinkedHashSet();
        for ( int i = 0; i < files.length; i++ )
        {
            File sourceFile = new File( sourceDirectory, files[i] );
            GrammarInfo grammarInfo = getGrammarInfo( sourceFile );
            if ( outputDirectory != null )
            {
                File[] targetFiles = getTargetFiles( outputDirectory, grammarInfo );
                for ( int j = 0; j < targetFiles.length; j++ )
                {
                    File targetFile = targetFiles[j];
                    if ( !targetFile.exists() || targetFile.lastModified() + staleMillis < sourceFile.lastModified() )
                    {
                        grammarInfos.add( grammarInfo );
                        break;
                    }
                }
            }
            else
            {
                grammarInfos.add( grammarInfo );
            }
        }

        getLog().debug( "Found grammars: " + grammarInfos );

        return (GrammarInfo[]) grammarInfos.toArray( new GrammarInfo[grammarInfos.size()] );
    }

    /**
     * Determines the output files corresponding to the specified grammar file.
     * 
     * @param outputDirectory The absolute path to the output directory for the target files, must not be
     *            <code>null</code>.
     * @param grammarInfo The grammar info describing the grammar file, must not be <code>null</code>
     * @return A file array with target files, never <code>null</code>.
     */
    protected File[] getTargetFiles( File outputDirectory, GrammarInfo grammarInfo )
    {
        File parserFile = new File( outputDirectory, grammarInfo.getParserFile().getPath() );
        return new File[] { parserFile };
    }

    /**
     * Assembles the command line arguments for the invocation of JavaCC according to the mojo configuration.<br/><br/>
     * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in the grammar file,
     * only those mojo parameters that have been explicitly set by the user are passed on the command line.
     * 
     * @param grammarFile The absolute path of the grammar file to compile, must not be <code>null</code>.
     * @param outputDirectory The absolute path to the output directory for the generated Java files, must not be
     *            <code>null</code>. This path should already contain the package hierarchy.
     * @return A string array that represents the command line arguments to use for JavaCC.
     */
    protected String[] generateArgumentsForJavaCC( File grammarFile, File outputDirectory )
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

        argsList.add( "-OUTPUT_DIRECTORY:" + outputDirectory.getAbsolutePath() );

        argsList.add( grammarFile.getAbsolutePath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

}

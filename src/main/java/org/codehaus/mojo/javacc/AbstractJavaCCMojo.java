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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Provides common services for all mojos that compile JavaCC grammar files.
 * 
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public abstract class AbstractJavaCCMojo
    extends AbstractMojo
{

    /**
     * The current Maven project.
     * 
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * The Java version for which to generate source code. Default value is <code>1.4</code>.
     * 
     * @parameter expression="${jdkVersion}"
     * @since 2.4
     */
    private String jdkVersion;

    /**
     * The number of tokens to look ahead before making a decision at a choice point during parsing. The default value
     * is <code>1</code>.
     * 
     * @parameter expression="${lookAhead}"
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
     * @parameter expression="${otherAmbiguityCheck}"
     */
    private Integer otherAmbiguityCheck;

    /**
     * If <code>true</code>, all methods and class variables are specified as static in the generated parser and
     * token manager. This allows only one parser object to be present, but it improves the performance of the parser.
     * Default value is <code>true</code>.
     * 
     * @parameter expression="${isStatic}"
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
     * @parameter expression="${tokenManagerUsesParser}"
     */
    private Boolean tokenManagerUsesParser;

    /**
     * The name of the base class for the generated <code>Token</code> class. Default value is
     * <code>java.lang.Object</code>.
     * 
     * @parameter expression="${tokenExtends}"
     * @since 2.5
     */
    private String tokenExtends;

    /**
     * The name of a user-defined token factory class that provides a
     * <code>public static Token newToken(int ofKind, String image)</code> method. By default, tokens are created by
     * calling <code>Token.newToken()</code>.
     * 
     * @parameter expression="${tokenFactory}"
     * @since 2.5
     */
    private String tokenFactory;

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
     * Gets the absolute path to the directory where the grammar files are located.
     * 
     * @return The absolute path to the directory where the grammar files are located, never <code>null</code>.
     */
    protected abstract File getSourceDirectory();

    /**
     * Gets a set of Ant-like inclusion patterns used to select files from the source directory for processing.
     * 
     * @return A set of Ant-like inclusion patterns used to select files from the source directory for processing, can
     *         be <code>null</code> if all files should be included.
     */
    protected abstract String[] getIncludes();

    /**
     * Gets a set of Ant-like exclusion patterns used to unselect files from the source directory for processing.
     * 
     * @return A set of Ant-like inclusion patterns used to unselect files from the source directory for processing, can
     *         be <code>null</code> if no files should be excluded.
     */
    protected abstract String[] getExcludes();

    /**
     * Gets the absolute path to the directory where the generated Java files for the parser will be stored.
     * 
     * @return The absolute path to the directory where the generated Java files for the parser will be stored, never
     *         <code>null</code>.
     */
    protected abstract File getOutputDirectory();

    /**
     * Gets the granularity in milliseconds of the last modification date for testing whether a source needs
     * recompilation.
     * 
     * @return The granularity in milliseconds of the last modification date for testing whether a source needs
     *         recompilation.
     */
    protected abstract int getStaleMillis();

    /**
     * Gets the package into which the generated parser files should be stored.
     * 
     * @return The package into which the generated parser files should be stored, can be <code>null</code> to use the
     *         package declaration from the grammar file.
     */
    // TODO: Once the parameter "packageName" from the javacc mojo has been deleted, remove this method, too.
    protected String getParserPackage()
    {
        return null;
    }

    /**
     * Execute the tool.
     * 
     * @throws MojoExecutionException If the invocation of the tool failed.
     * @throws MojoFailureException If the tool reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        GrammarInfo[] grammarInfos = scanForGrammars();

        if ( grammarInfos == null )
        {
            getLog().info( "Skipping non-existing source directory: " + getSourceDirectory() );
            return;
        }
        else if ( grammarInfos.length <= 0 )
        {
            getLog().info( "Skipping - all parsers are up to date" );
        }
        else
        {
            File tempDirectory =
                new File( this.project.getBuild().getDirectory(), "javacc-" + System.currentTimeMillis() );
            tempDirectory.mkdirs();

            for ( int i = 0; i < grammarInfos.length; i++ )
            {
                processGrammar( grammarInfos[i], tempDirectory );
            }
            
            try
            {
                copyNonCustomizedSourceFiles( tempDirectory );
                FileUtils.deleteDirectory( tempDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to copy generated source files to output directory:"
                    + tempDirectory + " -> " + getOutputDirectory(), e );
            }

            getLog().info( "Processed " + grammarInfos.length + " grammar" + ( grammarInfos.length != 1 ? "s" : "" ) );
        }

        addCompileSourceRoot( getOutputDirectory() );
    }

    /**
     * Passes the specified grammar file through the tool.
     * 
     * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>null</code>.
     * @param targetDirectory The absolute path to the output directory for the generated source files, must not be
     *            <code>null</code>.
     * @throws MojoExecutionException If the invocation of the tool failed.
     * @throws MojoFailureException If the tool reported a non-zero exit code.
     */
    protected abstract void processGrammar( GrammarInfo grammarInfo, File targetDirectory )
        throws MojoExecutionException, MojoFailureException;

    /**
     * Scans the configured source directory for grammar files which need processing.
     * 
     * @return An array of grammar infos describing the found grammar files or <code>null</code> if the source
     *         directory does not exist.
     * @throws MojoExecutionException If the source directory could not be scanned.
     */
    private GrammarInfo[] scanForGrammars()
        throws MojoExecutionException
    {
        if ( !getSourceDirectory().isDirectory() )
        {
            return null;
        }

        GrammarInfo[] grammarInfos;

        getLog().debug( "Scanning for grammars: " + getSourceDirectory() );
        try
        {
            GrammarDirectoryScanner scanner = new GrammarDirectoryScanner();
            scanner.setSourceDirectory( getSourceDirectory() );
            scanner.setIncludes( getIncludes() );
            scanner.setExcludes( getExcludes() );
            scanner.setOutputDirectory( getOutputDirectory() );
            scanner.setParserPackage( getParserPackage() );
            scanner.setStaleMillis( getStaleMillis() );
            scanner.scan();
            grammarInfos = scanner.getIncludedGrammars();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to scan for grammars: " + getSourceDirectory(), e );
        }
        getLog().debug( "Found grammars: " + Arrays.asList( grammarInfos ) );

        return grammarInfos;
    }

    /**
     * Copies the Java files from the specified temporary source root to the configured output directory, excluding all
     * those source files that are already present in one of the compile source roots of the current project. This
     * prevents duplicate source errors in case the user created customized source files for some generator outputs in
     * <code>src/main/java</code>.
     * 
     * @param tempDirectory The absolute path to the temporary source root, must not be <code>null</code>.
     * @throws IOException If any file could not be copied.
     */
    private void copyNonCustomizedSourceFiles( File tempDirectory )
        throws IOException
    {
        if ( !tempDirectory.exists() )
        {
            return;
        }

        getLog().debug( "Copying generated source files: " + tempDirectory + " -> " + getOutputDirectory() );

        Collection filenames = FileUtils.getFileNames( tempDirectory, "**/*.java", null, false );
        for ( Iterator it = filenames.iterator(); it.hasNext(); )
        {
            String filename = (String) it.next();
            if ( !isSourceFile( filename ) )
            {
                FileUtils.copyFile( new File( tempDirectory, filename ), new File( getOutputDirectory(), filename ) );
            }
            else
            {
                getLog().debug( "Detected customized generator output: " + filename );
            }
        }
    }

    /**
     * Determines whether the specified source file is already present in any of the compile source roots registered
     * with the current Maven project.
     * 
     * @param filename The source filename to check, relative to a source root, must not be <code>null</code>.
     * @return <code>true</code> if any compile source root of the current project already contains a source file with
     *         the specified name, <code>false</code> otherwise.
     */
    private boolean isSourceFile( String filename )
    {
        Collection sourceRoots = this.project.getCompileSourceRoots();
        for ( Iterator it = sourceRoots.iterator(); it.hasNext(); )
        {
            File sourceRoot = new File( (String) it.next() );
            if ( !sourceRoot.isAbsolute() )
            {
                sourceRoot = new File( this.project.getBasedir(), sourceRoot.getPath() );
            }
            File sourceFile = new File( sourceRoot, filename );
            if ( sourceFile.exists() )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers the specified directory as a compile source root for the current project.
     * 
     * @param directory The absolute path to the compile source, must not be <code>null</code>.
     */
    private void addCompileSourceRoot( File directory )
    {
        if ( this.project != null )
        {
            getLog().debug( "Adding compile source root: " + directory );
            this.project.addCompileSourceRoot( directory.getAbsolutePath() );
        }
    }

    /**
     * Creates a new facade to invoke JavaCC. Most options for the invocation are derived from the current values of the
     * corresponding mojo parameters. The caller is responsible to set the input file and output directory on the
     * returned facade.
     * 
     * @return The facade for the tool invocation, never <code>null</code>.
     */
    protected JavaCC newJavaCC()
    {
        JavaCC javacc = new JavaCC();
        javacc.setLog( getLog() );
        javacc.setJdkVersion( this.jdkVersion );
        javacc.setStatic( this.isStatic );
        javacc.setBuildParser( this.buildParser );
        javacc.setBuildTokenManager( this.buildTokenManager );
        javacc.setCacheTokens( this.cacheTokens );
        javacc.setChoiceAmbiguityCheck( this.choiceAmbiguityCheck );
        javacc.setCommonTokenAction( this.commonTokenAction );
        javacc.setDebugLookAhead( this.debugLookAhead );
        javacc.setDebugParser( this.debugParser );
        javacc.setDebugTokenManager( this.debugTokenManager );
        javacc.setErrorReporting( this.errorReporting );
        javacc.setForceLaCheck( this.forceLaCheck );
        javacc.setIgnoreCase( this.ignoreCase );
        javacc.setJavaUnicodeEscape( this.javaUnicodeEscape );
        javacc.setKeepLineColumn( this.keepLineColumn );
        javacc.setLookAhead( this.lookAhead );
        javacc.setOtherAmbiguityCheck( this.otherAmbiguityCheck );
        javacc.setSanityCheck( this.sanityCheck );
        javacc.setTokenManagerUsesParser( this.tokenManagerUsesParser );
        javacc.setTokenExtends( this.tokenExtends );
        javacc.setTokenFactory( this.tokenFactory );
        javacc.setUnicodeInput( this.unicodeInput );
        javacc.setUserCharStream( this.userCharStream );
        javacc.setUserTokenManager( this.userTokenManager );
        return javacc;
    }

}

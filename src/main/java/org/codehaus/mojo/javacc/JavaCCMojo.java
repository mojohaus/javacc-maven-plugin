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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.javacc.parser.Main;

/**
 * Parses a JavaCC grammar file (<code>*.jj</code>) and transforms it to Java source files. Detailed information
 * about the JavaCC options can be found on the <a href="https://javacc.dev.java.net/">JavaCC website</a>.
 * 
 * @goal javacc
 * @phase generate-sources
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JavaCCMojo
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
     * Package into which the generated classes will be put. Note that this will also be used to create the directory
     * structure where sources will be generated. Defaults to the package name specified in a grammar file.
     * 
     * @parameter expression="${packageName}"
     */
    private String packageName;

    /**
     * The directory where the JavaCC grammar files (<code>*.jj</code>) are located.
     * 
     * @parameter expression="${basedir}/src/main/javacc"
     * @required
     */
    private File sourceDirectory;

    /**
     * The directory where the output Java files will be located.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/javacc"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/javacc-timestamp"
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * A set of Ant-like inclusion patterns for the compiler.
     * 
     * @parameter
     */
    private Set includes;

    /**
     * A set of Ant-like exclusion patterns for the compiler.
     * 
     * @parameter
     */
    private Set excludes;

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Execute the JavaCC compiler.
     * 
     * @throws MojoExecutionException If it fails.
     */
    public void execute()
        throws MojoExecutionException
    {
        // check packageName for . vs /
        if ( packageName != null )
        {
            packageName = StringUtils.replace( packageName, '.', File.separatorChar );
        }

        if ( !sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + sourceDirectory );
            return;
        }

        if ( !timestampDirectory.exists() )
        {
            timestampDirectory.mkdirs();
        }

        if ( includes == null )
        {
            includes = Collections.singleton( "**/*" );
        }

        if ( excludes == null )
        {
            excludes = Collections.EMPTY_SET;
        }

        Set staleGrammars = computeStaleGrammars( sourceDirectory, timestampDirectory );

        if ( staleGrammars.isEmpty() )
        {
            getLog().info( "Nothing to process - all grammars in " + sourceDirectory + " are up to date." );
        }
        else
        {
            for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
            {
                File jjFile = (File) i.next();

                File outputDir = getOutputDirectory( jjFile );
                if ( !outputDir.exists() )
                {
                    outputDir.mkdirs();
                }

                // Copy all .java files from sourceDirectory to outputDirectory, in
                // order to prevent regeneration of customized Token.java or similar
                try
                {
                    FileUtils.copyDirectory( jjFile.getParentFile(), outputDir, "*.java", "*.jj,*.JJ" );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Unable to copy overriden java files.", e );
                }

                try
                {
                    Main.mainProgram( generateArgumentList( jjFile, outputDir ) );

                    URI relativeURI = sourceDirectory.toURI().relativize( jjFile.toURI() );
                    File timestampFile = new File( timestampDirectory.toURI().resolve( relativeURI ) );
                    FileUtils.copyFile( jjFile, timestampFile );
                }
                catch ( Exception e )
                {
                    throw new MojoExecutionException( "JavaCC execution failed", e );
                }
            }
        }

        if ( project != null )
        {
            project.addCompileSourceRoot( outputDirectory.getPath() );
        }
    }

    /**
     * Get the output directory for the Java files.
     * 
     * @param jjFile The JavaCC input file.
     * @return The directory that will contain the generated code.
     * @throws MojoExecutionException If there is a problem getting the package name.
     */
    private File getOutputDirectory( File jjFile )
        throws MojoExecutionException
    {
        try
        {
            GrammarInfo info = new GrammarInfo( jjFile, this.packageName );
            return new File( this.outputDirectory, info.getPackageDirectory().getPath() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to retrieve package name from grammar file", e );
        }
    }

    /**
     * @param javaccInput The path of the file to compile.
     * @param outputDir The output directory for the generated Java files. This path should already contain the package
     *            hierarchy.
     * @return A string array that represents the arguments to use for JavaCC.
     */
    private String[] generateArgumentList( File javaccInput, File outputDir )
    {
        List argsList = new ArrayList();

        if ( jdkVersion != null )
        {
            argsList.add( "-JDK_VERSION=" + jdkVersion );
        }

        if ( lookAhead != null )
        {
            argsList.add( "-LOOKAHEAD=" + lookAhead );
        }

        if ( choiceAmbiguityCheck != null )
        {
            argsList.add( "-CHOICE_AMBIGUITY_CHECK=" + choiceAmbiguityCheck );
        }

        if ( otherAmbiguityCheck != null )
        {
            argsList.add( "-OTHER_AMBIGUITY_CHECK=" + otherAmbiguityCheck );
        }

        if ( isStatic != null )
        {
            argsList.add( "-STATIC=" + isStatic );
        }

        if ( debugParser != null )
        {
            argsList.add( "-DEBUG_PARSER=" + debugParser );
        }

        if ( debugLookAhead != null )
        {
            argsList.add( "-DEBUG_LOOKAHEAD=" + debugLookAhead );
        }

        if ( debugTokenManager != null )
        {
            argsList.add( "-DEBUG_TOKEN_MANAGER=" + debugTokenManager );
        }

        if ( errorReporting != null )
        {
            argsList.add( "-ERROR_REPORTING=" + errorReporting );
        }

        if ( javaUnicodeEscape != null )
        {
            argsList.add( "-JAVA_UNICODE_ESCAPE=" + javaUnicodeEscape );
        }

        if ( unicodeInput != null )
        {
            argsList.add( "-UNICODE_INPUT=" + unicodeInput );
        }

        if ( ignoreCase != null )
        {
            argsList.add( "-IGNORE_CASE=" + ignoreCase );
        }

        if ( commonTokenAction != null )
        {
            argsList.add( "-COMMON_TOKEN_ACTION=" + commonTokenAction );
        }

        if ( userTokenManager != null )
        {
            argsList.add( "-USER_TOKEN_MANAGER=" + userTokenManager );
        }

        if ( userCharStream != null )
        {
            argsList.add( "-USER_CHAR_STREAM=" + userCharStream );
        }

        if ( buildParser != null )
        {
            argsList.add( "-BUILD_PARSER=" + buildParser );
        }

        if ( buildTokenManager != null )
        {
            argsList.add( "-BUILD_TOKEN_MANAGER=" + buildTokenManager );
        }

        if ( tokenManagerUsesParser != null )
        {
            argsList.add( "-TOKEN_MANAGER_USES_PARSER=" + tokenManagerUsesParser );
        }

        if ( sanityCheck != null )
        {
            argsList.add( "-SANITY_CHECK=" + sanityCheck );
        }

        if ( forceLaCheck != null )
        {
            argsList.add( "-FORCE_LA_CHECK=" + forceLaCheck );
        }

        if ( cacheTokens != null )
        {
            argsList.add( "-CACHE_TOKENS=" + cacheTokens );
        }

        if ( keepLineColumn != null )
        {
            argsList.add( "-KEEP_LINE_COLUMN=" + keepLineColumn );
        }

        argsList.add( "-OUTPUT_DIRECTORY:" + outputDir );

        argsList.add( javaccInput.getPath() );

        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * @param sourceDir The source directory to scan for grammar files.
     * @param timestampDir The output directory for timestamp files.
     * @return A set of <code>File</code> objects to compile.
     * @throws MojoExecutionException If it fails.
     */
    private Set computeStaleGrammars( File sourceDir, File timestampDir )
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".jj", ".jj" );
        SuffixMapping mappingCAP = new SuffixMapping( ".JJ", ".JJ" );

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis, includes, excludes );

        scanner.addSourceMapping( mapping );
        scanner.addSourceMapping( mappingCAP );

        Set staleSources = new HashSet();

        try
        {
            staleSources.addAll( scanner.getIncludedSources( sourceDir, timestampDir ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir
                + "\' for stale grammars to reprocess.", e );
        }

        return staleSources;
    }

}

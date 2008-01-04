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

/**
 * Parses a javacc (jj) grammar file and transforms it to Java source files. Detailed information about the javacc
 * options can be found on the <a href="https://javacc.dev.java.net/">javacc</a> site.
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
     * The number of tokens to look ahead before making a decision at a choice point during parsing. The default value
     * is 1.
     * 
     * @parameter expression=${lookAhead}"
     */
    private Integer lookAhead;

    /**
     * This is the number of tokens considered in checking choices
     * of the form "A | B | ..." for ambiguity.  Default value is 2.
     * 
     * @parameter expression="${choiceAmbiguityCheck}"
     */
    private Integer choiceAmbiguityCheck;

    /**
     * This is the number of tokens considered in checking all other kinds of choices (i.e., of the forms "(A)*",
     * "(A)+", and "(A)?") for ambiguity. Default value is 1.
     * 
     * @parameter expression=${otherAmbiguityCheck}"
     */
    private Integer otherAmbiguityCheck;

    /**
     * If true, all methods and class variables are specified as static in the generated parser and token manager. This
     * allows only one parser object to be present, but it improves the performance of the parser. Default value is
     * true.
     * 
     * @parameter expression=${isStatic}"
     */
    private Boolean isStatic;

    /**
     * This option is used to obtain debugging information from the generated parser. Setting this option to true causes
     * the parser to generate a trace of its actions. Default value is false.
     * 
     * @parameter expression="${debugParser}"
     */
    private Boolean debugParser;

    /**
     * This is a boolean option whose default value is false. Setting this option to true causes the parser to generate
     * all the tracing information it does when the option DEBUG_PARSER is true, and in addition, also causes it to
     * generated a trace of actions performed during lookahead operation.
     * 
     * @parameter expression="${debugLookAhead}"
     */
    private Boolean debugLookAhead;

    /**
     * This option is used to obtain debugging information from the generated token manager. Default value is false.
     * 
     * @parameter expression="${debugTokenManager}"
     */
    private Boolean debugTokenManager;

    /**
     * Setting it to false causes errors due to parse errors to be reported in somewhat less detail. Default value is
     * true.
     * 
     * @parameter expression="${errorReporting}"
     */
    private Boolean errorReporting;

    /**
     * When set to true, the generated parser uses an input stream object that processes Java Unicode escapes (\ u...)
     * before sending characters to the token manager. Default value is false.
     * 
     * @parameter expression="${javaUnicodeEscape}"
     */
    private Boolean javaUnicodeEscape;

    /**
     * When set to true, the generated parser uses uses an input stream object that reads Unicode files. By default,
     * ASCII files are assumed. Default value is false.
     * 
     * @parameter expression="${unicodeInput}"
     */
    private Boolean unicodeInput;

    /**
     * Setting this option to true causes the generated token manager to ignore case in the token specifications and the
     * input files. Default value is false.
     * 
     * @parameter expression="${ignoreCase}"
     */
    private Boolean ignoreCase;

    /**
     * When set to true, every call to the token manager's method "getNextToken" (see the description of the Java
     * Compiler Compiler API) will cause a call to a used defined method "CommonTokenAction" after the token has been
     * scanned in by the token manager. Default value is false.
     * 
     * @parameter expression="${commonTokenAction}"
     */
    private Boolean commonTokenAction;

    /**
     * The default action is to generate a token manager that works on the specified grammar tokens. If this option is
     * set to true, then the parser is generated to accept tokens from any token manager of type "TokenManager" - this
     * interface is generated into the generated parser directory. Default value is false.
     * 
     * @parameter expression="${userTokenManager}"
     */
    private Boolean userTokenManager;

    /**
     * The default action is to generate a character stream reader as specified by the options JAVA_UNICODE_ESCAPE and
     * UNICODE_INPUT. Default value is false.
     * 
     * @parameter expression="${userCharStream}"
     */
    private Boolean userCharStream;

    /**
     * The default action is to generate the parser file. Default value is true.
     * 
     * @parameter expression="${buildParser}"
     */
    private Boolean buildParser;

    /**
     * The default action is to generate the token manager. Default value is true.
     * 
     * @parameter expression="${buildTokenManager}"
     */
    private Boolean buildTokenManager;

    /**
     * When set to true, the generated token manager will include a field called parser that references the
     * instantiating parser instance. Default value is false.
     * 
     * @parameter
     */
    private Boolean tokenManagerUsesParser;

    /**
     * This is a string option whose default value is "", meaning that the generated Token class will extend
     * java.lang.Object. This option may be set to the name of a class that will be used as the base class for the
     * generated Token class.
     * 
     * @parameter
     */
    private String tokenExtends;

    /**
     * This is a string option whose default value is "", meaning that Tokens will be created by calling
     * Token.newToken(). If set the option names a Token factory class containing a public static Token newToken(int
     * ofKind, String image) method.
     */
    private String tokenFactory;

    /**
     * JavaCC performs many syntactic and semantic checks on the grammar file during parser generation. Default value is
     * true.
     * 
     * @parameter expression="${sanityCheck}"
     */
    private Boolean sanityCheck;

    /**
     * This option setting controls lookahead ambiguity checking performed by JavaCC. Default value is false.
     * 
     * @parameter expression="${forceLaCheck}"
     */
    private Boolean forceLaCheck;

    /**
     * Setting this option to true causes the generated parser to
     * lookahead for extra tokens ahead of time.  Default value is false.
     * 
     * @parameter expression="${cacheTokens}"
     */
    private Boolean cacheTokens;

    /**
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
     * Directory where the JJ file(s) are located.
     * 
     * @parameter expression="${basedir}/src/main/javacc"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where the output Java Files will be located.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/javacc"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory to store the processed .jj files
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
     * A list of inclusion filters for the compiler.
     * 
     * @parameter
     */
    private Set includes;

    /**
     * A list of exclusion filters for the compiler.
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
     * Execute the JavaCC compiler
     * 
     * @throws MojoExecutionException if it fails
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
            getLog().warn( "Source directory '" + sourceDirectory + "' does not exist. Skipping..." );
            return;
        }

        File outputDirPackages = outputDirectory;
        if ( packageName != null )
        {
            outputDirPackages = new File( outputDirectory, packageName );
        }
        if ( !outputDirPackages.exists() )
        {
            outputDirPackages.mkdirs();
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
            // Copy all .java file from sourceDirectory to outputDirectory, in
            // order to override Token.java
            try
            {
                FileUtils.copyDirectory( sourceDirectory, outputDirPackages, "*.java", "*.jj,*.JJ" );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to copy overriden java files.", e );
            }

            for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
            {
                File javaccFile = (File) i.next();
                try
                {
                    org.javacc.parser.Main.mainProgram( generateJavaCCArgumentList( javaccFile, outputDirectory ) );

                    URI relativeURI = sourceDirectory.toURI().relativize( javaccFile.toURI() );
                    File timestampFile = new File( timestampDirectory.toURI().resolve( relativeURI ) );
                    FileUtils.copyFile( javaccFile, timestampFile );
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
     * @param javaccInput a <code>String</code> which rappresent the path of the file to compile
     * @param outputDir The output directory for the generated Java files. If a package name is provided by the user or
     *            the grammar file, it is appended to this directory.
     * @return a <code>String[]</code> that represent the argument to use for JavaCC
     * @throws MojoExecutionException If there is a problem generating the command line arguments.
     */
    private String[] generateJavaCCArgumentList( File javaccInput, File outputDir )
        throws MojoExecutionException
    {

        ArrayList argsList = new ArrayList();

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

        if ( tokenExtends != null )
        {
            argsList.add( "-TOKEN_EXTENDS=" + tokenExtends );
        }

        if ( tokenFactory != null )
        {
            argsList.add( "-TOKEN_FACTORY=" + tokenFactory );
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

        String outputPackage = packageName;
        if ( outputPackage == null )
        {
            outputPackage = JavaCCUtil.getDeclaredPackage( javaccInput );
        }
        File outputDirPackages = outputDir;
        if ( outputPackage != null )
        {
            outputDirPackages = new File( outputDir, outputPackage );
        }
        argsList.add( "-OUTPUT_DIRECTORY:" + outputDirPackages );

        argsList.add( javaccInput.getPath() );

        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * @param sourceDir The source directory to scan for grammar files.
     * @param timestampDir The output directory for timestamp files.
     * @return the <code>Set</code> contains a <code>String</code>tha rappresent the files to compile
     * @throws MojoExecutionException if it fails
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

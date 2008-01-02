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
 * @goal javacc
 * @phase generate-sources
 * @description Goal which parse a JJ file and transform it to Java Source
 *              Files.
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JavaCCMojo extends AbstractMojo
{
    /**
     * @parameter expression=${lookAhead}"
     */
    private Integer lookAhead;

    /**
     * @parameter expression="${choiceAmbiguityCheck}"
     */
    private Integer choiceAmbiguityCheck;

    /**
     * @parameter expression=${otherAmbiguityCheck}"
     */
    private Integer otherAmbiguityCheck;

    /**
     * @parameter expression=${isStatic}"
     */
    private Boolean isStatic;

    /**
     * @parameter expression="${debugParser}"
     */
    private Boolean debugParser;

    /**
     * @parameter expression="${debugLookAhead}"
     */
    private Boolean debugLookAhead;

    /**
     * @parameter expression="${debugTokenManager}"
     */
    private Boolean debugTokenManager;

    /**
     * @parameter expression="${optimizeTokenManager}"
     */
    private Boolean optimizeTokenManager;

    /**
     * @parameter expression="${errorReporting}"
     */
    private Boolean errorReporting;

    /**
     * @parameter expression="${javaUnicodeEscape}"
     */
    private Boolean javaUnicodeEscape;

    /**
     * @parameter expression="${unicodeInput}"
     */
    private Boolean unicodeInput;

    /**
     * @parameter expression="${ignoreCase}"
     */
    private Boolean ignoreCase;

    /**
     * @parameter expression="${commonTokenAction}"
     */
    private Boolean commonTokenAction;

    /**
     * @parameter expression="${userTokenManager}"
     */
    private Boolean userTokenManager;

    /**
     * @parameter expression="${userCharStream}"
     */
    private Boolean userCharStream;

    /**
     * @parameter expression="${buildParser}"
     */
    private Boolean buildParser;

    /**
     * @parameter expression="${buildTokenManager}"
     */
    private Boolean buildTokenManager;

    /**
     * @parameter expression="${sanityCheck}"
     */
    private Boolean sanityCheck;

    /**
     * @parameter expression="${forceLaCheck}"
     */
    private Boolean forceLaCheck;

    /**
     * @parameter expression="${cacheTokens}"
     */
    private Boolean cacheTokens;

    /**
     * @parameter expression="${keepLineColumn}"
     */
    private Boolean keepLineColumn;

    /**
     * Package into which the generated classes will be put. Note that this will
     * also be used to create the directory structure where sources will be
     * generated. Defaults to the package name specified in a grammar file.
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
     * The granularity in milliseconds of the last modification date for testing
     * whether a source needs recompilation.
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * A list of inclusion filters for the compiler.
     * @parameter
     */
    private Set includes;
    
    /**
     * A list of exclusion filters for the compiler.
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
     * @throws MojoExecutionException if it fails
     */
    public void execute() throws MojoExecutionException
    {
        // check packageName for . vs /
        if ( packageName != null )
        {
            packageName = StringUtils.replace( packageName, '.', File.separatorChar );
        }

        if (!sourceDirectory.isDirectory()) 
        {
            getLog().warn("Source directory '" + sourceDirectory + "' does not exist. Skipping...");
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
            getLog().info("Nothing to process - all grammars in " + sourceDirectory + " are up to date.");
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

                    File timestampFile = new File( timestampDirectory.toURI().resolve( sourceDirectory.toURI().relativize( javaccFile.toURI() ) ) );
                    FileUtils.copyFile(javaccFile, timestampFile);
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
     * @param outputDir The output directory for the generated Java files. If a package name is
     *     provided by the user or the grammar file, it is appended to this directory.
     * @return a <code>String[]</code> that represent the argument to use for JavaCC
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

        if ( optimizeTokenManager != null )
        {
            argsList.add( "-OPTIMIZE_TOKEN_MANAGER=" + optimizeTokenManager );
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
        argsList.add("-OUTPUT_DIRECTORY:" + outputDirPackages);

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
    private Set computeStaleGrammars( File sourceDir, File timestampDir ) throws MojoExecutionException
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

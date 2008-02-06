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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

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
    extends AbstractJavaCCMojo
{

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
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/javacc"
     */
    private File sourceDirectory;

    /**
     * The directory where the output Java files will be located.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/javacc"
     */
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     * 
     * @parameter expression="${timestampDirectory}"
     *            default-value="${project.build.directory}/generated-sources/javacc-timestamp"
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
     * @throws MojoExecutionException If the invocation of JavaCC failed.
     * @throws MojoFailureException If JavaCC reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // check packageName for . vs /
        if ( this.packageName != null )
        {
            this.packageName = StringUtils.replace( this.packageName, '.', File.separatorChar );
        }

        if ( this.includes == null )
        {
            this.includes = Collections.singleton( "**/*" );
        }

        if ( this.excludes == null )
        {
            this.excludes = Collections.EMPTY_SET;
        }

        GrammarInfo[] grammarInfos = scanForGrammars();

        if ( grammarInfos == null )
        {
            getLog().info( "Skipping non-existing source directory: " + this.sourceDirectory );
            return;
        }
        else if ( grammarInfos.length <= 0 )
        {
            getLog().info( "Skipping - all grammars up to date" );
        }
        else
        {
            if ( !this.timestampDirectory.exists() )
            {
                this.timestampDirectory.mkdirs();
            }

            for ( int i = 0; i < grammarInfos.length; i++ )
            {
                processGrammar( grammarInfos[i] );
            }
            getLog().info( "Processed " + grammarInfos.length + " grammars" );
        }

        if ( this.project != null )
        {
            getLog().debug( "Adding compile source root: " + this.outputDirectory );
            this.project.addCompileSourceRoot( this.outputDirectory.getAbsolutePath() );
        }
    }

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
        if ( !this.sourceDirectory.isDirectory() )
        {
            return null;
        }

        Collection grammarInfos = new ArrayList();

        getLog().debug( "Scanning for grammars: " + this.sourceDirectory );
        try
        {
            SourceInclusionScanner scanner = new StaleSourceScanner( this.staleMillis, this.includes, this.excludes );

            scanner.addSourceMapping( new SuffixMapping( ".jj", ".jj" ) );
            scanner.addSourceMapping( new SuffixMapping( ".JJ", ".JJ" ) );

            Collection staleSources = scanner.getIncludedSources( this.sourceDirectory, this.timestampDirectory );

            for ( Iterator it = staleSources.iterator(); it.hasNext(); )
            {
                File grammarFile = (File) it.next();
                grammarInfos.add( new GrammarInfo( grammarFile, this.packageName ) );
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to scan for grammars: " + this.sourceDirectory, e );
        }
        getLog().debug( "Found grammars: " + grammarInfos );

        return (GrammarInfo[]) grammarInfos.toArray( new GrammarInfo[grammarInfos.size()] );
    }

    /**
     * Passes the specified grammar file through JavaCC.
     * 
     * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>null</code>.
     * @throws MojoExecutionException If the invocation of JavaCC failed.
     * @throws MojoFailureException If JavaCC reported a non-zero exit code.
     */
    private void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjFile = grammarInfo.getGrammarFile();
        File outputDir = new File( this.outputDirectory, grammarInfo.getPackageDirectory().getPath() );

        // Copy all .java files from sourceDirectory to outputDirectory, in
        // order to prevent regeneration of customized Token.java or similar
        try
        {
            FileUtils.copyDirectory( jjFile.getParentFile(), outputDir, "*.java", "*.jj,*.JJ" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy custom source files to output directory:"
                + jjFile.getParent() + " -> " + outputDir, e );
        }

        // generate parser file
        runJavaCC( jjFile, outputDir );

        // create timestamp file
        try
        {
            URI relativeURI = this.sourceDirectory.toURI().relativize( jjFile.toURI() );
            File timestampFile = new File( this.timestampDirectory.toURI().resolve( relativeURI ) );
            FileUtils.copyFile( jjFile, timestampFile );
        }
        catch ( Exception e )
        {
            getLog().warn( "Failed to create copy for timestamp check: " + jjFile, e );
        }
    }

}

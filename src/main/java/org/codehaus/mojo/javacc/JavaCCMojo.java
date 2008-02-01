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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
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
        if ( !this.sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + this.sourceDirectory );
            return;
        }

        // check packageName for . vs /
        if ( this.packageName != null )
        {
            this.packageName = StringUtils.replace( this.packageName, '.', File.separatorChar );
        }

        if ( !this.timestampDirectory.exists() )
        {
            this.timestampDirectory.mkdirs();
        }

        if ( this.includes == null )
        {
            this.includes = Collections.singleton( "**/*" );
        }

        if ( this.excludes == null )
        {
            this.excludes = Collections.EMPTY_SET;
        }

        Set staleGrammars = computeStaleGrammars( this.sourceDirectory, this.timestampDirectory );

        if ( staleGrammars.isEmpty() )
        {
            getLog().info( "Skipping - all grammars up to date: " + this.sourceDirectory );
        }
        else
        {
            for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
            {
                File jjFile = (File) i.next();
                File outputDir = getOutputDirectory( jjFile );

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

        if ( this.project != null )
        {
            getLog().debug( "Adding compile source root: " + this.outputDirectory );
            this.project.addCompileSourceRoot( this.outputDirectory.getAbsolutePath() );
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

        SourceInclusionScanner scanner = new StaleSourceScanner( this.staleMillis, this.includes, this.excludes );

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

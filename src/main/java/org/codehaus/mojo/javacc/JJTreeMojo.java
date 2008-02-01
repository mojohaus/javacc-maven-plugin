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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Parses a JJTree grammar file (<code>*.jjt</code>) and transforms it to Java source files and a JavaCC grammar
 * file. Please see the <a href="https://javacc.dev.java.net/doc/JJTree.html">JJTree Reference Documentation</a> for
 * more information.
 * 
 * @goal jjtree
 * @phase generate-sources
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JJTreeMojo
    extends AbstractMojo
{

    /**
     * The Java version for which to generate source code. Default value is <code>1.4</code>.
     * 
     * @parameter expression="${jdkVersion}"
     * @since 2.4
     */
    private String jdkVersion;

    /**
     * A flag whether to generate sample implementations for <code>SimpleNode</code> and any other nodes used in the
     * grammar. Default value is <code>true</code>.
     * 
     * @parameter expression="${buildNodeFiles}"
     */
    private Boolean buildNodeFiles;

    /**
     * A flag whether to generate a multi mode parse tree or a single mode parse tree. Default value is
     * <code>false</code>.
     * 
     * @parameter expression="${multi}"
     */
    private Boolean multi;

    /**
     * A flag whether to make each non-decorated production void instead of an indefinite node. Default value is
     * <code>false</code>.
     * 
     * @parameter expression="${nodeDefaultVoid}"
     */
    private Boolean nodeDefaultVoid;

    /**
     * The name of a custom factory class to create <code>Node</code> objects.
     * 
     * @parameter expression="${nodeFactory}"
     */
    private Boolean nodeFactory;

    /**
     * The package to generate the node classes into. By default, the package of the corresponding parser is used.
     * 
     * @parameter expression="${nodePackage}"
     */
    private String nodePackage;

    /**
     * The prefix used to construct node class names from node identifiers in multi mode. Default value is
     * <code>AST</code>.
     * 
     * @parameter expression="${nodePrefix}"
     */
    private String nodePrefix;

    /**
     * A flag whether user-defined parser methods should be called on entry and exit of every node scope. Default value
     * is <code>false</code>.
     * 
     * @parameter expression="${nodeScopeHook}"
     */
    private Boolean nodeScopeHook;

    /**
     * A flag whether the node construction routines need an additional method parameter to receive the parser object.
     * Default value is <code>false</code>.
     * 
     * @parameter expression="${nodeUsesParser}"
     */
    private Boolean nodeUsesParser;

    /**
     * A flag whether to generate code for a static parser. Note that this setting must match the corresponding option
     * for the <code>javacc</code> mojo. Default value is <code>true</code>.
     * 
     * @parameter expression="${isStatic}" alias="staticOption"
     */
    private Boolean isStatic;

    /**
     * A flag whether to insert a <code>jjtAccept()</code> method in the node classes and to generate a visitor
     * implementation with an entry for every node type used in the grammar. Default value is <code>false</code>.
     * 
     * @parameter expression="${visitor}"
     */
    private Boolean visitor;

    /**
     * The qualified name of an exception class to include in the signature of the generated <code>jjtAccept()</code>
     * and <code>visit()</code> methods. By default, the <code>throws</code> clause of the generated methods is
     * empty such that only unchecked exceptions can be thrown.
     * 
     * @parameter expression="${visitorException}"
     */
    private String visitorException;

    /**
     * Directory where the input JJTree files (<code>*.jjt</code>) are located.
     * 
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/jjtree"
     */
    private File sourceDirectory;

    /**
     * Directory where the output Java files for the node classes and the JavaCC grammar file will be located.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/jjtree"
     */
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     * 
     * @parameter expression="${timestampDirectory}"
     *            default-value="${project.build.directory}/generated-sources/jjtree-timestamp"
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
     * Contains the package name to use for the generated code.
     */
    private String packageName;

    /**
     * Execute the JJTree preprocessor.
     * 
     * @throws MojoExecutionException If the invocation of JJTree failed.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !this.sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + this.sourceDirectory );
            return;
        }

        if ( this.nodePackage != null )
        {
            this.packageName = StringUtils.replace( this.nodePackage, '.', File.separatorChar );
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

        Set staleGrammars = computeStaleGrammars();

        if ( staleGrammars.isEmpty() )
        {
            getLog().info( "Skipping - all grammars up to date: " + this.sourceDirectory );
        }
        else
        {
            for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
            {
                File jjtFile = (File) i.next();
                File outputDir = getOutputDirectory( jjtFile );

                // generate final grammar file
                JJTree jjtree = new JJTree();
                jjtree.setInputFile( jjtFile );
                jjtree.setOutputDirectory( outputDir );
                jjtree.setJdkVersion( this.jdkVersion );
                jjtree.setStatic( this.isStatic );
                jjtree.setBuildNodeFiles( this.buildNodeFiles );
                jjtree.setMulti( this.multi );
                jjtree.setNodeDefaultVoid( this.nodeDefaultVoid );
                jjtree.setNodeFactory( this.nodeFactory );
                jjtree.setNodePackage( this.nodePackage );
                jjtree.setNodePrefix( this.nodePrefix );
                jjtree.setNodeScopeHook( this.nodeScopeHook );
                jjtree.setNodeUsesParser( this.nodeUsesParser );
                jjtree.setVisitor( this.visitor );
                jjtree.setVisitorException( this.visitorException );
                jjtree.run( getLog() );

                // create timestamp file
                try
                {
                    URI relativeURI = this.sourceDirectory.toURI().relativize( jjtFile.toURI() );
                    File timestampFile = new File( this.timestampDirectory.toURI().resolve( relativeURI ) );
                    FileUtils.copyFile( jjtFile, timestampFile );
                }
                catch ( Exception e )
                {
                    getLog().warn( "Failed to create copy for timestamp check: " + jjtFile, e );
                }
            }
        }
    }

    /**
     * Get the output directory for the JavaCC files.
     * 
     * @param jjtFile The JJTree input file.
     * @return The directory that will contain the generated code.
     * @throws MojoExecutionException If there is a problem getting the package name.
     */
    private File getOutputDirectory( File jjtFile )
        throws MojoExecutionException
    {
        try
        {
            GrammarInfo info = new GrammarInfo( jjtFile, this.packageName );
            return new File( this.outputDirectory, info.getPackageDirectory().getPath() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to retrieve package name from grammar file", e );
        }
    }

    /**
     * @return A set of <code>File</code> objects to compile.
     * @throws MojoExecutionException If it fails.
     */
    private Set computeStaleGrammars()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".jjt", ".jjt" );
        SuffixMapping mappingCAP = new SuffixMapping( ".JJT", ".JJT" );

        SourceInclusionScanner scanner = new StaleSourceScanner( this.staleMillis, this.includes, this.excludes );

        scanner.addSourceMapping( mapping );
        scanner.addSourceMapping( mappingCAP );

        Set staleSources = new HashSet();

        try
        {
            staleSources.addAll( scanner.getIncludedSources( this.sourceDirectory, this.timestampDirectory ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + this.sourceDirectory
                + "\' for stale grammars to reprocess.", e );
        }

        return staleSources;
    }

}

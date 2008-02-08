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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

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
     * The package to generate the AST node classes into. This value may use a leading asterisk to reference the package
     * of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this parameter
     * is set to <code>*.demo</code>, the tree node classes will be located in the package
     * <code>org.apache.demo</code>. By default, the package of the corresponding parser is used.
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
     * Execute the JJTree preprocessor.
     * 
     * @throws MojoExecutionException If the invocation of JJTree failed.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
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

            scanner.addSourceMapping( new SuffixMapping( ".jjt", ".jjt" ) );
            scanner.addSourceMapping( new SuffixMapping( ".JJT", ".JJT" ) );

            Collection staleSources = scanner.getIncludedSources( this.sourceDirectory, this.timestampDirectory );

            for ( Iterator it = staleSources.iterator(); it.hasNext(); )
            {
                File grammarFile = (File) it.next();
                grammarInfos.add( new GrammarInfo( grammarFile ) );
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
     * Passes the specified grammar file through JJTree.
     * 
     * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>null</code>.
     * @throws MojoExecutionException If the invocation of JJTree failed.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    private void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjtFile = grammarInfo.getGrammarFile();

        // determine target directory for tree node files
        String nodePackageName = grammarInfo.resolvePackageName( this.nodePackage );
        File nodeDirectory;
        if ( nodePackageName != null )
        {
            nodeDirectory = new File( nodePackageName.replace( '.', File.separatorChar ) );
        }
        else
        {
            nodeDirectory = grammarInfo.getPackageDirectory();
        }
        nodeDirectory = new File( this.outputDirectory, nodeDirectory.getPath() );

        // generate final grammar file and node files
        JJTree jjtree = newJJTree();
        jjtree.setInputFile( jjtFile );
        jjtree.setOutputDirectory( nodeDirectory );
        jjtree.setNodePackage( nodePackageName );
        jjtree.run();

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

    /**
     * Creates a new facade to invoke JJTree. Most options for the invocation are derived from the current values of the
     * corresponding mojo parameters. The caller is responsible to set the input file, output directory and package on
     * the returned facade.
     * 
     * @return The facade for the tool invocation, never <code>null</code>.
     */
    protected JJTree newJJTree()
    {
        JJTree jjtree = new JJTree();
        jjtree.setLog( getLog() );
        jjtree.setJdkVersion( this.jdkVersion );
        jjtree.setStatic( this.isStatic );
        jjtree.setBuildNodeFiles( this.buildNodeFiles );
        jjtree.setMulti( this.multi );
        jjtree.setNodeDefaultVoid( this.nodeDefaultVoid );
        jjtree.setNodeFactory( this.nodeFactory );
        jjtree.setNodePrefix( this.nodePrefix );
        jjtree.setNodeScopeHook( this.nodeScopeHook );
        jjtree.setNodeUsesParser( this.nodeUsesParser );
        jjtree.setVisitor( this.visitor );
        jjtree.setVisitorException( this.visitorException );
        return jjtree;
    }

}

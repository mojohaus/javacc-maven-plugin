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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.javacc.jjtree.JJTree;

/**
 * Preprocesses decorated grammar files (<code>*.jjt</code>) with JJTree and passes the output to JavaCC in order to
 * finally generate a parser with parse tree actions.
 * 
 * @goal jjtree-javacc
 * @phase generate-sources
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class JJTreeJavaCCMojo
    extends AbstractJavaCCMojo
{

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
     * The name of a custom factory class to create <code>Node</code> objects. Default value is <code>""</code>.
     * 
     * @parameter expression="${nodeFactory}"
     */
    private Boolean nodeFactory;

    /**
     * The package to generate the node classes into. Default value is <code>""</code> meaning to use the package of
     * the corresponding parser.
     * 
     * @parameter expression="${nodePackage}"
     */
    private String nodePackage;

    /**
     * The prefix used to construct node class names from node identifiers in multi mode. Default value is
     * <code>"AST"</code>.
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
     * A flag whether to insert a <code>jjtAccept()</code> method in the node classes and to generate a visitor
     * implementation with an entry for every node type used in the grammar. Default value is <code>false</code>.
     * 
     * @parameter expression="${visitor}"
     */
    private Boolean visitor;

    /**
     * The name of an exception class to include in the signature of the generated <code>jjtAccept()</code> and
     * <code>visit()</code> methods. Default value is <code>""</code>.
     * 
     * @parameter expression="${visitorException}"
     */
    private String visitorException;

    /**
     * The directory where the decorated JavaCC grammar files (<code>*.jjt</code>) are located. It will be
     * recursively scanned for input files to pass to JJTree. The parameters <code>includes</code> and
     * <code>excludes</code> can be used to select a subset of files.
     * 
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/jjtree"
     */
    private File sourceDirectory;

    /**
     * The directory where the interim output from JJTree will be stored. This directory will hold the prepared grammar
     * files (<code>*.jj</code>) along with their tree node files (<code>*.java</code>). The prepared grammar
     * files will then be passed on to JavaCC to generate the parser files.
     * 
     * @parameter expression="${interimDirectory}" default-value="${project.build.directory}/generated-sources/jjtree"
     */
    private File interimDirectory;

    /**
     * The directory where the generated Java files will be stored. More precisely, the parser files generated by JavaCC
     * together with some auxiliary files and the previously generated tree node files from JJTree will be saved here.
     * The directory will be registered as a compile source root of the project such that the generated files will
     * participate in later build phases like compiling and packaging.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/javacc"
     */
    private File outputDirectory;

    /**
     * A set of Ant-like inclusion patterns used to select files from the source directory for processing. By default,
     * the patterns <code>"**&#47;*.jj"</code>, <code>"**&#47;*.JJ"</code>, <code>"**&#47;*.jjt"</code> and
     * <code>"**&#47;*.JJT"</code> are used to select grammar files.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from being processing. By default, this set if
     * empty such that no files are excluded.
     * 
     * @parameter
     */
    private String[] excludes;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a grammar file needs
     * recompilation.
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * The current Maven project.
     * 
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Executes JJTree and JavaCC.
     * 
     * @throws MojoExecutionException If the invocation of JJTree or JavaCC failed.
     * @throws MojoFailureException If JJTree or JavaCC reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !this.sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + this.sourceDirectory );
            return;
        }

        GrammarInfo[] grammarInfos = scanForGrammars();

        if ( grammarInfos.length <= 0 )
        {
            getLog().info( "Skipping - all grammars up to date: " + this.sourceDirectory );
        }
        else
        {
            for ( int i = 0; i < grammarInfos.length; i++ )
            {
                processGrammar( grammarInfos[i] );
            }
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
     * @return An array of grammar infos describing the found grammar files, never <code>null</code>.
     * @throws MojoExecutionException If the source directory could not be scanned.
     */
    private GrammarInfo[] scanForGrammars()
        throws MojoExecutionException
    {
        getLog().debug( "Scanning for grammars: " + this.sourceDirectory );
        GrammarInfo[] grammarInfos;
        try
        {
            String[] defaultIncludes = { "**/*.jj", "**/*.JJ", "**/*.jjt", "**/*.JJT" };
            GrammarDirectoryScanner scanner = new GrammarDirectoryScanner();
            scanner.setSourceDirectory( this.sourceDirectory );
            scanner.setIncludes( ( this.includes != null ) ? this.includes : defaultIncludes );
            scanner.setExcludes( this.excludes );
            scanner.setOutputDirectory( this.outputDirectory );
            scanner.setStaleMillis( this.staleMillis );
            scanner.scan();
            grammarInfos = scanner.getIncludedGrammars();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to scan source root for grammars: " + this.sourceDirectory, e );
        }
        getLog().debug( "Found grammars: " + Arrays.asList( grammarInfos ) );
        return grammarInfos;
    }

    /**
     * Passes the specified grammar file through JJTree and JavaCC.
     * 
     * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>null</code>.
     * @throws MojoExecutionException If the invocation of JJTree or JavaCC failed.
     * @throws MojoFailureException If JJTree or JavaCC reported a non-zero exit code.
     */
    private void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjtFile = grammarInfo.getGrammarFile();
        File jjtDirectory = jjtFile.getParentFile();

        // determine target directory of grammar file (*.jj) and node files (*.java) generated by JJTree
        File jjDirectory = new File( this.interimDirectory, grammarInfo.getPackageDirectory().getPath() );

        // determine target location of grammar file (*.jj) generated by JJTree
        File jjFile = new File( jjDirectory, FileUtils.removeExtension( jjtFile.getName() ) + ".jj" );

        // determine output directory of parser file (*.java) generated by JavaCC
        File parserDirectory = new File( this.outputDirectory, grammarInfo.getPackageDirectory().getPath() );

        // determine output directory of tree node files (*.java) generated by JJTree
        File nodeDirectory;
        if ( this.nodePackage != null )
        {
            nodeDirectory = new File( this.nodePackage.replace( '.', File.separatorChar ) );
        }
        else
        {
            nodeDirectory = grammarInfo.getPackageDirectory();
        }
        nodeDirectory = new File( this.outputDirectory, nodeDirectory.getPath() );

        // generate final grammar file
        runJJTree( jjtFile, jjDirectory );

        // copy generated tree node files to output directory
        try
        {
            getLog().debug( "Copying tree nodes files: " + jjDirectory + " -> " + nodeDirectory );
            FileUtils.copyDirectory( jjDirectory, nodeDirectory, "*.java", "" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy tree nodes files to output directory: " + jjDirectory
                + " -> " + nodeDirectory, e );
        }

        // copy custom source files to output directory
        try
        {
            getLog().debug( "Copying custom source files: " + jjtDirectory + " -> " + parserDirectory );
            FileUtils.copyDirectory( jjtDirectory, parserDirectory, "*.java", "" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy custom source files to output directory:" + jjtDirectory
                + " -> " + parserDirectory, e );
        }

        // generate parser file
        runJavaCC( jjFile, parserDirectory );
    }

    /**
     * Runs JJTree on the specified decorated grammar file to generate a grammar file annotated with node actions. The
     * options for JJTree are derived from the current values of the corresponding mojo parameters.
     * 
     * @param jjtFile The absolute path to the grammar file to pass into JJTree for preprocessing, must not be
     *            <code>null</code>.
     * @param grammarDirectory The absolute path to the output directory for the generated grammar file, must not be
     *            <code>null</code>. Note that this path should already include the desired package hierarchy because
     *            JJTree will not append the required sub directories automatically.
     * @throws MojoExecutionException If JJTree could not be invoked.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    private void runJJTree( File jjtFile, File grammarDirectory )
        throws MojoExecutionException, MojoFailureException
    {
        int exitCode;
        try
        {
            String[] args = generateArgumentsForJJTree( jjtFile, grammarDirectory );
            getLog().debug( "Running JJTree: " + Arrays.asList( args ) );
            if ( !grammarDirectory.exists() )
            {
                grammarDirectory.mkdirs();
            }
            JJTree jjtree = new JJTree();
            exitCode = jjtree.main( args );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to execute JJTree", e );
        }
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "JJTree reported exit code " + exitCode + ": " + jjtFile );
        }
    }

    /**
     * Assembles the command line arguments for the invocation of JJTree according to the mojo configuration.<br/><br/>
     * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in the grammar file,
     * only those mojo parameters that have been explicitly set by the user are passed on the command line.
     * 
     * @param jjtFile The absolute path of the grammar file to compile, must not be <code>null</code>.
     * @param grammarDirectory The absolute path to the output directory for the generated grammar file, must not be
     *            <code>null</code>. Note that this path should already include the desired package hierarchy because
     *            JJTree will not append the required sub directories automatically.
     * @return A string array that represents the arguments to use for JJTree.
     */
    private String[] generateArgumentsForJJTree( File jjtFile, File grammarDirectory )
    {
        List argsList = new ArrayList();

        if ( getJdkVersion() != null )
        {
            argsList.add( "-JDK_VERSION=" + getJdkVersion() );
        }

        if ( this.buildNodeFiles != null )
        {
            argsList.add( "-BUILD_NODE_FILES=" + this.buildNodeFiles );
        }

        if ( this.multi != null )
        {
            argsList.add( "-MULTI=" + this.multi );
        }

        if ( this.nodeDefaultVoid != null )
        {
            argsList.add( "-NODE_DEFAULT_VOID=" + this.nodeDefaultVoid );
        }

        if ( this.nodeFactory != null )
        {
            argsList.add( "-NODE_FACTORY=" + this.nodeFactory );
        }

        if ( this.nodePackage != null )
        {
            argsList.add( "-NODE_PACKAGE=" + this.nodePackage );
        }

        if ( this.nodePrefix != null )
        {
            argsList.add( "-NODE_PREFIX=" + this.nodePrefix );
        }

        if ( this.nodeScopeHook != null )
        {
            argsList.add( "-NODE_SCOPE_HOOK=" + this.nodeScopeHook );
        }

        if ( this.nodeUsesParser != null )
        {
            argsList.add( "-NODE_USES_PARSER=" + this.nodeUsesParser );
        }

        if ( getIsStatic() != null )
        {
            argsList.add( "-STATIC=" + getIsStatic() );
        }

        if ( this.visitor != null )
        {
            argsList.add( "-VISITOR=" + this.visitor );
        }

        if ( this.visitorException != null )
        {
            argsList.add( "-VISITOR_EXCEPTION=\'" + this.visitorException + "\'" );
        }

        argsList.add( "-OUTPUT_DIRECTORY=" + grammarDirectory.getAbsolutePath() );

        argsList.add( jjtFile.getAbsolutePath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

}

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Preprocesses decorated grammar files (<code>*.jjt</code>) with JJTree and passes the output to JavaCC in order to
 * finally generate a parser with parse tree actions.
 * 
 * @goal jjtree-javacc
 * @phase generate-sources
 * @since 2.4
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
     * The name of a custom factory class to create <code>Node</code> objects.
     * 
     * @parameter expression="${nodeFactory}"
     */
    private Boolean nodeFactory;

    /**
     * The package to generate the AST node classes into. This value may use a leading asterisk to reference the package
     * of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this parameter
     * is set to <code>*.node</code>, the tree node classes will be located in the package
     * <code>org.apache.node</code>. By default, the package of the corresponding parser is used.
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
     * the patterns <code>**&#47;*.jj</code>, <code>**&#47;*.JJ</code>, <code>**&#47;*.jjt</code> and
     * <code>**&#47;*.JJT</code> are used to select grammar files.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from being processed. By default, this set if
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
     * {@inheritDoc}
     */
    protected File getSourceDirectory()
    {
        return this.sourceDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getIncludes()
    {
        if ( this.includes != null )
        {
            return this.includes;
        }
        else
        {
            return new String[] { "**/*.jj", "**/*.JJ", "**/*.jjt", "**/*.JJT" };
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExcludes()
    {
        return this.excludes;
    }

    /**
     * {@inheritDoc}
     */
    protected File getOutputDirectory()
    {
        return this.outputDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected int getStaleMillis()
    {
        return this.staleMillis;
    }

    /**
     * Gets the absolute path to the directory where the interim output from JJTree will be stored.
     * 
     * @return The absolute path to the directory where the interim output from JJTree will be stored.
     */
    private File getInterimDirectory()
    {
        return this.interimDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjtFile = grammarInfo.getGrammarFile();
        File jjtDirectory = jjtFile.getParentFile();

        // determine target directory of grammar file (*.jj) and node files (*.java) generated by JJTree
        File jjDirectory = new File( getInterimDirectory(), grammarInfo.getParserDirectory() );

        // determine output directory of parser file (*.java) generated by JavaCC
        File parserDirectory = new File( getOutputDirectory(), grammarInfo.getParserDirectory() );

        // determine output directory of tree node files (*.java) generated by JJTree
        String nodePackageName = grammarInfo.resolvePackageName( this.nodePackage );
        File nodeDirectory;
        if ( nodePackageName != null )
        {
            nodeDirectory = new File( nodePackageName.replace( '.', File.separatorChar ) );
        }
        else
        {
            nodeDirectory = new File( grammarInfo.getParserDirectory() );
        }
        nodeDirectory = new File( getOutputDirectory(), nodeDirectory.getPath() );

        // generate final grammar file
        JJTree jjtree = newJJTree();
        jjtree.setInputFile( jjtFile );
        jjtree.setOutputDirectory( jjDirectory );
        jjtree.setNodePackage( nodePackageName );
        jjtree.run();

        // copy generated tree node files to output directory
        try
        {
            getLog().debug( "Copying tree nodes files: " + jjDirectory + " -> " + nodeDirectory );
            FileUtils.copyDirectory( jjDirectory, nodeDirectory, "*.java", "" );
        }
        catch ( Exception e )
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
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to copy custom source files to output directory:" + jjtDirectory
                + " -> " + parserDirectory, e );
        }

        // generate parser file
        JavaCC javacc = newJavaCC();
        javacc.setInputFile( jjtree.getOutputFile() );
        javacc.setOutputDirectory( parserDirectory );
        javacc.run();
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
        jjtree.setJdkVersion( getJdkVersion() );
        jjtree.setStatic( getIsStatic() );
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

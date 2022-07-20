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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Preprocesses decorated grammar files (<code>*.jjt</code>) with JJTree and passes the output to JavaCC in order to
 * finally generate a parser with parse tree actions.
 * 
 * @since 2.4
 * @author Benjamin Bentmann
 *
 */
@Mojo(name = "jjtree-javacc", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class JJTreeJavaCCMojo
    extends AbstractJavaCCMojo
{

    /**
     * A flag whether to generate sample implementations for <code>SimpleNode</code> and any other nodes used in the
     * grammar. Default value is <code>true</code>.
     *
     */
    @Parameter(property = "javacc.buildNodeFiles", defaultValue = "true")
    private boolean buildNodeFiles;

    /**
     * A flag whether to generate a multi mode parse tree or a single mode parse tree. Default value is
     * <code>false</code>.
     * 
     */
    @Parameter(property = "javacc.multi", defaultValue = "false")
    private boolean multi;

    /**
     * A flag whether to make each non-decorated production void instead of an indefinite node. Default value is
     * <code>false</code>.
     *
     */
    @Parameter(property = "javacc.nodeDefaultVoid", defaultValue = "false")
    private boolean nodeDefaultVoid;

    /**
     * The name of a custom class that extends <code>SimpleNode</code> and will be used as the super class for the
     * generated tree node classes. By default, the tree node classes will directly extend the class
     * <code>SimpleNode</code>.
     *
     * @since 2.5
     */
    @Parameter(property = "javacc.nodeClass")
    private String nodeClass;

    /**
     * The name of a custom factory class used to create <code>Node</code> objects. This class must have a method with
     * the signature <code>public static Node jjtCreate(int id)</code>. By default, the class <code>SimpleNode</code>
     * will be used as the factory class.
     * 
     */
    @Parameter(property = "javacc.nodeFactory")
    private String nodeFactory;

    /**
     * The package to generate the AST node classes into. This value may use a leading asterisk to reference the package
     * of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this parameter
     * is set to <code>*.node</code>, the tree node classes will be located in the package
     * <code>org.apache.node</code>. By default, the package of the corresponding parser is used.
     *
     */
    @Parameter(property = "javacc.nodePackage")
    private String nodePackage;

    /**
     * The prefix used to construct node class names from node identifiers in multi mode. Default value is
     * <code>AST</code>.
     *
     */
    @Parameter(property = "javacc.nodePrefix", defaultValue = "AST")
    private String nodePrefix;

    /**
     * A flag whether user-defined parser methods should be called on entry and exit of every node scope. Default value
     * is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.nodeScopeHook", defaultValue = "false")
    private boolean nodeScopeHook;

    /**
     * A flag whether the node construction routines need an additional method parameter to receive the parser object.
     * Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.nodeUsesParser", defaultValue = "false")
    private boolean nodeUsesParser;

    /**
     * A flag whether to insert the methods <code>jjtGetFirstToken()</code>, <code>jjtSetFirstToken()</code>,
     * <code>getLastToken()</code> and <code>jjtSetLastToken()</code> into the class <code>SimpleNode</code>. Default
     * value is <code>false</code>.
     * 
     * @since 2.5
     */
    @Parameter(property = "javacc.trackTokens", defaultValue = "false")
    private boolean trackTokens;

    /**
     * A flag whether to insert a <code>jjtAccept()</code> method in the node classes and to generate a visitor
     * implementation with an entry for every node type used in the grammar. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.visitor", defaultValue = "false")
    private boolean visitor;

    /**
     * The name of a class to use for the data argument of the <code>jjtAccept()</code> and <code>visit()</code>
     * methods. Default value is <code>java.lang.Object</code>.
     *
     * @since 2.5
     */
    @Parameter(property = "javacc.visitorDataType")
    private String visitorDataType;

    /**
     * The name of a class to use as the return type of the <code>jjtAccept()</code> and <code>visit()</code> methods.
     * Default value is <code>java.lang.Object</code>.
     *
     * @since 2.5
     */
    @Parameter(property = "javacc.visitorReturnType")
    private String visitorReturnType;

    /**
     * The name of an exception class to include in the signature of the generated <code>jjtAccept()</code> and
     * <code>visit()</code> methods. By default, the <code>throws</code> clause of the generated methods is empty such
     * that only unchecked exceptions can be thrown.
     *
     */
    @Parameter(property = "javacc.visitorException")
    private String visitorException;

    /**
     * The directory where the decorated JavaCC grammar files (<code>*.jjt</code>) are located. It will be
     * recursively scanned for input files to pass to JJTree. The parameters <code>includes</code> and
     * <code>excludes</code> can be used to select a subset of files.
     *
     */
    @Parameter(property = "javacc.sourceDirectory", defaultValue = "${basedir}/src/main/jjtree")
    private File sourceDirectory;

    /**
     * The directory where the AST node files generated by JJTree will be stored. The directory will be registered as a
     * compile source root of the project such that the generated files will participate in later build phases like
     * compiling and packaging.
     *
     */
    @Parameter(property = "javacc.interimDirectory", defaultValue = "${project.build.directory}/generated-sources/jjtree")
    private File interimDirectory;

    /**
     * The directory where the parser files generated by JavaCC will be stored. The directory will be registered as a
     * compile source root of the project such that the generated files will participate in later build phases like
     * compiling and packaging.
     *
     */
    @Parameter(property = "javacc.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/javacc")
    private File outputDirectory;

    /**
     * A set of Ant-like inclusion patterns used to select files from the source directory for processing. By default,
     * the patterns <code>**&#47;*.jj</code>, <code>**&#47;*.JJ</code>, <code>**&#47;*.jjt</code> and
     * <code>**&#47;*.JJT</code> are used to select grammar files.
     *
     */
    @Parameter
    private String[] includes;

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from being processed. By default, this set is
     * empty such that no files are excluded.
     *
     */
    @Parameter
    private String[] excludes;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a grammar file needs
     * recompilation.
     *
     */
    @Parameter(property = "javacc.lastModGranularityMs", defaultValue = "0")
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
    protected File[] getCompileSourceRoots()
    {
        return new File[] { getOutputDirectory(), getInterimDirectory() };
    }

    /**
     * {@inheritDoc}
     */
    protected void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjtFile = grammarInfo.getGrammarFile();
        File jjtDirectory = jjtFile.getParentFile();

        File tempDirectory = getTempDirectory();

        // setup output directory of grammar file (*.jj) and node files (*.java) generated by JJTree
        File jjDirectory = new File( tempDirectory, "node" );

        // setup output directory of parser file (*.java) generated by JavaCC
        File parserDirectory = new File( tempDirectory, "parser" );

        // setup output directory of tree node files (*.java) generated by JJTree
        String nodePackageName = grammarInfo.resolvePackageName( this.nodePackage );

        // generate final grammar file
        JJTree jjtree = newJJTree();
        jjtree.setInputFile( jjtFile );
        jjtree.setOutputDirectory( jjDirectory );
        jjtree.setNodePackage( nodePackageName );
        jjtree.run();

        // generate parser files
        JavaCC javacc = newJavaCC();
        javacc.setInputFile( jjtree.getOutputFile() );
        javacc.setOutputDirectory( parserDirectory );
        javacc.run();

        // copy output from JJTree
        copyGrammarOutput( getInterimDirectory(), ( nodePackageName != null ) ? nodePackageName
                        : grammarInfo.getParserPackage(), jjDirectory, grammarInfo.getParserName() + "TreeConstants*" );

        // copy parser files from JavaCC
        copyGrammarOutput( getOutputDirectory(), grammarInfo.getParserPackage(), parserDirectory,
                           grammarInfo.getParserName() + "*" );

        // copy source files which are next to grammar unless the grammar resides in an ordinary source root
        // (legacy support for custom sources)
        if ( !isSourceRoot( grammarInfo.getSourceDirectory() ) )
        {
            copyGrammarOutput( getOutputDirectory(), grammarInfo.getParserPackage(), jjtDirectory, "*" );
        }

        deleteTempDirectory( tempDirectory );
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
        jjtree.setGrammarEncoding( getGrammarEncoding() );
        jjtree.setJdkVersion( getJdkVersion() );
        jjtree.setStatic( getIsStatic() );
        jjtree.setBuildNodeFiles( this.buildNodeFiles );
        jjtree.setMulti( this.multi );
        jjtree.setNodeDefaultVoid( this.nodeDefaultVoid );
        jjtree.setNodeClass( this.nodeClass );
        jjtree.setNodeFactory( this.nodeFactory );
        jjtree.setNodePrefix( this.nodePrefix );
        jjtree.setNodeScopeHook( this.nodeScopeHook );
        jjtree.setNodeUsesParser( this.nodeUsesParser );
        jjtree.setTrackTokens( this.trackTokens );
        jjtree.setVisitor( this.visitor );
        jjtree.setVisitorDataType( this.visitorDataType );
        jjtree.setVisitorReturnType( this.visitorReturnType );
        jjtree.setVisitorException( this.visitorException );
        return jjtree;
    }

}

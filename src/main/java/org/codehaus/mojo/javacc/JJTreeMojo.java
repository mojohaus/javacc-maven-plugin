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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.javacc.jjtree.JJTree;

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
     * The package to generate the node classes into. Default value is <code>""</code> meaning to use the package of
     * the corresponding parser.
     * 
     * @parameter expression="${nodePackage}"
     */
    private String nodePackage;

    /**
     * The name of an exception class to include in the signature of the generated <code>jjtAccept()</code> and
     * <code>visit()</code> methods. Default value is <code>""</code>.
     * 
     * @parameter expression="${visitorException}"
     */
    private String visitorException;

    /**
     * The prefix used to construct node class names from node identifiers in multi mode. Default value is
     * <code>"AST"</code>.
     * 
     * @parameter expression="${nodePrefix}"
     */
    private String nodePrefix;

    /**
     * Directory where the input JJTree files (<code>*.jjt</code>) are located.
     * 
     * @parameter expression="${basedir}/src/main/jjtree"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where the output Java files for the node classes and the JavaCC grammar file will be located.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/jjtree"
     * @required
     */
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/jjtree-timestamp"
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
     * @throws MojoExecutionException If the compilation fails.
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( !sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + sourceDirectory );
            return;
        }

        if ( nodePackage != null )
        {
            packageName = StringUtils.replace( nodePackage, '.', File.separatorChar );
        }

        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
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

        Set staleGrammars = computeStaleGrammars();

        if ( staleGrammars.isEmpty() )
        {
            getLog().info( "Nothing to process - all grammars in " + sourceDirectory + " are up to date." );
            return;
        }

        for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
        {
            File jjTreeFile = (File) i.next();
            try
            {
                JJTree jjtree = new JJTree();
                jjtree.main( generateArgumentList( jjTreeFile.getAbsolutePath() ) );

                URI relativeURI = sourceDirectory.toURI().relativize( jjTreeFile.toURI() );
                File timestampFile = new File( timestampDirectory.toURI().resolve( relativeURI ) );
                FileUtils.copyFile( jjTreeFile, timestampFile );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "JJTree execution failed", e );
            }
        }
    }

    /**
     * Get the output directory for the JavaCC files.
     * 
     * @param jjtreeInput The path to the JJTree file.
     * @return The directory that will contain the generated code.
     * @throws MojoExecutionException If there is a problem getting the package name.
     */
    private File getOutputDirectory( String jjtreeInput )
        throws MojoExecutionException
    {
        if ( packageName != null )
        {
            return new File( outputDirectory, packageName );
        }
        else
        {
            String declaredPackage = JavaCCUtil.getDeclaredPackage( new File( jjtreeInput ) );

            if ( declaredPackage != null )
            {
                return new File( outputDirectory, declaredPackage );
            }
        }
        return outputDirectory;
    }

    /**
     * Create the argument list to be passed to JJTree on the command line.
     * 
     * @param jjTreeFilename A <code>String</code> which represents the path of the file to compile.
     * @return A string array that represents the arguments to use for JJTree.
     * @throws MojoExecutionException If it fails.
     */
    private String[] generateArgumentList( String jjTreeFilename )
        throws MojoExecutionException
    {

        ArrayList argsList = new ArrayList();

        if ( buildNodeFiles != null )
        {
            argsList.add( "-BUILD_NODE_FILES=" + buildNodeFiles.toString() );
        }

        if ( multi != null )
        {
            argsList.add( "-MULTI=" + multi );
        }

        if ( nodeDefaultVoid != null )
        {
            argsList.add( "-NODE_DEFAULT_VOID=" + nodeDefaultVoid );
        }

        if ( nodeFactory != null )
        {
            argsList.add( "-NODE_FACTORY=" + nodeFactory );
        }

        if ( nodePackage != null )
        {
            argsList.add( "-NODE_PACKAGE=" + nodePackage );
        }

        if ( nodePrefix != null )
        {
            argsList.add( "-NODE_PREFIX=" + nodePrefix );
        }

        if ( nodeScopeHook != null )
        {
            argsList.add( "-NODE_SCOPE_HOOK=" + nodeScopeHook );
        }

        if ( nodeUsesParser != null )
        {
            argsList.add( "-NODE_USES_PARSER=" + nodeUsesParser );
        }

        if ( visitor != null )
        {
            argsList.add( "-VISITOR=" + visitor );
        }

        if ( isStatic != null )
        {
            argsList.add( "-STATIC=" + isStatic );
        }

        if ( visitorException != null )
        {
            argsList.add( "-VISITOR_EXCEPTION=\'" + visitorException + "\'" );
        }

        argsList.add( "-OUTPUT_DIRECTORY:" + getOutputDirectory( jjTreeFilename ) );

        argsList.add( jjTreeFilename );

        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
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

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis, includes, excludes );

        scanner.addSourceMapping( mapping );
        scanner.addSourceMapping( mappingCAP );

        Set staleSources = new HashSet();

        try
        {
            staleSources.addAll( scanner.getIncludedSources( sourceDirectory, timestampDirectory ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDirectory
                + "\' for stale grammars to reprocess.", e );
        }

        return staleSources;
    }

}

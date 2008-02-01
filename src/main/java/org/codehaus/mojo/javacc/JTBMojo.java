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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Parses a JTB file and transforms it into source files for an AST and a JavaCC grammar file which automatically builds
 * the AST. <b>Note: <a href="http://compilers.cs.ucla.edu/jtb/">JTB</a> requires Java 1.5 or higher. This mojo will
 * not work with earlier versions of the JRE</b>.
 * 
 * @goal jtb
 * @phase generate-sources
 * @author Gregory Kick (gk5885@kickstyle.net)
 * @version $Id$
 */
public class JTBMojo
    extends AbstractMojo
{

    /**
     * This option is short for <code>nodePackageName</code> = <code>&lt;packageName&gt;.syntaxtree</code> and
     * <code>visitorPackageName</code> = <code>&lt;packageName&gt;.visitor</code>. Note that this option takes
     * precedence over <code>nodePackageName</code> and <code>visitorPackageName</code> if specified.
     * 
     * @parameter expression="${package}"
     */
    private String packageName;

    /**
     * This option specifies the package for the generated AST nodes. Default value is <code>syntaxtree</code>.
     * 
     * @parameter expression="${nodePackageName}"
     */
    private String nodePackageName;

    /**
     * This option specifies the package for the generated visitors. Default value is <code>visitor</code>.
     * 
     * @parameter expression="${visitorPackageName}"
     */
    private String visitorPackageName;

    /**
     * If <code>true</code>, JTB will suppress its semantic error checking. Default value is <code>false</code>.
     * 
     * @parameter expression="${supressErrorChecking}"
     */
    private Boolean supressErrorChecking;

    /**
     * If <code>true</code>, all generated comments will be wrapped in <code>&lt;pre&gt;</code> tags so that they
     * are formatted correctly in API docs. Default value is <code>false</code>.
     * 
     * @parameter expression="${javadocFriendlyComments}"
     */
    private Boolean javadocFriendlyComments;

    /**
     * Setting this option to <code>true</code> causes JTB to generate field names that reflect the structure of the
     * tree instead of generic names like <code>f0</code>, <code>f1</code> etc. Default value is <code>false</code>.
     * 
     * @parameter expression="${descriptiveFieldNames}"
     */
    private Boolean descriptiveFieldNames;

    /**
     * The qualified name of a user-defined class from which all AST nodes will inherit. By default, AST nodes will
     * inherit from the generated class <code>Node</code>.
     * 
     * @parameter expression="${nodeParentClass}"
     */
    private String nodeParentClass;

    /**
     * If <code>true</code>, all nodes will contain fields for its parent node. Default value is <code>false</code>.
     * 
     * @parameter expression="${parentPointers}"
     */
    private Boolean parentPointers;

    /**
     * If <code>true</code>, JTB will include JavaCC "special tokens" in the AST. Default value is <code>false</code>.
     * 
     * @parameter expression="${specialTokens}"
     */
    private Boolean specialTokens;

    /**
     * If <code>true</code>, JTB will generate the following files to support the Schema programming language:
     * <ul>
     * <li>Scheme records representing the grammar.</li>
     * <li>A Scheme tree building visitor.</li>
     * </ul>
     * Default value is <code>false</code>.
     * 
     * @parameter expression="${scheme}"
     */
    private Boolean scheme;

    /**
     * If <code>true</code>, JTB will generate a syntax tree dumping visitor. Default value is <code>false</code>.
     * 
     * @parameter expression="${printer}"
     */
    private Boolean printer;

    /**
     * The directory where the JavaCC grammar files (<code>*.jtb</code>) are located. It will be recursively scanned
     * for input files to pass to JTB.
     * 
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/jtb"
     */
    private File sourceDirectory;

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
     * The directory where the output Java files will be located.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/jtb"
     */
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     * 
     * @parameter expression="${timestampDirectory}"
     *            default-value="${project.build.directory}/generated-sources/jtb-timestamp"
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The current working directory. Unfortunately, JTB always outputs in this directory so we need it to find the
     * generated files.
     * 
     * @parameter expression="${user.dir}"
     * @required
     * @readonly
     */
    private File workingDirectory;

    /**
     * Execute the JTB preprocessor.
     * 
     * @throws MojoExecutionException If the invocation of JTB failed.
     * @throws MojoFailureException If JTB reported a non-zero exit code.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !this.sourceDirectory.isDirectory() )
        {
            getLog().info( "Skipping non-existing source directory: " + this.sourceDirectory );
            return;
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
                File jtbFile = (File) i.next();
                String nodePackage = getNodePackageName();
                String visitorPackage = getVisitorPackageName();

                // generate final grammar file and the node/visitor files
                runJTB( jtbFile, this.outputDirectory );

                /*
                 * since jtb was meant to be run as a command-line tool, it only outputs to the current directory.
                 * therefore, the files must be moved to the correct locations.
                 */
                movePackage( nodePackage );
                movePackage( visitorPackage );

                // create timestamp file
                try
                {
                    URI relativeURI = this.sourceDirectory.toURI().relativize( jtbFile.toURI() );
                    File timestampFile = new File( this.timestampDirectory.toURI().resolve( relativeURI ) );
                    FileUtils.copyFile( jtbFile, timestampFile );
                }
                catch ( Exception e )
                {
                    getLog().warn( "Failed to create copy for timestamp check: " + jtbFile, e );
                }
            }
        }

        if ( this.project != null )
        {
            getLog().debug( "Adding compile source root: " + this.outputDirectory );
            this.project.addCompileSourceRoot( this.outputDirectory.getPath() );
        }

    }

    /**
     * Gets the effective package name for the AST node files.
     * 
     * @return The effective package name for the AST node files.
     */
    private String getNodePackageName()
    {
        if ( this.packageName != null )
        {
            return this.packageName + ".syntaxtree";
        }
        else if ( this.nodePackageName == null )
        {
            return "syntaxtree";
        }
        else
        {
            return this.nodePackageName;
        }
    }

    /**
     * Gets the effective package name for the visitor files.
     * 
     * @return The effective package name for the visitor files.
     */
    private String getVisitorPackageName()
    {
        if ( this.packageName != null )
        {
            return this.packageName + ".visitor";
        }
        else if ( this.visitorPackageName == null )
        {
            return "visitor";
        }
        else
        {
            return this.visitorPackageName;
        }
    }

    /**
     * @param jtbFile The absolute path to the grammar file to pass into JTB for preprocessing, must not be
     *            <code>null</code>.
     * @param grammarDirectory The absolute path to the output directory for the generated grammar file and its AST node
     *            files, must not be <code>null</code>. If this directory does not exist yet, it is created. Note
     *            that this path should already include the desired package hierarchy because JTB will not append the
     *            required sub directories automatically.
     * @throws MojoExecutionException If JJTree could not be invoked.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    private void runJTB( File jtbFile, File grammarDirectory )
        throws MojoExecutionException, MojoFailureException
    {
        int exitCode;
        try
        {
            JTB jtb = new JTB();
            jtb.setInputFile( jtbFile );
            jtb.setOutputDirectory( grammarDirectory );
            jtb.setDescriptiveFieldNames( this.descriptiveFieldNames );
            jtb.setJavadocFriendlyComments( this.javadocFriendlyComments );
            jtb.setNodePackageName( this.nodePackageName );
            jtb.setNodeParentClass( this.nodeParentClass );
            jtb.setPackageName( this.packageName );
            jtb.setParentPointers( this.parentPointers );
            jtb.setPrinter( this.printer );
            jtb.setScheme( this.scheme );
            jtb.setSpecialTokens( this.specialTokens );
            jtb.setSupressErrorChecking( this.supressErrorChecking );
            jtb.setVisitorPackageName( this.visitorPackageName );
            getLog().debug( "Running JTB: " + jtb );
            exitCode = jtb.run();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to execute JTB", e );
        }
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "JTB reported exit code " + exitCode + ": " + jtbFile );
        }
    }

    /**
     * Moves the specified output package from JTB to the given target directory. JTB assumes that the current working
     * directory represents the parent package of the configured node/visitor package.
     * 
     * @param pkgName The qualified name of the package to move, must not be <code>null</code>.
     * @throws MojoExecutionException If the move failed.
     */
    private void movePackage( String pkgName )
        throws MojoExecutionException
    {
        String pkgPath = pkgName.replace( '.', File.separatorChar );
        String subDir = pkgPath.substring( pkgPath.lastIndexOf( File.separatorChar ) + 1 );
        File sourceDir = new File( this.workingDirectory, subDir );
        File targetDir = new File( this.outputDirectory, pkgPath );
        moveDirectory( sourceDir, targetDir );
    }

    /**
     * Moves all JTB output files from the specified source directory to the given target directory. Existing files in
     * the target directory will be overwritten. Note that this move assumes a flat source directory, i.e. copying of
     * sub directories is not supported.<br/><br/>This method must be used instead of
     * {@link java.io.File#renameTo(java.io.File)} which would fail if the target directory already existed (at least on
     * Windows).
     * 
     * @param sourceDir The absolute path to the source directory, must not be <code>null</code>.
     * @param targetDir The absolute path to the target directory, must not be <code>null</code>.
     * @throws MojoExecutionException If the move failed.
     */
    private void moveDirectory( File sourceDir, File targetDir )
        throws MojoExecutionException
    {
        getLog().debug( "Moving JTB output files: " + sourceDir + " -> " + targetDir );
        /*
         * NOTE: The source directory might be the current working directory if JTB was told to output into the default
         * package. The current working directory might be quite anything and will likely contain sub directories not
         * created by JTB. Therefore, we do a defensive move and only delete the expected Java source files.
         */
        File[] sourceFiles = sourceDir.listFiles();
        for ( int i = 0; i < sourceFiles.length; i++ )
        {
            File sourceFile = sourceFiles[i];
            if ( sourceFile.isFile() && sourceFile.getName().endsWith( ".java" ) )
            {
                try
                {
                    FileUtils.copyFileToDirectory( sourceFile, targetDir );
                    if ( !sourceFile.delete() )
                    {
                        getLog().error( "Failed to delete original JTB output file: " + sourceFile );
                    }
                }
                catch ( Exception e )
                {
                    throw new MojoExecutionException( "Failed to move JTB output file: " + sourceFile + " -> "
                        + targetDir );
                }
            }
        }
        if ( sourceDir.list().length <= 0 )
        {
            if ( !sourceDir.delete() )
            {
                getLog().error( "Failed to delete original JTB output directory: " + sourceDir );
            }
        }
        else
        {
            getLog().debug( "Keeping non empty JTB output directory: " + sourceDir );
        }
    }

    /**
     * @return A set of <code>File</code> objects to compile.
     * @throws MojoExecutionException If it fails.
     */
    private Set computeStaleGrammars()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".jtb", ".jtb" );
        SuffixMapping mappingCAP = new SuffixMapping( ".JTB", ".JTB" );

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

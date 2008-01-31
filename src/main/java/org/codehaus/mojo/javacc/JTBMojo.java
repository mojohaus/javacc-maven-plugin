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

import EDU.purdue.jtb.JTB;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
     * This option is short for <code>nodePackageName = package + syntaxtree</code> and
     * <code>visitorPackageName = package + visitor</code>.
     * 
     * @parameter expression=${package}"
     */
    private String packageName;

    /**
     * The path of the package name relate to <code>packageName</code>
     * 
     * @see #packageName
     */
    private String packagePath;

    /**
     * This option specifies the package for the generated AST nodes.
     * 
     * @parameter expression=${nodePackageName}"
     */
    private String nodePackageName;

    /**
     * The path of the package name relate to <code>nodePackageName</code>
     * 
     * @see #nodePackageName
     */
    private String nodePackagePath;

    /**
     * This option specifies the package for the generated visitors.
     * 
     * @parameter expression=${visitorPackageName}"
     */
    private String visitorPackageName;

    /**
     * The path of the package name relate to <code>visitorPackageName</code>
     * 
     * @see #visitorPackageName
     */
    private String visitorPackagePath;

    /**
     * If true, JTB will supress its semantic error checking.
     * 
     * @parameter expression=${supressErrorChecking}"
     */
    private Boolean supressErrorChecking;

    /**
     * If true, all generated comments will be wrapped in pre tags so that they are formatted correctly in javadocs.
     * 
     * @parameter expression=${javadocFriendlyComments}"
     */
    private Boolean javadocFriendlyComments;

    /**
     * Setting this option to true causes JTB to generate field names that reflect the structure of the tree.
     * 
     * @parameter expression=${descriptiveFieldNames}"
     */
    private Boolean descriptiveFieldNames;

    /**
     * All AST nodes will inherit from the class specified for this parameter.
     * 
     * @parameter expression=${nodeParentClass}"
     */
    private String nodeParentClass;

    /**
     * If true, all nodes will contain fields for its parent node.
     * 
     * @parameter expression=${parentPointers}"
     */
    private Boolean parentPointers;

    /**
     * If true, JTB will include JavaCC "special tokens" in the AST.
     * 
     * @parameter expression=${specialTokens}"
     */
    private Boolean specialTokens;

    /**
     * If true, JTB will generate:
     * <ul>
     * <li>Scheme records representing the grammar.</li>
     * <li>A Scheme tree building visitor.</li>
     * </ul>
     * 
     * @parameter expression=${scheme}"
     */
    private Boolean scheme;

    /**
     * If true, JTB will generate a syntax tree dumping visitor.
     * 
     * @parameter expression=${printer}"
     */
    private Boolean printer;

    /**
     * Directory where the JTB file(s) are located.
     * 
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/jtb"
     */
    private File sourceDirectory;

    /**
     * Directory where the output Java Files will be located.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/jtb"
     */
    private File outputDirectory;

    /**
     * the directory to store the resulting JavaCC grammar(s)
     * 
     * @parameter expression="${timestampDirectory}"
     *            default-value="${project.build.directory}/generated-sources/jtb-timestamp"
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation
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
     * Execute the JTB
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

        if ( packageName != null )
        {
            packagePath = StringUtils.replace( packageName, '.', File.separatorChar );

            getLog().debug( "Using packagePath: " + packagePath );
        }
        else
        {
            if ( visitorPackageName != null )
            {
                visitorPackagePath = StringUtils.replace( visitorPackageName, '.', File.separatorChar );

                getLog().debug( "Using visitorPackagePath: " + visitorPackagePath );
            }

            if ( nodePackageName != null )
            {
                nodePackagePath = StringUtils.replace( nodePackageName, '.', File.separatorChar );

                getLog().debug( "Using nodePackagePath: " + nodePackagePath );
            }
        }
        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
        }
        if ( !timestampDirectory.exists() )
        {
            timestampDirectory.mkdirs();
        }

        Set staleGrammars = computeStaleGrammars();

        if ( staleGrammars.isEmpty() )
        {
            getLog().info( "Nothing to process - all grammars are up to date" );
            if ( project != null )
            {
                project.addCompileSourceRoot( outputDirectory.getPath() );
            }
            return;
        }

        for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
        {
            File jtbFile = (File) i.next();
            String nodePackage = getNodePackageName();
            String visitorPackage = getVisitorPackageName();
            try
            {
                JTB.main( generateArgumentList( jtbFile ) );

                /*
                 * since jtb was meant to be run as a command-line tool, it only outputs to the current directory.
                 * therefore, the files must be moved to the correct locations.
                 */
                movePackage( nodePackage );
                movePackage( visitorPackage );

                FileUtils.copyFileToDirectory( jtbFile, timestampDirectory );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "JTB execution failed", e );
            }
        }
        if ( project != null )
        {
            project.addCompileSourceRoot( outputDirectory.getPath() );
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
     * @param jtbFile The path of the file to compile.
     * @return A string array that represents the arguments to use for JTB.
     */
    private String[] generateArgumentList( File jtbFile )
    {
        List argsList = new ArrayList();

        argsList.add( "-o" );
        argsList.add( outputDirectory + File.separator + FileUtils.basename( jtbFile.getName() ) + "jj" );
        if ( packageName != null )
        {
            argsList.add( "-p" );
            argsList.add( packageName );
        }
        else
        {
            if ( nodePackageName != null )
            {
                argsList.add( "-np" );
                argsList.add( nodePackageName );
            }
            if ( visitorPackageName != null )
            {
                argsList.add( "-vp" );
                argsList.add( visitorPackageName );
            }
        }
        if ( ( supressErrorChecking != null ) && supressErrorChecking.booleanValue() )
        {
            argsList.add( "-e" );
        }
        if ( ( javadocFriendlyComments != null ) && javadocFriendlyComments.booleanValue() )
        {
            argsList.add( "-jd" );
        }
        if ( ( descriptiveFieldNames != null ) && descriptiveFieldNames.booleanValue() )
        {
            argsList.add( "-f" );
        }
        if ( nodeParentClass != null )
        {
            argsList.add( "-ns" );
            argsList.add( nodeParentClass );
        }
        if ( ( parentPointers != null ) && parentPointers.booleanValue() )
        {
            argsList.add( "-pp" );
        }
        if ( ( specialTokens != null ) && specialTokens.booleanValue() )
        {
            argsList.add( "-tk" );
        }
        if ( ( scheme != null ) && scheme.booleanValue() )
        {
            argsList.add( "-scheme" );
        }
        if ( ( printer != null ) && printer.booleanValue() )
        {
            argsList.add( "-printer" );
        }
        argsList.add( jtbFile.getAbsolutePath() );

        getLog().debug( "Using arguments: " + argsList );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * @return the <code>Set</code> contains a <code>String</code>tha rappresent the files to compile
     * @throws MojoExecutionException if it fails
     */
    private Set computeStaleGrammars()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".jtb", ".jtb" );
        SuffixMapping mappingCAP = new SuffixMapping( ".JTB", ".JTB" );

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis );

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

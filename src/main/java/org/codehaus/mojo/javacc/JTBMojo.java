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
import java.util.Set;

/**
 * Parses a JTB file and transforms it into source files for an AST and a JavaCC grammar file which automatically
 * builds the AST.  <b>Note: <a href="http://compilers.cs.ucla.edu/jtb/">JTB</a> requires JDK 1.5 and higher.  This mojo will not work with earlier versions of the
 * JDK</b>.
 * 
 * @goal jtb
 * @phase generate-sources
 * @author Gregory Kick (gk5885@kickstyle.net)
 */
public class JTBMojo extends AbstractMojo
{
    /**
     * This option is short for
     * <code>nodePackageName = package + syntaxtree</code> and
     * <code>visitorPackageName = package + visitor</code>.
     * 
     * @parameter expression=${package}"
     */
    private String packageName;

    /**
     * The path of the package name relate to <code>packageName</code> 
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
     * If true, all generated comments will be wrapped in pre tags so that they
     * are formatted correctly in javadocs.
     * 
     * @parameter expression=${javadocFriendlyComments}"
     */
    private Boolean javadocFriendlyComments;

    /**
     * Setting this option to true causes JTB to generate field names that
     * reflect the structure of the tree.
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
     * @parameter expression="${basedir}/src/main/jtb"
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory where the output Java Files will be located.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/jtb"
     * @required
     */
    private File outputDirectory;

    /**
     * the directory to store the resulting JavaCC grammar(s)
     * 
     * @parameter expression="${basedir}/target"
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing
     * whether a source needs recompilation
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
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File baseDir;

    /**
     * Execute the JTB
     * 
     * @throws MojoExecutionException if the compilation fails
     */
    public void execute() throws MojoExecutionException
    {
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
            try
            {
                JTB.main( generateJTBArgumentList( jtbFile.getAbsolutePath() ) );

                /*
                 * since jtb was meant to be run as a command-line tool, it only
                 * outputs to the current directory. therefore, the files must
                 * be moved to the correct locations.
                 */
                File tempDir;
                File newDir;
                if ( packagePath != null )
                {
                    tempDir = new File( baseDir, "syntaxtree" );
                    newDir = new File( outputDirectory + File.separator + packagePath + File.separator + "syntaxtree" );
                    newDir.mkdirs();

                    getLog().debug( "Moving " + tempDir + " to " + newDir );
                    tempDir.renameTo( newDir );

                    tempDir = new File( baseDir, "visitor" );
                    newDir = new File( outputDirectory + File.separator + packagePath + File.separator + "visitor" );
                    newDir.mkdirs();

                    getLog().debug( "Moving " + tempDir + " to " + newDir );
                    tempDir.renameTo( newDir );
                }
                else
                {
                    if ( nodePackagePath != null )
                    {
                        tempDir = new File( baseDir, nodePackagePath.substring( nodePackagePath
                                .lastIndexOf( File.separator ) ) );
                        newDir = new File( outputDirectory + File.separator + nodePackagePath );
                        newDir.mkdirs();

                        getLog().debug( "Moving " + tempDir + " to " + newDir );
                        tempDir.renameTo( newDir );
                    }
                    else
                    {
                        tempDir = new File( baseDir, "syntaxtree" );
                        newDir = new File( outputDirectory + File.separator + "syntaxtree" );
                        newDir.mkdirs();

                        getLog().debug( "Moving " + tempDir + " to " + newDir );
                        tempDir.renameTo( newDir );
                    }

                    if ( visitorPackagePath != null )
                    {
                        tempDir = new File( baseDir, visitorPackagePath.substring( visitorPackagePath
                                .lastIndexOf( File.separator ) ) );
                        newDir = new File( outputDirectory + File.separator + visitorPackagePath );
                        newDir.mkdirs();

                        getLog().debug( "Moving " + tempDir + " to " + newDir );
                        tempDir.renameTo( newDir );
                    }
                    else
                    {
                        tempDir = new File( baseDir, "visitor" );
                        newDir = new File( outputDirectory + File.separator + "visitor" );
                        newDir.mkdirs();

                        getLog().debug( "Moving " + tempDir + " to " + newDir );
                        tempDir.renameTo( newDir );
                    }
                }

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
     * @param jtbFileName a <code>String</code> which rappresent the path of the file to compile
     * @return a <code>String[]</code> that represent the argument to use for JTB
     */
    private String[] generateJTBArgumentList( String jtbFileName )
    {

        ArrayList argsList = new ArrayList();

        argsList.add( "-o" );
        argsList.add( outputDirectory + File.separator + FileUtils.basename( jtbFileName ) + "jj" );
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
        argsList.add( jtbFileName );

        getLog().debug( "Using arguments: " + argsList );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * @return the <code>Set</code> contains a <code>String</code>tha rappresent the files to compile
     * @throws MojoExecutionException if it fails
     */
    private Set computeStaleGrammars() throws MojoExecutionException
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

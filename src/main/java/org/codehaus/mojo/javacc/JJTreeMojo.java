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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.javacc.jjtree.JJTree;

/**
 * @goal jjtree
 * @phase generate-sources
 * @description Goal which parses a JJT file and transforms it to Java Source
 *              Files.
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JJTreeMojo extends AbstractMojo
{

    /**
     * @parameter expression="${buildNodeFiles}"
     */
    private Boolean buildNodeFiles;

    /**
     * @parameter expression="${multi}"
     */
    private Boolean multi;

    /**
     * @parameter expression="${nodeDefaultVoid}"
     */
    private Boolean nodeDefaultVoid;

    /**
     * @parameter expression="${nodeFactory}"
     */
    private Boolean nodeFactory;

    /**
     * @parameter expression="${nodeScopeHook}"
     */
    private Boolean nodeScopeHook;

    /**
     * @parameter expression="${nodeUsesParser}"
     */
    private Boolean nodeUsesParser;

    /**
     * @parameter expression="${staticOption}"
     */
    private Boolean staticOption;

    /**
     * @parameter expression="${visitor}"
     */
    private Boolean visitor;

    /**
     * @parameter expression="${nodePackage}"
     */
    private String nodePackage;

    /**
     * @parameter expression="${visitorException}"
     */
    private String visitorException;

    /**
     * @parameter expression="${nodePrefix}"
     */
    private String nodePrefix;

    /**
     * Directory where the JJT file(s) are located.
     * 
     * @parameter expression="${basedir}/src/main/jjtree"
     * @required
     */
    private String sourceDirectory;

    /**
     * Directory where the output Java Files will be located.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/jjtree"
     * @required
     */
    private String outputDirectory;

    /**
     * the directory to store the processed .jjt files
     * 
     * @parameter expression="${project.build.directory}/generated-sources/jjtree-timestamp"
     */
    private String timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing
     * whether a source needs recompilation
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * A list of inclusion filters for the compiler.
     * @parameter
     */
    private Set includes;
        
    /**
     * A list of exclusion filters for the compiler.
     * @parameter
     */
    private Set excludes;
    
    /**
     * Contains the package name to use for the generated code 
     */
    private String packageName;

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * Execute the JJTree
     * 
     * @throws MojoExecutionException if the compilation fails
     */
    public void execute() throws MojoExecutionException
    {
        if ( nodePackage != null )
        {
            packageName = StringUtils.replace( nodePackage, '.', File.separatorChar );
        }

        if ( !FileUtils.fileExists( outputDirectory ) )
        {
            FileUtils.mkdir( outputDirectory );
        }

        if ( !FileUtils.fileExists( timestampDirectory ) )
        {
            FileUtils.mkdir( timestampDirectory );
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
            if ( project != null )
            {
                project.addCompileSourceRoot( outputDirectory );
            }
            return;
        }

        for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
        {
            File jjTreeFile = (File) i.next();
            try
            {
                JJTree jjtree = new JJTree();
                jjtree.main( generateArgumentList( jjTreeFile.getAbsolutePath() ) );

                FileUtils.copyFileToDirectory( jjTreeFile, new File( timestampDirectory ) );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "JJTree execution failed", e );
            }
        }

        if ( project != null )
        {
            project.addCompileSourceRoot( outputDirectory );
        }
    }

    /**
     * @return the directory that will conatin the generated code
     */
    private String getOutputDirectory( String jjtreeInput ) throws MojoExecutionException
    {
        if ( packageName != null )
        {
            return outputDirectory + File.separator + packageName;
        }
        else 
        {
            String declaredPackage = JavaCCUtil.getDeclaredPackage(jjtreeInput);
            
            if (declaredPackage != null)
            {
               return outputDirectory + File.separator + declaredPackage;
            }
        }    
        return outputDirectory;
    }

    /**
     * @param jjTreeFilename a <code>String</code> which rappresent the path of the file to compile
     * @return a <code>String[]</code> that represent the argument to use for JJTree
     */
    private String[] generateArgumentList( String jjTreeFilename ) throws MojoExecutionException
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

        if ( staticOption != null )
        {
            argsList.add( "-STATIC=" + staticOption );
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
     * @return the <code>Set</code> contains a <code>String</code>tha rappresent the files to compile
     * @throws MojoExecutionException if it fails
     */
    private Set computeStaleGrammars() throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".jjt", ".jjt" );
        SuffixMapping mappingCAP = new SuffixMapping( ".JJT", ".JJT" );

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis, includes, excludes );

        scanner.addSourceMapping( mapping );
        scanner.addSourceMapping( mappingCAP );

        File outDir = new File( timestampDirectory );

        Set staleSources = new HashSet();

        File sourceDir = new File( sourceDirectory );

        try
        {
            staleSources.addAll( scanner.getIncludedSources( sourceDir, outDir ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir
                    + "\' for stale grammars to reprocess.", e );
        }

        return staleSources;
    }

}

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
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     * This option specifies the package for the generated AST nodes. This value may use a leading asterisk to reference
     * the package of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this
     * parameter is set to <code>*.demo</code>, the tree node classes will be located in the package
     * <code>org.apache.demo</code>. Default value is <code>*.syntaxtree</code>.
     * 
     * @parameter expression="${nodePackageName}"
     */
    private String nodePackageName;

    /**
     * This option specifies the package for the generated visitors. This value may use a leading asterisk to reference
     * the package of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this
     * parameter is set to <code>*.demo</code>, the visitor classes will be located in the package
     * <code>org.apache.demo</code>. Default value is <code>*.visitor</code>.
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
     * Execute the JTB preprocessor.
     * 
     * @throws MojoExecutionException If the invocation of JTB failed.
     * @throws MojoFailureException If JTB reported a non-zero exit code.
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

        if ( this.project != null )
        {
            getLog().debug( "Adding compile source root: " + this.outputDirectory );
            this.project.addCompileSourceRoot( this.outputDirectory.getPath() );
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

            scanner.addSourceMapping( new SuffixMapping( ".jtb", ".jtb" ) );
            scanner.addSourceMapping( new SuffixMapping( ".JTB", ".JTB" ) );

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
     * Passes the specified grammar file through JTB.
     * 
     * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>null</code>.
     * @throws MojoExecutionException If the invocation of JTB failed.
     * @throws MojoFailureException If JTB reported a non-zero exit code.
     */
    private void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jtbFile = grammarInfo.getGrammarFile();
        File jjDirectory = new File( this.outputDirectory, grammarInfo.getPackageDirectory().getPath() );

        String nodePackage = grammarInfo.resolvePackageName( getNodePackageName() );
        String visitorPackage = grammarInfo.resolvePackageName( getVisitorPackageName() );

        // generate final grammar file and the node/visitor files
        runJTB( jtbFile, jjDirectory, nodePackage, visitorPackage );

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

    /**
     * Gets the effective package name for the AST node files.
     * 
     * @return The effective package name for the AST node files, never <code>null</code>.
     */
    private String getNodePackageName()
    {
        if ( this.packageName != null )
        {
            return this.packageName + ".syntaxtree";
        }
        else if ( this.nodePackageName != null )
        {
            return this.nodePackageName;
        }
        else
        {
            return "*.syntaxtree";
        }
    }

    /**
     * Gets the effective package name for the visitor files.
     * 
     * @return The effective package name for the visitor files, never <code>null</code>.
     */
    private String getVisitorPackageName()
    {
        if ( this.packageName != null )
        {
            return this.packageName + ".visitor";
        }
        else if ( this.visitorPackageName != null )
        {
            return this.visitorPackageName;
        }
        else
        {
            return "*.visitor";
        }
    }

    /**
     * Runs JTB on the specified grammar file to generate a annotated grammar file. The options for JTB are derived from
     * the current values of the corresponding mojo parameters.
     * 
     * @param jtbFile The absolute path to the grammar file to pass into JTB for preprocessing, must not be
     *            <code>null</code>.
     * @param grammarDirectory The absolute path to the output directory for the generated grammar file and its AST node
     *            files, must not be <code>null</code>. If this directory does not exist yet, it is created. Note
     *            that this path should already include the desired package hierarchy because JTB will not append the
     *            required sub directories automatically.
     * @param nodePackage The qualified name of the package for the AST nodes, must not be <code>null</code>.
     * @param visitorPackage The qualified name of the package for the visitor files, must not be <code>null</code>.
     * @throws MojoExecutionException If JJTree could not be invoked.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    private void runJTB( File jtbFile, File grammarDirectory, String nodePackage, String visitorPackage )
        throws MojoExecutionException, MojoFailureException
    {
        File nodeDirectory = new File( this.outputDirectory, nodePackage.replace( '.', File.separatorChar ) );
        File visitorDirectory = new File( this.outputDirectory, visitorPackage.replace( '.', File.separatorChar ) );

        JTB jtb = new JTB();
        jtb.setInputFile( jtbFile );
        jtb.setOutputDirectory( grammarDirectory );
        jtb.setNodeDirectory( nodeDirectory );
        jtb.setVisitorDirectory( visitorDirectory );
        jtb.setDescriptiveFieldNames( this.descriptiveFieldNames );
        jtb.setJavadocFriendlyComments( this.javadocFriendlyComments );
        jtb.setNodePackageName( nodePackage );
        jtb.setNodeParentClass( this.nodeParentClass );
        jtb.setParentPointers( this.parentPointers );
        jtb.setPrinter( this.printer );
        jtb.setScheme( this.scheme );
        jtb.setSpecialTokens( this.specialTokens );
        jtb.setSupressErrorChecking( this.supressErrorChecking );
        jtb.setVisitorPackageName( visitorPackage );
        jtb.setLog( getLog() );
        jtb.run();
    }

}

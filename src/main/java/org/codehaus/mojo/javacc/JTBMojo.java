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
 * Parses a JTB file and transforms it into source files for an AST and a JavaCC grammar file which automatically builds
 * the AST.<strong>Note:</strong> <a href="http://compilers.cs.ucla.edu/jtb/">JTB</a> requires Java 1.5
 * or higher. This goal will not work with earlier versions of the JRE.
 * 
 * @since 2.2
 * @deprecated As of version 2.4, use the <code>jtb-javacc</code> goal instead.
 * @author Gregory Kick (gk5885@kickstyle.net)
 *
 */
@Mojo(name = "jtb", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class JTBMojo
    extends AbstractPreprocessorMojo
{

    /**
     * This option is short for <code>nodePackageName</code> = <code>&lt;packageName&gt;.syntaxtree</code> and
     * <code>visitorPackageName</code> = <code>&lt;packageName&gt;.visitor</code>. Note that this option takes
     * precedence over <code>nodePackageName</code> and <code>visitorPackageName</code> if specified.
     *
     */
    @Parameter(property = "javacc.packageName")
    private String packageName;

    /**
     * This option specifies the package for the generated AST nodes. This value may use a leading asterisk to reference
     * the package of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this
     * parameter is set to <code>*.demo</code>, the tree node classes will be located in the package
     * <code>org.apache.demo</code>. Default value is <code>*.syntaxtree</code>.
     *
     */
    @Parameter(property = "javacc.nodePackageName")
    private String nodePackageName;

    /**
     * This option specifies the package for the generated visitors. This value may use a leading asterisk to reference
     * the package of the corresponding parser. For example, if the parser package is <code>org.apache</code> and this
     * parameter is set to <code>*.demo</code>, the visitor classes will be located in the package
     * <code>org.apache.demo</code>. Default value is <code>*.visitor</code>.
     *
     */
    @Parameter(property = "javacc.visitorPackageName")
    private String visitorPackageName;

    /**
     * If <code>true</code>, JTB will suppress its semantic error checking. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.supressErrorChecking", defaultValue = "false")
    private boolean supressErrorChecking;

    /**
     * If <code>true</code>, all generated comments will be wrapped in <code>&lt;pre&gt;</code> tags so that they
     * are formatted correctly in API docs. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.javadocFriendlyComments", defaultValue = "false")
    private Boolean javadocFriendlyComments;

    /**
     * Setting this option to <code>true</code> causes JTB to generate field names that reflect the structure of the
     * tree instead of generic names like <code>f0</code>, <code>f1</code> etc. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.descriptiveFieldNames", defaultValue = "false")
    private Boolean descriptiveFieldNames;

    /**
     * The qualified name of a user-defined class from which all AST nodes will inherit. By default, AST nodes will
     * inherit from the generated class <code>Node</code>.
     *
     */
    @Parameter(property = "javacc.nodeParentClass")
    private String nodeParentClass;

    /**
     * If <code>true</code>, all nodes will contain fields for its parent node. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.parentPointers", defaultValue = "false")
    private boolean parentPointers;

    /**
     * If <code>true</code>, JTB will include JavaCC "special tokens" in the AST. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.specialTokens", defaultValue = "false")
    private boolean specialTokens;

    /**
     * If <code>true</code>, JTB will generate the following files to support the Schema programming language:
     * <ul>
     * <li>Scheme records representing the grammar.</li>
     * <li>A Scheme tree building visitor.</li>
     * </ul>
     * Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.scheme", defaultValue = "false")
    private boolean scheme;

    /**
     * If <code>true</code>, JTB will generate a syntax tree dumping visitor. Default value is <code>false</code>.
     *
     */
    @Parameter(property = "javacc.printer", defaultValue = "false")
    private Boolean printer;

    /**
     * The directory where the JavaCC grammar files (<code>*.jtb</code>) are located. It will be recursively scanned
     * for input files to pass to JTB.
     *
     */
    @Parameter(property = "javacc.sourceDirectory", defaultValue = "${basedir}/src/main/jtb")
    private File sourceDirectory;

    /**
     * The directory where the output Java files will be located.
     *
     */
    @Parameter(property = "javacc.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/jtb")
    private File outputDirectory;

    /**
     * The directory to store the processed input files for later detection of stale sources.
     *
     */
    @Parameter(property = "javacc.timestampDirectory", defaultValue = "${project.build.directory}/generated-sources/jtb-timestamp")
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     *
     */
    @Parameter(property = "javacc.lastModGranularityMs", defaultValue = "0")
    private int staleMillis;

    /**
     * A set of Ant-like inclusion patterns used to select files from the source directory for processing. By default,
     * the patterns <code>**&#47;*.jtb</code> and <code>**&#47;*.JTB</code> are used to select grammar files.
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
            return new String[] { "**/*.jtb", "**/*.JTB" };
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
    protected File getTimestampDirectory()
    {
        return this.timestampDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected int getStaleMillis()
    {
        return this.staleMillis;
    }

    /**
     * {@inheritDoc}
     */
    protected void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jtbFile = grammarInfo.getGrammarFile();
        File jjDirectory = new File( getOutputDirectory(), grammarInfo.getParserDirectory() );

        String nodePackage = grammarInfo.resolvePackageName( getNodePackageName() );
        File nodeDirectory = new File( getOutputDirectory(), nodePackage.replace( '.', File.separatorChar ) );

        String visitorPackage = grammarInfo.resolvePackageName( getVisitorPackageName() );
        File visitorDirectory = new File( getOutputDirectory(), visitorPackage.replace( '.', File.separatorChar ) );

        // generate final grammar file and the node/visitor files
        JTB jtb = newJTB();
        jtb.setInputFile( jtbFile );
        jtb.setOutputDirectory( jjDirectory );
        jtb.setNodeDirectory( nodeDirectory );
        jtb.setVisitorDirectory( visitorDirectory );
        jtb.setNodePackageName( nodePackage );
        jtb.setVisitorPackageName( visitorPackage );
        jtb.run();

        // create timestamp file
        createTimestamp( grammarInfo );
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
     * Creates a new facade to invoke JTB. Most options for the invocation are derived from the current values of the
     * corresponding mojo parameters. The caller is responsible to set the input file, output directories and packages
     * on the returned facade.
     * 
     * @return The facade for the tool invocation, never <code>null</code>.
     */
    private JTB newJTB()
    {
        JTB jtb = new JTB();
        jtb.setLog( getLog() );
        jtb.setDescriptiveFieldNames( this.descriptiveFieldNames );
        jtb.setJavadocFriendlyComments( this.javadocFriendlyComments );
        jtb.setNodeParentClass( this.nodeParentClass );
        jtb.setParentPointers( this.parentPointers );
        jtb.setPrinter( this.printer );
        jtb.setScheme( this.scheme );
        jtb.setSpecialTokens( this.specialTokens );
        jtb.setSupressErrorChecking( this.supressErrorChecking );
        return jtb;
    }

}

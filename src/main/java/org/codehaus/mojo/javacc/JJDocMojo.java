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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * JJDoc takes a JavaCC [tm] parser specification and produces documentation for the BNF grammar. <a
 * href="https://javacc.dev.java.net/doc/JJDoc.html">JJDoc Documentation</a>.
 * 
 * @author <a href="mailto:pgier@redhat.com">Paul Gier</a>
 * @version $Id$
 * @goal jjdoc
 * @phase generate-sources
 * @see <a href="https://javacc.dev.java.net/doc/JJDoc.html">JJDoc Documentation</a>
 */
public class JJDocMojo
    extends AbstractMavenReport
{

    /**
     * The jjdoc classname that is used to call jjdoc from the command line.
     */
    public static final String JJDOC_CLASSNAME = "jjdoc";

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * Maven Project
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Generates the site report
     * 
     * @component
     */
    private Renderer siteRenderer;

    /**
     * Specifies the destination directory where javadoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    private File reportOutputDirectory;

    /**
     * The name of the destination directory. This will be a subdirectory of the report output directory.
     * 
     * @parameter expression="${destDir}" default-value="jjdoc"
     */
    private String destDir;

    /**
     * The name of the JJDoc report.
     * 
     * @parameter expression="${name}" default-value="JJDoc"
     */
    private String name;

    /**
     * The description of the JJDoc report.
     * 
     * @parameter expression="${description}" default-value="Javacc grammar documentation."
     */
    private String description;

    /**
     * The plugin dependencies.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List pluginArtifacts;

    /**
     * Directory where the JJ file(s) are located.
     * 
     * @parameter expression="${basedir}/src/main/javacc"
     * @required
     */
    private File sourceDirectory;

    /**
     * Specifies the destination directory where jjdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/jjdoc"
     * @required
     */
    protected File outputDirectory;

    /**
     * Setting TEXT to true causes JJDoc to generate a plain text format description of the BNF. Some formatting is done
     * via tab characters, but the intention is to leave it as plain as possible. The default value of TEXT causes JJDoc
     * to generate a hyperlinked HTML document.
     * 
     * @parameter default-value=false
     */
    private boolean text;

    /**
     * The default value of ONE_TABLE is used to generate a single HTML table for the BNF. Setting it to false will
     * produce one table for every production in the grammar.
     * 
     * @parameter default-value=true
     */
    private boolean oneTable;

    /**
     * This option allows you to specify a CSS file name. If you supply a file name in this option it will appear in a
     * LINK element in the HEAD section of the file. This option only applies to HTML output.
     * 
     * @parameter
     */
    private String css;

    /**
     * Get the maven project.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Get the site renderer.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Get the output directory of the report.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory()
    {
        return reportOutputDirectory.getAbsolutePath();
    }

    // ----------------------------------------------------------------------
    // public methods
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        if ( StringUtils.isEmpty( name ) )
        {
            return "JJDoc";
        }

        return name;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        if ( StringUtils.isEmpty( description ) )
        {
            return "JJDoc documentation.";
        }

        return description;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return destDir + "/index";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     */
    public boolean isExternalReport()
    {
        return false;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     */
    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getCategoryName()
     */
    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getReportOutputDirectory()
     */
    public File getReportOutputDirectory()
    {
        if ( reportOutputDirectory == null )
        {
            return outputDirectory;
        }

        return reportOutputDirectory;
    }

    /**
     * Method to set the directory where the generated reports will be put
     * 
     * @param reportOutputDirectory the directory file to be set
     */
    public void setReportOutputDirectory( File reportOutputDirectory )
    {
        if ( ( reportOutputDirectory != null ) && ( !reportOutputDirectory.getAbsolutePath().endsWith( destDir ) ) )
        {
            this.reportOutputDirectory = new File( reportOutputDirectory, destDir );
        }
        else
        {
            this.reportOutputDirectory = reportOutputDirectory;
        }
    }

    /**
     * Run the actual report.
     * 
     * @param locale The locale of the user.
     */
    public void executeReport( Locale locale )
    {
        Sink sink = getSink();

        createReportHeader( sink );

        try
        {
            Set grammarFiles = scanForGrammarFiles();

            for ( Iterator i = grammarFiles.iterator(); i.hasNext(); )
            {
                File grammarFile = (File) i.next();

                String relativeOutputFilePath =
                    grammarFile.getAbsolutePath().replace( sourceDirectory.getAbsolutePath(), "" );
                relativeOutputFilePath = relativeOutputFilePath.replace( ".jj", getOutputFileExtension() );
                relativeOutputFilePath = relativeOutputFilePath.replace( ".JJ", getOutputFileExtension() );

                File jjdocOutputFile = new File( getReportOutputDirectory(), relativeOutputFilePath );
                jjdocOutputFile.getParentFile().mkdirs();

                String[] jjdocArgs = generateArgs( grammarFile, jjdocOutputFile );
                forkJJDoc( jjdocArgs );

                this.createReportLink( sink, grammarFile, relativeOutputFilePath );
            }
        }
        catch ( MojoExecutionException e )
        {
            e.printStackTrace();
        }

        createReportFooter( sink );
        sink.flush();
        sink.close();

    }
    
    /**
     * The jjdoc output file will have a .html or .txt extension depending on the value of the "text" parameter.
     * @return
     */
    public String getOutputFileExtension()
    {
        if (text)
        {
            return ".txt";
        }
        else
        {
            return ".html";
        }
    }

    /**
     * Create the header and title for the html report page.
     * 
     * @param sink
     */
    public void createReportHeader( Sink sink )
    {
        sink.head();
        sink.head_();

        sink.body();

        sink.section1();
        sink.rawText( "<h2>JJDoc Reports</h2>" );
        sink.text( "This page provides a list of the jjdoc reports that were generated for the javacc grammar files." );
        sink.section1_();

        sink.lineBreak();
        sink.section2();
        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( "Grammar File" );
        sink.tableHeaderCell_();
        sink.tableRow_();

    }

    /**
     * Create a table row containing a link to the jjdoc report for a grammar file.
     * 
     * @param sink The sink to write the report
     * @param grammarFile The javacc grammar file.
     * @param linkPath The path to the jjdoc output.
     */
    public void createReportLink( Sink sink, File grammarFile, String linkPath )
    {
        sink.tableRow();
        sink.tableCell();
        if ( linkPath.startsWith( "/" ) )
        {
            linkPath = linkPath.substring( 1 );
        }
        sink.link( linkPath );
        String grammarFileRelativePath = grammarFile.getAbsolutePath().replace( sourceDirectory.getAbsolutePath(), "" );
        if ( grammarFileRelativePath.startsWith( "/" ) )
        {
            grammarFileRelativePath = grammarFileRelativePath.substring( 1 );
        }
        sink.text( grammarFileRelativePath );
        sink.link_();
        sink.tableCell_();
        sink.tableRow_();
    }

    public void createReportFooter( Sink sink )
    {
        sink.table_();
        sink.section2_();
        sink.body_();
    }

    /**
     * Generate the command line arguments for calling javacc.
     * 
     * @param javaccFile The grammar file to be documented
     * @param outputFile The path to the report output
     * @return An array of the parameters.
     */
    public String[] generateArgs( File javaccFile, File outputFile )
    {
        ArrayList argsList = new ArrayList();

        argsList.add( "-OUTPUT_FILE=" + outputFile );

        if ( this.text )
        {
            argsList.add( "-TEXT=true" );
        }
        if ( !this.oneTable )
        {
            argsList.add( "-ONE_TABLE=false" );
        }
        if ( ( this.css != null ) && ( !this.css.equals( "" ) ) )
        {
            argsList.add( "-CSS=" + css );
        }
        argsList.add( javaccFile.getPath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * Searches the source directory to find grammar files that can be documented.
     * 
     * @return A set of the javacc grammar files.
     */
    public Set scanForGrammarFiles()
        throws MojoExecutionException
    {

        SuffixMapping mapping = new SuffixMapping( ".jj", getOutputFileExtension() );
        SuffixMapping mappingCAP = new SuffixMapping( ".JJ", getOutputFileExtension() );

        Set includes = Collections.singleton( "**/*" );
        Set excludes = Collections.EMPTY_SET;
        SourceInclusionScanner scanner = new SimpleSourceInclusionScanner( includes, excludes );

        scanner.addSourceMapping( mapping );
        scanner.addSourceMapping( mappingCAP );

        Set grammarFiles = new HashSet();

        try
        {
            grammarFiles.addAll( scanner.getIncludedSources( sourceDirectory, outputDirectory ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDirectory +
                "\' for stale grammars to reprocess.", e );
        }

        return grammarFiles;

    }

    /**
     * Create a classpath that contains the javacc jar file with jjdoc
     * 
     * @return The classpath string.
     */
    public String createJJDocClasspath()
    {
        StringBuffer classpath = new StringBuffer();

        for ( Iterator i = pluginArtifacts.iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();
            if ( artifact.getArtifactId().contains( "javacc" ) )
            {
                try
                {
                    classpath.append( artifact.getFile().getCanonicalPath() );
                    classpath.append( File.pathSeparatorChar );
                }
                catch ( IOException e )
                {
                    getLog().warn( "Unable to get path to artifact: " + artifact.getFile(), e );
                }
            }
        }
        return classpath.toString();
    }

    /**
     * Runs jjdoc in a forked jvm. This must be done because of the calls to System.exit in jjdoc.
     * 
     * @param jjdocArgs
     * @throws MojoExecutionException
     */
    public void forkJJDoc( String[] jjdocArgs )
        throws MojoExecutionException
    {
        Commandline cli = new Commandline();

        // use the same JVM as the one used to run Maven (the "java.home" one)
        String jvm = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + "java";
        cli.setExecutable( jvm );

        String[] jvmArgs = new String[3];
        jvmArgs[0] = "-cp";
        jvmArgs[1] = createJJDocClasspath();
        jvmArgs[2] = JJDOC_CLASSNAME;

        cli.addArguments( jvmArgs );

        cli.addArguments( jjdocArgs );

        StreamConsumer out = new MojoLogStreamConsumer();
        StreamConsumer err = new MojoLogStreamConsumer();

        getLog().debug( "Forking Command Line: " );
        getLog().debug( cli.toString() );
        getLog().debug( "" );

        try
        {
            int returnCode = CommandLineUtils.executeCommandLine( cli, out, err );
            if ( returnCode != 0 )
            {
                throw new MojoExecutionException( "There were errors while generating the jjdoc" );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Error while executing forked tests.", e );
        }

    }

    /**
     * Consume and log command output from the jjdoc process
     */
    public class MojoLogStreamConsumer
        implements StreamConsumer
    {
        public void consumeLine( String line )
        {
            getLog().info( line );
        }
    }

}

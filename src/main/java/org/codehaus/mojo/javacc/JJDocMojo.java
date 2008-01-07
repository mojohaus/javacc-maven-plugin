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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
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
 * href="https://javacc.dev.java.net/doc/JJDoc.html">JJDoc Documentation</a>.  This mojo will search
 * the source directory for all .jj files and run jjdoc once for each file it finds.  Each of these 
 * output files, along with an index.html file will be placed in the site directory (target/site/jjdoc),
 * and a link will be created in the "Project Reports" menu of the generated site.
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
    private List pluginArtifacts;

    /**
     * Directory where the JJ file(s) are located.
     * 
     * @parameter expression="${basedir}/src/main/javacc"
     */
    private File sourceDirectory;


    /**
     * The relative path of the jjdoc reports in the output directory.
     * This path will be appended to the output directory.
     * 
     * @parameter default-value="jjdoc";
     */
    private String jjdocDirectory;
    
    /**
     * Specifies the destination directory where jjdoc saves the generated HTML or Text files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
     */
    private File outputDirectory;

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
     * @return The current maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Get the site renderer.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     * @return The site renderer
     */
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Get the output directory of the report.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     * @return the report output directory.
     */
    protected String getOutputDirectory()
    {
        if ( ! outputDirectory.toString().endsWith( jjdocDirectory ) )
        {
            outputDirectory = new File( outputDirectory, jjdocDirectory );
        }
        return outputDirectory.toString();
    }

    // ----------------------------------------------------------------------
    // public methods
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     * @param locale The locale
     * @return The name of this report
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
     * @param locale The locale to use
     * @return The description of the report
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
     * @return The name of the main report file.
     */
    public String getOutputName()
    {
        return jjdocDirectory + "/index";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     * @return Determines if the report is using a sink. This always returns false.
     */
    public boolean isExternalReport()
    {
        return false;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     * @return Always returns true because this mojo can generate a report.
     */
    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getCategoryName()
     * @return The category where this report is located.
     */
    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    /**
     * Run the actual report.
     * 
     * @param locale The locale of the user.
     */
    public void executeReport( Locale locale )
    {
        Sink sink = getSink();

        createReportHeader( getBundle( locale ), sink );

        try
        {
            Set grammarFiles = scanForGrammarFiles();

            for ( Iterator i = grammarFiles.iterator(); i.hasNext(); )
            {
                File grammarFile = (File) i.next();

                URI relativeOutputFileURI = sourceDirectory.toURI().relativize( grammarFile.toURI() );
                String relativeOutputFileName =
                    relativeOutputFileURI.toString().replaceAll( "(.jj|.JJ)$", getOutputFileExtension() );

                File jjdocOutputFile = new File( getOutputDirectory(), relativeOutputFileName );
                jjdocOutputFile.getParentFile().mkdirs();

                String[] jjdocArgs = generateArgs( grammarFile, jjdocOutputFile );
                
                // Fork jjdoc because of calls to System.exit().
                forkJJDoc( jjdocArgs );

                this.createReportLink( sink, grammarFile, relativeOutputFileName );
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
     * 
     * @return The file extension to be used for the jjdoc output files.
     */
    public String getOutputFileExtension()
    {
        if ( text )
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
     * @param bundle The resource bundle with the text.
     * @param sink The sink for writing to the main report file.
     */
    public void createReportHeader( ResourceBundle bundle, Sink sink )
    {
        sink.head();
        sink.title();
        sink.text( bundle.getString( "report.jjdoc.title" ) );
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.jjdoc.title" ) );
        sink.sectionTitle1_();
        sink.text( bundle.getString( "report.jjdoc.description" ) );
        sink.section1_();

        sink.lineBreak();
        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.jjdoc.table.heading" ) );
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
        String grammarFileRelativePath = sourceDirectory.toURI().relativize( grammarFile.toURI() ).toString() ;
        if ( grammarFileRelativePath.startsWith( "/" ) )
        {
            grammarFileRelativePath = grammarFileRelativePath.substring( 1 );
        }
        sink.text( grammarFileRelativePath );
        sink.link_();
        sink.tableCell_();
        sink.tableRow_();
    }

    /**
     * Create the html footer for the report page.
     * 
     * @param sink The sink to write the html report page.
     */
    public void createReportFooter( Sink sink )
    {
        sink.table_();
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
     * @throws MojoExecutionException If there is a problem while scanning for .jj files.
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
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDirectory 
                + "\' for stale grammars to reprocess.", e );
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
     * @param jjdocArgs The arguments to pass to jjdoc.
     * @throws MojoExecutionException If there is a problem while running jjdoc.
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
     * Get the ResourceBundle for the report text.
     * @param locale The user locale
     * @return The resource bundle
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "jjdoc-report", locale, this.getClass().getClassLoader() );
    }

    /**
     * Consume and log command output from the jjdoc process
     */
    public class MojoLogStreamConsumer
        implements StreamConsumer
    {
        /**
         * Consume a line of text.
         * 
         * @param line The line to consume
         */
        public void consumeLine( String line )
        {
            getLog().info( line );
        }
    }

}

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * <a href="https://javacc.dev.java.net/doc/JJDoc.html">JJDoc</a> takes a JavaCC parser specification and produces
 * documentation for the BNF grammar. This mojo will search the source directory for all <code>*.jj</code> files and
 * run JJDoc once for each file it finds. Each of these output files, along with an <code>index.html</code> file will
 * be placed in the site directory (<code>target/site/jjdoc</code>), and a link will be created in the "Project
 * Reports" menu of the generated site.
 * 
 * @goal jjdoc
 * @execute phase=generate-sources
 * @since 2.3
 * @author <a href="mailto:pgier@redhat.com">Paul Gier</a>
 * @version $Id$
 * @see <a href="https://javacc.dev.java.net/doc/JJDoc.html">JJDoc Documentation</a>
 */
public class JJDocMojo
    extends AbstractMavenReport
{

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The current Maven project.
     * 
     * @parameter property="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The site renderer.
     * 
     * @component
     */
    private Renderer siteRenderer;

    /**
     * The directories where the JavaCC grammar files (<code>*.jj</code>) are located. By default, the directories
     * <code>${basedir}/src/main/javacc</code>, <code>${project.build.directory}/generated-sources/jjtree</code>
     * and <code>${project.build.directory}/generated-sources/jtb</code> are scanned for grammar files to document.
     * 
     * @parameter
     */
    private File[] sourceDirectories;

    /**
     * The default source directory for hand-crafted grammar files.
     * 
     * @parameter default-value="${basedir}/src/main/javacc"
     * @readonly
     */
    private File defaultGrammarDirectoryJavaCC;

    /**
     * The default source directory for grammar files generated by JJTree.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/jjtree"
     * @readonly
     */
    private File defaultGrammarDirectoryJJTree;

    /**
     * The default source directory for grammar files generated by JTB.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/jtb"
     * @readonly
     */
    private File defaultGrammarDirectoryJTB;

    /**
     * The relative path of the JJDoc reports in the output directory. This path will be appended to the output
     * directory.
     * 
     * @parameter default-value="jjdoc";
     */
    private String jjdocDirectory;

    /**
     * The destination directory where JJDoc saves the generated documentation files. Note that this parameter is only
     * relevant if the goal is run from the command line or from the default build lifecycle. If the goal is run
     * indirectly as part of a site generation, the output directory configured in the Maven Site Plugin is used
     * instead.
     * 
     * @parameter property="${outputDirectory}" default-value="${project.reporting.outputDirectory}"
     */
    private File outputDirectory;

    /**
     * The file encoding to use for reading the grammar files.
     * 
     * @parameter property="${grammarEncoding}" default-value="${project.build.sourceEncoding}"
     * @since 2.6
     */
    private String grammarEncoding;

    /**
     * The hypertext reference to an optional CSS file for the generated HTML documents. If specified, this CSS file
     * will be included via a <code>&lt;link&gt;</code> element in the HTML documents. Otherwise, the default style will
     * be used.
     * 
     * @parameter property="${cssHref}"
     * @since 2.5
     */
    private String cssHref;

    /**
     * A flag to specify the output format for the generated documentation. If set to <code>true</code>, JJDoc will
     * generate a plain text description of the BNF. Some formatting is done via tab characters, but the intention is to
     * leave it as plain as possible. Specifying <code>false</code> causes JJDoc to generate a hyperlinked HTML document
     * unless the parameter {@link #bnf} has been set to <code>true</code>. Default value is <code>false</code>.
     * 
     * @parameter property="${text}"
     */
    private Boolean text;

    /**
     * A flag whether to generate a plain text document with the unformatted BNF. Note that setting this option to
     * <code>true</code> is only effective if the parameter {@link #text} is <code>false</code>. Default value is
     * <code>false</code>.
     * 
     * @parameter property="${bnf}"
     * @since 2.6
     */
    private Boolean bnf;

    /**
     * This option controls the structure of the generated HTML output. If set to <code>true</code>, a single HTML
     * table for the entire BNF is generated. Setting it to <code>false</code> will produce one table for every
     * production in the grammar.
     * 
     * @parameter property="${oneTable}" default-value=true
     */
    private boolean oneTable;

    /**
     * Get the maven project.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     * @return The current Maven project.
     */
    protected MavenProject getProject()
    {
        return this.project;
    }

    /**
     * Get the site renderer.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     * @return The site renderer.
     */
    protected Renderer getSiteRenderer()
    {
        return this.siteRenderer;
    }

    /**
     * Get the output directory of the report if run directly from the command line.
     * 
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     * @return The report output directory.
     */
    protected String getOutputDirectory()
    {
        return this.outputDirectory.getAbsolutePath();
    }

    /**
     * Get the output directory of the JJDoc files, i.e. the sub directory in the report output directory as specified
     * by the {@link #jjdocDirectory} parameter.
     * 
     * @return The report output directory of the JJDoc files.
     */
    private File getJJDocOutputDirectory()
    {
        return new File( getReportOutputDirectory(), this.jjdocDirectory );
    }

    /**
     * Get the source directories that should be scanned for grammar files.
     * 
     * @return The source directories that should be scanned for grammar files, never <code>null</code>.
     */
    private File[] getSourceDirectories()
    {
        Set directories = new LinkedHashSet();
        if ( this.sourceDirectories != null && this.sourceDirectories.length > 0 )
        {
            directories.addAll( Arrays.asList( this.sourceDirectories ) );
        }
        else
        {
            if ( this.defaultGrammarDirectoryJavaCC != null )
            {
                directories.add( this.defaultGrammarDirectoryJavaCC );
            }
            if ( this.defaultGrammarDirectoryJJTree != null )
            {
                directories.add( this.defaultGrammarDirectoryJJTree );
            }
            if ( this.defaultGrammarDirectoryJTB != null )
            {
                directories.add( this.defaultGrammarDirectoryJTB );
            }
        }
        return (File[]) directories.toArray( new File[directories.size()] );
    }

    // ----------------------------------------------------------------------
    // public methods
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     * @param locale The locale to use for this report.
     * @return The name of this report.
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.jjdoc.name" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     * @param locale The locale to use for this report.
     * @return The description of this report.
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.jjdoc.short.description" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     * @return The name of the main report file.
     */
    public String getOutputName()
    {
        return this.jjdocDirectory + "/index";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     * @return <code>true</code> if the configured source directories are not empty, <code>false</code> otherwise.
     */
    public boolean canGenerateReport()
    {
        File sourceDirs[] = getSourceDirectories();
        for ( int i = 0; i < sourceDirs.length; i++ )
        {
            File sourceDir = sourceDirs[i];
            String[] files = sourceDir.list();
            if ( files != null && files.length > 0 )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Run the actual report.
     * 
     * @param locale The locale to use for this report.
     * @throws MavenReportException If the report generation failed.
     */
    public void executeReport( Locale locale )
        throws MavenReportException
    {
        Sink sink = getSink();

        createReportHeader( getBundle( locale ), sink );

        File[] sourceDirs = getSourceDirectories();
        for ( int j = 0; j < sourceDirs.length; j++ )
        {
            File sourceDir = sourceDirs[j];
            GrammarInfo[] grammarInfos = scanForGrammars( sourceDir );

            if ( grammarInfos == null )
            {
                getLog().debug( "Skipping non-existing source directory: " + sourceDir );
            }
            else
            {
                Arrays.sort( grammarInfos, GrammarInfoComparator.getInstance() );
                for ( int i = 0; i < grammarInfos.length; i++ )
                {
                    GrammarInfo grammarInfo = grammarInfos[i];
                    File grammarFile = grammarInfo.getGrammarFile();

                    String relativeOutputFileName = grammarInfo.getRelativeGrammarFile();
                    relativeOutputFileName =
                        relativeOutputFileName.replaceAll( "(?i)\\.(jj|jjt|jtb)$", getOutputFileExtension() );

                    File jjdocOutputFile = new File( getJJDocOutputDirectory(), relativeOutputFileName );

                    JJDoc jjdoc = newJJDoc();
                    jjdoc.setInputFile( grammarFile );
                    jjdoc.setOutputFile( jjdocOutputFile );
                    try
                    {
                        jjdoc.run();
                    }
                    catch ( Exception e )
                    {
                        throw new MavenReportException( "Failed to create BNF documentation: " + grammarFile, e );
                    }

                    createReportLink( sink, sourceDir, grammarFile, relativeOutputFileName );
                }
            }
        }

        createReportFooter( sink );
        sink.flush();
        sink.close();
    }

    /**
     * The JJDoc output file will have a <code>.html</code> or <code>.txt</code> extension depending on the value of
     * the parameters {@link #text} and {@link #bnf}.
     * 
     * @return The file extension (including the leading period) to be used for the JJDoc output files.
     */
    private String getOutputFileExtension()
    {
        if ( Boolean.TRUE.equals( this.text ) || Boolean.TRUE.equals( this.bnf ) )
        {
            return ".txt";
        }
        else
        {
            return ".html";
        }
    }

    /**
     * Create the header and title for the HTML report page.
     * 
     * @param bundle The resource bundle with the text.
     * @param sink The sink for writing to the main report file.
     */
    private void createReportHeader( ResourceBundle bundle, Sink sink )
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
     * Create a table row containing a link to the JJDoc report for a grammar file.
     * 
     * @param sink The sink to write the report
     * @param sourceDirectory The source directory of the grammar file.
     * @param grammarFile The JavaCC grammar file.
     * @param linkPath The path to the JJDoc output.
     */
    private void createReportLink( Sink sink, File sourceDirectory, File grammarFile, String linkPath )
    {
        sink.tableRow();
        sink.tableCell();
        if ( linkPath.startsWith( "/" ) )
        {
            linkPath = linkPath.substring( 1 );
        }
        sink.link( linkPath );
        String grammarFileRelativePath = sourceDirectory.toURI().relativize( grammarFile.toURI() ).toString();
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
     * Create the HTML footer for the report page.
     * 
     * @param sink The sink to write the HTML report page.
     */
    private void createReportFooter( Sink sink )
    {
        sink.table_();
        sink.body_();
    }

    /**
     * Creates a new facade to invoke JJDoc. Most options for the invocation are derived from the current values of the
     * corresponding mojo parameters. The caller is responsible to set the input file and output file on the returned
     * facade.
     * 
     * @return The facade for the tool invocation, never <code>null</code>.
     */
    private JJDoc newJJDoc()
    {
        JJDoc jjdoc = new JJDoc();
        jjdoc.setLog( getLog() );
        jjdoc.setGrammarEncoding( this.grammarEncoding );
        jjdoc.setCssHref( this.cssHref );
        jjdoc.setText( this.text );
        jjdoc.setBnf( this.bnf );
        jjdoc.setOneTable( Boolean.valueOf( this.oneTable ) );
        return jjdoc;
    }

    /**
     * Searches the specified source directory to find grammar files that can be documented.
     * 
     * @param sourceDirectory The source directory to scan for grammar files.
     * @return An array of grammar infos describing the found grammar files or <code>null</code> if the source
     *         directory does not exist.
     * @throws MavenReportException If there is a problem while scanning for .jj files.
     */
    private GrammarInfo[] scanForGrammars( File sourceDirectory )
        throws MavenReportException
    {
        if ( !sourceDirectory.isDirectory() )
        {
            return null;
        }

        GrammarInfo[] grammarInfos;

        getLog().debug( "Scanning for grammars: " + sourceDirectory );
        try
        {
            String[] includes = { "**/*.jj", "**/*.JJ", "**/*.jjt", "**/*.JJT", "**/*.jtb", "**/*.JTB" };
            GrammarDirectoryScanner scanner = new GrammarDirectoryScanner();
            scanner.setSourceDirectory( sourceDirectory );
            scanner.setIncludes( includes );
            scanner.scan();
            grammarInfos = scanner.getIncludedGrammars();
        }
        catch ( Exception e )
        {
            throw new MavenReportException( "Failed to scan for grammars: " + sourceDirectory, e );
        }
        getLog().debug( "Found grammars: " + Arrays.asList( grammarInfos ) );

        return grammarInfos;
    }

    /**
     * Get the resource bundle for the report text.
     * 
     * @param locale The locale to use for this report.
     * @return The resource bundle.
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "jjdoc-report", locale, getClass().getClassLoader() );
    }

    /**
     * Compares grammar infos using their relative grammar file paths as the sort key.
     */
    private static class GrammarInfoComparator
        implements Comparator
    {

        /**
         * The singleton instance of this comparator.
         */
        private static final GrammarInfoComparator INSTANCE = new GrammarInfoComparator();

        /**
         * Gets the singleton instance of this class.
         * 
         * @return The singleton instance of this class.
         */
        public static GrammarInfoComparator getInstance()
        {
            return INSTANCE;
        }

        /**
         * Compares the path of two grammar files lexicographically.
         * 
         * @param o1 The first grammar info.
         * @param o2 The second grammar info.
         * @return A negative integer if the first grammar is considered "smaller", a positive integer if it is
         *         considered "greater" and zero otherwise.
         */
        public int compare( Object o1, Object o2 )
        {
            int rel;

            GrammarInfo info1 = (GrammarInfo) o1;
            String[] paths1 = info1.getRelativeGrammarFile().split( "\\" + File.separatorChar );

            GrammarInfo info2 = (GrammarInfo) o2;
            String[] paths2 = info2.getRelativeGrammarFile().split( "\\" + File.separatorChar );

            int dirs = Math.min( paths1.length, paths2.length ) - 1;
            for ( int i = 0; i < dirs; i++ )
            {
                rel = paths1[i].compareToIgnoreCase( paths2[i] );
                if ( rel != 0 )
                {
                    return rel;
                }
            }

            rel = paths1.length - paths2.length;
            if ( rel != 0 )
            {
                return rel;
            }

            return paths1[paths1.length - 1].compareToIgnoreCase( paths2[paths1.length - 1] );
        }

    }

}

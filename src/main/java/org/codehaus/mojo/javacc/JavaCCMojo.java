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

/**
 * Parses a JavaCC grammar file (<code>*.jj</code>) and transforms it to Java source files. Detailed information
 * about the JavaCC options can be found on the <a href="https://github.com/javacc/javacc/">JavaCC website</a>.
 * 
 * @goal javacc
 * @phase generate-sources
 * @since 2.0
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JavaCCMojo
    extends AbstractJavaCCMojo
{

    /**
     * Package into which the generated classes will be put. Note that this will also be used to create the directory
     * structure where sources will be generated. Defaults to the package name specified in a grammar file.
     * 
     * @parameter property="${packageName}"
     * @deprecated As of version 2.4 because the plugin extracts the package name from each grammar file.
     */
    private String packageName;

    /**
     * The directory where the JavaCC grammar files (<code>*.jj</code>) are located.
     * 
     * @parameter property="${sourceDirectory}" default-value="${basedir}/src/main/javacc"
     */
    private File sourceDirectory;

    /**
     * The directory where the parser files generated by JavaCC will be stored. The directory will be registered as a
     * compile source root of the project such that the generated files will participate in later build phases like
     * compiling and packaging.
     * 
     * @parameter property="${outputDirectory}" default-value="${project.build.directory}/generated-sources/javacc"
     */
    private File outputDirectory;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     * 
     * @parameter property="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * A set of Ant-like inclusion patterns used to select files from the source directory for processing. By default,
     * the patterns <code>**&#47;*.jj</code> and <code>**&#47;*.JJ</code> are used to select grammar files.
     * 
     * @parameter
     */
    private String[] includes;

    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from being processed. By default, this set is
     * empty such that no files are excluded.
     * 
     * @parameter
     */
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
            return new String[] { "**/*.jj", "**/*.JJ" };
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
    protected int getStaleMillis()
    {
        return this.staleMillis;
    }

    /**
     * {@inheritDoc}
     */
    protected File[] getCompileSourceRoots()
    {
        return new File[] { getOutputDirectory() };
    }

    /**
     * {@inheritDoc}
     */
    protected String getParserPackage()
    {
        return this.packageName;
    }

    /**
     * {@inheritDoc}
     */
    protected void processGrammar( GrammarInfo grammarInfo )
        throws MojoExecutionException, MojoFailureException
    {
        File jjFile = grammarInfo.getGrammarFile();
        File jjDirectory = jjFile.getParentFile();

        File tempDirectory = getTempDirectory();

        // setup output directory of parser file (*.java) generated by JavaCC
        File parserDirectory = new File( tempDirectory, "parser" );

        // generate parser files
        JavaCC javacc = newJavaCC();
        javacc.setInputFile( jjFile );
        javacc.setOutputDirectory( parserDirectory );
        javacc.run();

        // copy parser files from JavaCC
        copyGrammarOutput( getOutputDirectory(), grammarInfo.getParserPackage(), parserDirectory,
                           grammarInfo.getParserName() + "*" );

        // copy source files which are next to grammar unless the grammar resides in an ordinary source root
        // (legacy support for custom sources)
        if ( !isSourceRoot( grammarInfo.getSourceDirectory() ) )
        {
            copyGrammarOutput( getOutputDirectory(), grammarInfo.getParserPackage(), jjDirectory, "*" );
        }

        deleteTempDirectory( tempDirectory );
    }

}

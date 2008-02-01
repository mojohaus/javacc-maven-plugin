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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Provides a facade for the mojos to invoke JJTree.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 * @see <a href="https://javacc.dev.java.net/doc/JJTree.html">JJTree Reference</a>
 */
class JJTree
{

    /**
     * The input grammar.
     */
    private File inputFile;

    /**
     * The option OUTPUT_DIRECTORY.
     */
    private File outputDirectory;

    /**
     * The option JDK_VERSION.
     */
    private String jdkVersion;

    /**
     * The option STATIC.
     */
    private Boolean isStatic;

    /**
     * The option BUILD_NODE_FILES.
     */
    private Boolean buildNodeFiles;

    /**
     * The option MULTI.
     */
    private Boolean multi;

    /**
     * The option NODE_DEFAULT_VOID.
     */
    private Boolean nodeDefaultVoid;

    /**
     * The option NODE_FACTORY.
     */
    private Boolean nodeFactory;

    /**
     * The option NODE_PACKAGE.
     */
    private String nodePackage;

    /**
     * The option NODE_PREFIX.
     */
    private String nodePrefix;

    /**
     * The option NODE_SCOPE_HOOK.
     */
    private Boolean nodeScopeHook;

    /**
     * The option NODE_USES_PARSER.
     */
    private Boolean nodeUsesParser;

    /**
     * The option VISITOR.
     */
    private Boolean visitor;

    /**
     * The option VISITOR_EXCEPTION.
     */
    private String visitorException;

    /**
     * Sets the absolute path to the grammar file to pass into JJTree for preprocessing.
     * 
     * @param value The absolute path to the grammar file to pass into JJTree for preprocessing.
     */
    public void setInputFile( File value )
    {
        this.inputFile = value;
    }

    /**
     * Sets the absolute path to the output directory.
     * 
     * @param value The absolute path to the output directory for the generated grammar file. If this directory does not
     *            exist yet, it is created. Note that this path should already include the desired package hierarchy
     *            because JJTree will not append the required sub directories automatically.
     */
    public void setOutputDirectory( File value )
    {
        this.outputDirectory = value;
    }

    /**
     * Sets the option JDK_VERSION.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setJdkVersion( String value )
    {
        this.jdkVersion = value;
    }

    /**
     * Sets the option STATIC.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setStatic( Boolean value )
    {
        this.isStatic = value;
    }

    /**
     * Sets the option value BUILD_NODE_FILES.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setBuildNodeFiles( Boolean value )
    {
        this.buildNodeFiles = value;
    }

    /**
     * Sets the option value MULTI.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setMulti( Boolean value )
    {
        this.multi = value;
    }

    /**
     * Sets the option value NODE_DEFAULT_VOID.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodeDefaultVoid( Boolean value )
    {
        this.nodeDefaultVoid = value;
    }

    /**
     * Sets the option value NODE_FACTORY.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodeFactory( Boolean value )
    {
        this.nodeFactory = value;
    }

    /**
     * Sets the option value NODE_PACKAGE.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodePackage( String value )
    {
        this.nodePackage = value;
    }

    /**
     * Sets the option value NODE_PREFIX.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodePrefix( String value )
    {
        this.nodePrefix = value;
    }

    /**
     * Sets the option value NODE_SCOPE_HOOK.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodeScopeHook( Boolean value )
    {
        this.nodeScopeHook = value;
    }

    /**
     * Sets the option value NODE_USES_PARSER.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodeUsesParser( Boolean value )
    {
        this.nodeUsesParser = value;
    }

    /**
     * Sets the option value VISITOR.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setVisitor( Boolean value )
    {
        this.visitor = value;
    }

    /**
     * Sets the option value VISITOR_EXCEPTION.
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setVisitorException( String value )
    {
        this.visitorException = value;
    }

    /**
     * Runs JJTree on the specified decorated grammar file to generate a grammar file annotated with node actions. The
     * options for JJTree are derived from the current values of the corresponding mojo parameters.
     * 
     * @param log A logger used to output diagnostic messages, may be <code>null</code>.
     * @throws MojoExecutionException If JJTree could not be invoked.
     * @throws MojoFailureException If JJTree reported a non-zero exit code.
     */
    public void run( Log log )
        throws MojoExecutionException, MojoFailureException
    {
        if ( this.inputFile == null )
        {
            throw new IllegalStateException( "input grammar not specified" );
        }
        if ( this.outputDirectory == null )
        {
            throw new IllegalStateException( "output directory not specified" );
        }

        int exitCode;
        try
        {
            String[] args = generateArguments();
            if ( log != null && log.isDebugEnabled() )
            {
                log.debug( "Running JJTree: " + Arrays.asList( args ) );
            }
            if ( !this.outputDirectory.exists() )
            {
                this.outputDirectory.mkdirs();
            }
            org.javacc.jjtree.JJTree jjtree = new org.javacc.jjtree.JJTree();
            exitCode = jjtree.main( args );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to execute JJTree", e );
        }
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "JJTree reported exit code " + exitCode + ": " + this.inputFile );
        }
    }

    /**
     * Assembles the command line arguments for the invocation of JJTree according to the configuration.<br/><br/>
     * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in the grammar file,
     * only those parameters that have been explicitly set by the user are passed on the command line.
     * 
     * @return A string array that represents the arguments to use for JJTree.
     */
    private String[] generateArguments()
    {
        List argsList = new ArrayList();

        if ( this.jdkVersion != null )
        {
            argsList.add( "-JDK_VERSION=" + this.jdkVersion );
        }

        if ( this.buildNodeFiles != null )
        {
            argsList.add( "-BUILD_NODE_FILES=" + this.buildNodeFiles );
        }

        if ( this.multi != null )
        {
            argsList.add( "-MULTI=" + this.multi );
        }

        if ( this.nodeDefaultVoid != null )
        {
            argsList.add( "-NODE_DEFAULT_VOID=" + this.nodeDefaultVoid );
        }

        if ( this.nodeFactory != null )
        {
            argsList.add( "-NODE_FACTORY=" + this.nodeFactory );
        }

        if ( this.nodePackage != null )
        {
            argsList.add( "-NODE_PACKAGE=" + this.nodePackage );
        }

        if ( this.nodePrefix != null )
        {
            argsList.add( "-NODE_PREFIX=" + this.nodePrefix );
        }

        if ( this.nodeScopeHook != null )
        {
            argsList.add( "-NODE_SCOPE_HOOK=" + this.nodeScopeHook );
        }

        if ( this.nodeUsesParser != null )
        {
            argsList.add( "-NODE_USES_PARSER=" + this.nodeUsesParser );
        }

        if ( this.isStatic != null )
        {
            argsList.add( "-STATIC=" + this.isStatic );
        }

        if ( this.visitor != null )
        {
            argsList.add( "-VISITOR=" + this.visitor );
        }

        if ( this.visitorException != null )
        {
            argsList.add( "-VISITOR_EXCEPTION=" + this.visitorException );
        }

        argsList.add( "-OUTPUT_DIRECTORY=" + this.outputDirectory.getAbsolutePath() );

        argsList.add( this.inputFile.getAbsolutePath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

}

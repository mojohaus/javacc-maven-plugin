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

import org.codehaus.plexus.util.FileUtils;

/**
 * Provides a facade for the mojos to invoke JTB.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 * @see <a href="http://compilers.cs.ucla.edu/jtb/">Java Tree Builder</a>
 */
public class JTB
{

    /**
     * The input grammar.
     */
    private File inputFile;

    /**
     * The base directory for the option "-o".
     */
    private File outputDirectory;

    /**
     * The option "-o".
     */
    private File outputFile;

    /**
     * The option "-p".
     */
    private String packageName;

    /**
     * The option "-np".
     */
    private String nodePackageName;

    /**
     * The option "-vp".
     */
    private String visitorPackageName;

    /**
     * The option "-e".
     */
    private Boolean supressErrorChecking;

    /**
     * The option "-jd".
     */
    private Boolean javadocFriendlyComments;

    /**
     * The option "-f".
     */
    private Boolean descriptiveFieldNames;

    /**
     * The option "-ns".
     */
    private String nodeParentClass;

    /**
     * The option "-pp".
     */
    private Boolean parentPointers;

    /**
     * The option "-tk".
     */
    private Boolean specialTokens;

    /**
     * The toolkit option "-scheme".
     */
    private Boolean scheme;

    /**
     * The toolkit option "-printer".
     */
    private Boolean printer;

    /**
     * Sets the absolute path to the grammar file to pass into JTB for preprocessing.
     * 
     * @param value The absolute path to the grammar file to pass into JTB for preprocessing.
     */
    public void setInputFile( File value )
    {
        this.inputFile = value;
        setOutputFile();
    }

    /**
     * Sets the absolute path to the output directory.
     * 
     * @param value The absolute path to the output directory for the generated grammar file. If this directory does not
     *            exist yet, it is created. Note that this path should already include the desired package hierarchy
     *            because JTB will not append the required sub directories automatically.
     */
    public void setOutputDirectory( File value )
    {
        this.outputDirectory = value;
        setOutputFile();
    }

    /**
     * Updates the path to the ouput file.
     */
    private void setOutputFile()
    {
        if ( this.outputDirectory != null && this.inputFile != null )
        {
            String fileName = FileUtils.removeExtension( this.inputFile.getName() ) + ".jj";
            this.outputFile = new File( this.outputDirectory, fileName );
        }
        else
        {
            this.outputFile = null;
        }
    }

    /**
     * Sets the option "-p".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setPackageName( String value )
    {
        this.packageName = value;
    }

    /**
     * Sets the option "-np".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodePackageName( String value )
    {
        this.nodePackageName = value;
    }

    /**
     * Sets the option "-vp".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setVisitorPackageName( String value )
    {
        this.visitorPackageName = value;
    }

    /**
     * Sets the option "-e".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setSupressErrorChecking( Boolean value )
    {
        this.supressErrorChecking = value;
    }

    /**
     * Sets the option "-jd".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setJavadocFriendlyComments( Boolean value )
    {
        this.javadocFriendlyComments = value;
    }

    /**
     * Sets the option "-f".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setDescriptiveFieldNames( Boolean value )
    {
        this.descriptiveFieldNames = value;
    }

    /**
     * Sets the option "-ns".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setNodeParentClass( String value )
    {
        this.nodeParentClass = value;
    }

    /**
     * Sets the option "-pp".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setParentPointers( Boolean value )
    {
        this.parentPointers = value;
    }

    /**
     * Sets the option "-tk".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setSpecialTokens( Boolean value )
    {
        this.specialTokens = value;
    }

    /**
     * Sets the toolkit option "-scheme".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setScheme( Boolean value )
    {
        this.scheme = value;
    }

    /**
     * Sets the toolkit option "-printer".
     * 
     * @param value The option value, may be <code>null</code>.
     */
    public void setPrinter( Boolean value )
    {
        this.printer = value;
    }

    /**
     * Runs JTB using the previously set parameters.
     * 
     * @return The exit code of JTB.
     * @throws Exception If the invocation failed.
     */
    public int run()
        throws Exception
    {
        if ( this.inputFile == null )
        {
            throw new IllegalStateException( "input grammar not specified" );
        }
        if ( this.outputDirectory == null )
        {
            throw new IllegalStateException( "output directory not specified" );
        }

        if ( !this.outputDirectory.exists() )
        {
            this.outputDirectory.mkdirs();
        }

        String[] args = generateArguments();
        EDU.purdue.jtb.JTB.main( args );
        return 0;
    }

    /**
     * Assembles the command line arguments for the invocation of JTB according to the configuration.
     * 
     * @return A string array that represents the command line arguments to use for JTB.
     */
    private String[] generateArguments()
    {
        List argsList = new ArrayList();

        if ( this.packageName != null )
        {
            argsList.add( "-p" );
            argsList.add( this.packageName );
        }
        else
        {
            if ( this.nodePackageName != null )
            {
                argsList.add( "-np" );
                argsList.add( this.nodePackageName );
            }
            if ( this.visitorPackageName != null )
            {
                argsList.add( "-vp" );
                argsList.add( this.visitorPackageName );
            }
        }

        if ( this.supressErrorChecking != null && this.supressErrorChecking.booleanValue() )
        {
            argsList.add( "-e" );
        }

        if ( this.javadocFriendlyComments != null && this.javadocFriendlyComments.booleanValue() )
        {
            argsList.add( "-jd" );
        }

        if ( this.descriptiveFieldNames != null && this.descriptiveFieldNames.booleanValue() )
        {
            argsList.add( "-f" );
        }

        if ( this.nodeParentClass != null )
        {
            argsList.add( "-ns" );
            argsList.add( this.nodeParentClass );
        }

        if ( this.parentPointers != null && this.parentPointers.booleanValue() )
        {
            argsList.add( "-pp" );
        }

        if ( this.specialTokens != null && this.specialTokens.booleanValue() )
        {
            argsList.add( "-tk" );
        }

        if ( this.scheme != null && this.scheme.booleanValue() )
        {
            argsList.add( "-scheme" );
        }

        if ( this.printer != null && this.printer.booleanValue() )
        {
            argsList.add( "-printer" );
        }

        argsList.add( "-o" );
        argsList.add( this.outputFile.getAbsolutePath() );

        argsList.add( this.inputFile.getAbsolutePath() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * Gets a string representation of the command line arguments.
     * 
     * @return A string representation of the command line arguments.
     */
    public String toString()
    {
        return Arrays.asList( generateArguments() ).toString();
    }

}

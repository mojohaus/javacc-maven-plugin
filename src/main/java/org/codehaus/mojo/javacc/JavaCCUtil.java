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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Some helper methods used by the javacc mojos.
 * 
 * @author pgier
 * @version $Id$
 */
public class JavaCCUtil
{

    /**
     * JavaCC class package declaration
     */
    public static final String PACKAGE_DECLARATION = "package ";

    /**
     * Searches the grammar file for a package declaration. If found, its value is returned.
     * Note: The relative package path (using OS-specific directory separator) is returned, not the actual package name.  For Example, if the
     * package is "com.stuff.mycode", the value returned will be "com/stuff/mycode".
     * 
     * @param javaccInput the grammar path name
     * @return the package declared in the class code or null if no package is declared.
     * @throws MojoExecutionException in case of IOException
     */
    public static String getDeclaredPackage( File javaccInput )
        throws MojoExecutionException
    {
        //
        // Let's read the content of the file first
        //
        String grammar = null;

        try
        {
            grammar = FileUtils.fileRead( javaccInput );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to read grammar file '" + javaccInput + "'", e );
        }

        //
        // Note: the way to search this parameter can be much more smart;
        // let's start easy.
        //
        int begin = grammar.indexOf( PACKAGE_DECLARATION );
        if ( begin < 0 )
        {
            return null;
        }

        int end = grammar.indexOf( ';', begin );

        String packageName =
            StringUtils.replace( grammar.substring( begin + PACKAGE_DECLARATION.length(), end ).trim(), '.',
                                 File.separatorChar );

        return packageName;
    }

}

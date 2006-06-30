package org.codehaus.mojo.javacc;

/*
 * Copyright 2001-2005 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.ArrayList;
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


/**
 * @goal javacc
 * @phase generate-sources
 * @description Goal which parse a JJ file and transform it to Java Source Files.
 * @author jruiz@exist.com
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 */
public class JavaCCMojo
    extends AbstractMojo
{      
    /**
     * @parameter expression=${lookAhead}"
     */
    private Integer lookAhead;
    
    /**
     * @parameter expression="${choiceAmbiguityCheck}"
     */
    private Integer choiceAmbiguityCheck;

    /**
     * @parameter expression=${otherAmbiguityCheck}"
     */
    private Integer otherAmbiguityCheck;
    
    /**
     * @parameter expression=${isStatic}"
     */
    private Boolean isStatic;

    /**
     * @parameter expression="${debugParser}"
     */
    private Boolean debugParser;

    /**
     * @parameter expression="${debugLookAhead}"
     */
    private Boolean debugLookAhead;

    /**
     * @parameter expression="${debugTokenManager}"
     */
    private Boolean debugTokenManager;

    /**
     * @parameter expression="${optimizeTokenManager}"
     */
    private Boolean optimizeTokenManager;

    /**
     * @parameter expression="${errorReporting}"
     */
    private Boolean errorReporting;

    /**
     * @parameter expression="${javaUnicodeEscape}"
     */
    private Boolean javaUnicodeEscape;

    /**
     * @parameter expression="${unicodeInput}"
     */
    private Boolean unicodeInput;

    /**
     * @parameter expression="${ignoreCase}"
     */
    private Boolean ignoreCase;

    /**
     * @parameter expression="${commonTokenAction}"
     */
    private Boolean commonTokenAction;

    /**
     * @parameter expression="${userTokenManager}"
     */
    private Boolean userTokenManager;

    /**
     * @parameter expression="${userCharStream}"
     */
    private Boolean userCharStream;

    /**
     * @parameter expression="${buildParser}"
     */
    private Boolean buildParser;

    /**
     * @parameter expression="${buildTokenManager}"
     */
    private Boolean buildTokenManager;

    /**
     * @parameter expression="${sanityCheck}"
     */
    private Boolean sanityCheck;

    /**
     * @parameter expression="${forceLaCheck}"
     */
    private Boolean forceLaCheck;

    /**
     * @parameter expression="${cacheTokens}"
     */
    private Boolean cacheTokens;

    /**
     * @parameter expression="${keepLineColumn}"
     */
    private Boolean keepLineColumn;
    
    /**
     * @parameter expression="${packageName}"
     */
    private String packageName;
    
    /**
     * Directory where the JJ file(s) are located.
     * @parameter expression="${basedir}/src/main/javacc"
     * @required
     */
    private String sourceDirectory;
    
    /**
     * Directory where the output Java Files will be located.
     * @parameter expression="${project.build.directory}/generated-sources/javacc"
     * @required
     */
    private String outputDirectory;

    /**
     * the directory to store the processed .jj files
     * 
     * @parameter expression="${project.build.directory}/generated-sources/javacc-timestamp"
     */
    private String timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification
     * date for testing whether a source needs recompilation
     * 
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    
    public void execute()
        throws MojoExecutionException
    {
        // check packageName for . vs /
        if ( packageName != null )
        {
            packageName = StringUtils.replace(packageName, '.', File.separatorChar);
        }
        
        if ( !FileUtils.fileExists( outputDirectory ) )
        {
            if ( packageName != null )
            {
                FileUtils.mkdir( outputDirectory  + File.separator + packageName );
            } 
            else 
            {
                FileUtils.mkdir( outputDirectory );   
            }
        }
        
        if ( !FileUtils.fileExists( timestampDirectory ) )
        {
            FileUtils.mkdir( timestampDirectory );
        }

        Set staleGrammars = computeStaleGrammars();

        if ( staleGrammars.isEmpty() )
        {
           getLog().info( "Nothing to process - all grammars are up to date" );
           if ( project != null )
           {
              project.addCompileSourceRoot( outputDirectory );
           }
           return;
        }

        for ( Iterator i = staleGrammars.iterator(); i.hasNext(); )
        {
            File javaccFile = (File) i.next();
            try
            {
                org.javacc.parser.Main.mainProgram( generateJavaCCArgumentList( javaccFile.getAbsolutePath()) );
                
                FileUtils.copyFileToDirectory(javaccFile, new File(timestampDirectory));
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "JavaCC execution failed", e );
            }
        }
        
        if ( project != null )
        {
           project.addCompileSourceRoot( outputDirectory );
        }
    }
    

    private String[] generateJavaCCArgumentList(String javaccInput) {

        ArrayList argsList = new ArrayList();
        
        if ( lookAhead != null )
        {
            argsList.add("-LOOKAHEAD=" + lookAhead);
        }
        
        if ( choiceAmbiguityCheck != null)
        {
            argsList.add("-CHOICE_AMBIGUITY_CHECK=" + choiceAmbiguityCheck);
        }
        
        if ( otherAmbiguityCheck != null )
        {
            argsList.add("-OTHER_AMBIGUITY_CHECK=" + otherAmbiguityCheck);
        }
        
        if ( isStatic != null )
        {
            argsList.add("-STATIC=" + isStatic);
        }
        
        if ( debugParser != null )
        {
            argsList.add("-DEBUG_PARSER=" + debugParser);
        }
        
        if ( debugLookAhead != null )
        {
            argsList.add("-DEBUG_LOOKAHEAD=" + debugLookAhead);
        }
        
        if ( debugTokenManager != null ) 
        {
            argsList.add("-DEBUG_TOKEN_MANAGER=" + debugTokenManager);
        }
        
        if ( optimizeTokenManager != null )
        {
            argsList.add("-OPTIMIZE_TOKEN_MANAGER=" + optimizeTokenManager);
        }
        
        if ( errorReporting != null )
        {
            argsList.add("-ERROR_REPORTING="+ errorReporting);
        }
        
        if ( javaUnicodeEscape != null )
        {
            argsList.add("-JAVA_UNICODE_ESCAPE=" + javaUnicodeEscape);
        }
        
        if ( unicodeInput != null )
        {
            argsList.add("-UNICODE_INPUT=" + unicodeInput);
        }
        
        if ( ignoreCase != null )
        {
            argsList.add("-IGNORE_CASE=" + ignoreCase);
        }
        
        if ( commonTokenAction != null )
        {
            argsList.add("-COMMON_TOKEN_ACTION=" + commonTokenAction);
        }
        
        if ( userTokenManager != null )
        {
            argsList.add("-USER_TOKEN_MANAGER=" + userTokenManager);
        }
        
        if ( userCharStream != null )
        {
            argsList.add("-USER_CHAR_STREAM=" + userCharStream);
        }
        
        if ( buildParser != null )
        {
            argsList.add("-BUILD_PARSER=" + buildParser);
        }
        
        if ( buildTokenManager != null )
        {
            argsList.add("-BUILD_TOKEN_MANAGER=" + buildTokenManager);
        }
        
        if ( sanityCheck != null )
        {
            argsList.add("-SANITY_CHECK=" + sanityCheck);
        }
        
        if ( forceLaCheck != null )
        {
            argsList.add("-FORCE_LA_CHECK=" + forceLaCheck);
        }
        
        if ( cacheTokens != null ) 
        {
            argsList.add("-CACHE_TOKENS=" + cacheTokens);
        }
        
        if ( keepLineColumn != null )
        {
            argsList.add("-KEEP_LINE_COLUMN=" + keepLineColumn);
        }
        
        if ( packageName != null )
        {
            argsList.add("-OUTPUT_DIRECTORY:" + outputDirectory + File.separator + packageName );
        } 
        else 
        {
           argsList.add("-OUTPUT_DIRECTORY:" + outputDirectory );
        }
        
        argsList.add(javaccInput);
     
        getLog().debug("argslist: " + argsList.toString());
        
        return (String[])argsList.toArray(new String[argsList.size()]);
     }
    
     private Set computeStaleGrammars() throws MojoExecutionException
       {
          SuffixMapping mapping = new SuffixMapping( ".jj", ".jj" );
          SuffixMapping mappingCAP = new SuffixMapping( ".JJ", ".JJ" );

          SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis );

          scanner.addSourceMapping( mapping );
          scanner.addSourceMapping( mappingCAP);

          File outDir = new File( timestampDirectory );

          Set staleSources = new HashSet();

          File sourceDir = new File( sourceDirectory );

          try
          {
             staleSources.addAll( scanner.getIncludedSources( sourceDir, outDir ) );
          }
          catch ( InclusionScanException e )
          {
             throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir + "\' for stale grammars to reprocess.", e );
          }

          return staleSources;
       }

    
}


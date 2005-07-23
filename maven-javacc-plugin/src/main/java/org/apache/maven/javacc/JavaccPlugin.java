package org.apache.maven.javacc;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.javacc.parser.Main;


/**
 * @goal override-generate
 * @phase generate-sources
 * @description Goal which parse a JJ file and transform it to Java Source Files.
 */
public class JavaccPlugin
    extends AbstractMojo
{      
    /**
     * @parameter expression=1
     * @required
     */
    private int lookAhead;
    
    /**
     * @parameter expression=2
     * @required
     */
    private int choiceAmbiguityCheck;

    /**
     * @parameter expression=1
     * @required
     */
    private int otherAmbiguityCheck;
    
    /**
     * @parameter expression="true"
     * @required
     */
    private String isStatic;

    /**
     * @parameter expression="false"
     * @required
     */
    private String debugParser;

    /**
     * @parameter expression="false"
     * @required
     */
    private String debugLookAhead;

    /**
     * @parameter expression="false"
     * @required
     */
    private String debugTokenManager;

    /**
     * @parameter expression="true"
     * @required
     */
    private String optimizeTokenManager;

    /**
     * @parameter expression="true"
     * @required
     */
    private String errorReporting;

    /**
     * @parameter expression="false"
     * @required
     */
    private String javaUnicodeEscape;

    /**
     * @parameter expression="false"
     * @required
     */
    private String unicodeInput;

    /**
     * @parameter expression="false"
     * @required
     */
    private String ignoreCase;

    /**
     * @parameter expression="false"
     * @required
     */
    private String commonTokenAction;

    /**
     * @parameter expression="false"
     * @required
     */
    private String userTokenManager;

    /**
     * @parameter expression="false"
     * @required
     */
    private String userCharStream;

    /**
     * @parameter expression="true"
     * @required
     */
    private String buildParser;

    /**
     * @parameter expression="true"
     * @required
     */
    private String buildTokenManager;

    /**
     * @parameter expression="true"
     * @required
     */
    private String sanityCheck;

    /**
     * @parameter expression="false"
     * @required
     */
    private String forceLaCheck;

    /**
     * @parameter expression="false"
     * @required
     */
    private String cacheTokens;

    /**
     * @parameter expression="true"
     * @required
     */
    private String keepLineColumn;
    
    /**
     * Directory where the output Java Files will be located.
     * @parameter  
     * @required
     */
    private String outputDirectory;
    
    /**
     * Directory where the JJ  file is located.
     * @parameter 
     * @required
     */
    private String targetFileName;

    public void execute()
        throws MojoExecutionException
    {
        
    	String[] args = new String[23];   
       
        args[0] ="-LOOKAHEAD=" + lookAhead;
        args[1] ="-CHOICE_AMBIGUITY_CHECK=" + choiceAmbiguityCheck;
        args[2] ="-OTHER_AMBIGUITY_CHECK=" + otherAmbiguityCheck;        
        args[3] ="-STATIC=" + isStatic;
        args[4] ="-DEBUG_PARSER=" + debugParser;
        args[5] ="-DEBUG_LOOKAHEAD=" + debugLookAhead;
        args[6] ="-DEBUG_TOKEN_MANAGER=" + debugTokenManager;
        args[7] ="-OPTIMIZE_TOKEN_MANAGER=" + optimizeTokenManager;
        args[8] ="-ERROR_REPORTING="+ errorReporting;
        args[9] ="-JAVA_UNICODE_ESCAPE=" + javaUnicodeEscape;
        args[10]="-UNICODE_INPUT=" + unicodeInput;
        args[11]="-IGNORE_CASE=" + ignoreCase;
        args[12]="-COMMON_TOKEN_ACTION=" + commonTokenAction;
        args[13]="-USER_TOKEN_MANAGER=" + userTokenManager;
        args[14]="-USER_CHAR_STREAM=" + userCharStream;
        args[15]="-BUILD_PARSER=" + buildParser;
        args[16]="-BUILD_TOKEN_MANAGER=" + buildTokenManager;
        args[17]="-SANITY_CHECK=" + sanityCheck;
        args[18]="-FORCE_LA_CHECK=" + forceLaCheck;
        args[19]="-CACHE_TOKENS=" + cacheTokens;
        args[20]="-KEEP_LINE_COLUMN=" + keepLineColumn;
        args[21]="-OUTPUT_DIRECTORY:" + createAbsoluteOutputDirectory(outputDirectory); 
        args[22]=createAbsoluteTargetFileName(targetFileName);
        
    	

        try
        {
            Main.mainProgram(args);      
         }
        catch (Exception e){
        	e.printStackTrace();
        }
    }
    
    /**
     * @return Returns the absolute path of the  outputDirectory.
     */
   public String createAbsoluteOutputDirectory(String absoluteOutputDirectory) 
    {
        File f = new File(absoluteOutputDirectory);
        
        outputDirectory = f.getAbsolutePath();
        
        if (!f.exists())  f.mkdirs();
        
        return outputDirectory;
    }
    
    /**
     * @return Returns the absolute path of the  targetFileName.
     */
    public String createAbsoluteTargetFileName(String absoluteTargetFileName)
    {
        File f = new File(absoluteTargetFileName);
        
        targetFileName = f.getAbsolutePath() ;
            
        return targetFileName;
    } 
    
	/**
	 * @return Returns the buildParser.
	 */
	public String getBuildParser() {
		return buildParser;
	}
	/**
	 * @param buildParser The buildParser to set.
	 */
	public void setBuildParser(String buildParser) {
		this.buildParser = buildParser;
	}
	/**
	 * @return Returns the buildTokenManager.
	 */
	public String getBuildTokenManager() {
		return buildTokenManager;
	}
	/**
	 * @param buildTokenManager The buildTokenManager to set.
	 */
	public void setBuildTokenManager(String buildTokenManager) {
		this.buildTokenManager = buildTokenManager;
	}
	/**
	 * @return Returns the cacheTokens.
	 */
	public String getCacheTokens() {
		return cacheTokens;
	}
	/**
	 * @param cacheTokens The cacheTokens to set.
	 */
	public void setCacheTokens(String cacheTokens) {
		this.cacheTokens = cacheTokens;
	}
	/**
	 * @return Returns the choiceAmbiguityCheck.
	 */
	public int getChoiceAmbiguityCheck() {
		return choiceAmbiguityCheck;
	}
	/**
	 * @param choiceAmbiguityCheck The choiceAmbiguityCheck to set.
	 */
	public void setChoiceAmbiguityCheck(int choiceAmbiguityCheck) {
		this.choiceAmbiguityCheck = choiceAmbiguityCheck;
	}
	/**
	 * @return Returns the commonTokenAction.
	 */
	public String getCommonTokenAction() {
		return commonTokenAction;
	}
	/**
	 * @param commonTokenAction The commonTokenAction to set.
	 */
	public void setCommonTokenAction(String commonTokenAction) {
		this.commonTokenAction = commonTokenAction;
	}
	/**
	 * @return Returns the debugLookAhead.
	 */
	public String getDebugLookAhead() {
		return debugLookAhead;
	}
	/**
	 * @param debugLookAhead The debugLookAhead to set.
	 */
	public void setDebugLookAhead(String debugLookAhead) {
		this.debugLookAhead = debugLookAhead;
	}
	/**
	 * @return Returns the debugParser.
	 */
	public String getDebugParser() {
		return debugParser;
	}
	/**
	 * @param debugParser The debugParser to set.
	 */
	public void setDebugParser(String debugParser) {
		this.debugParser = debugParser;
	}
	/**
	 * @return Returns the debugTokenManager.
	 */
	public String getDebugTokenManager() {
		return debugTokenManager;
	}
	/**
	 * @param debugTokenManager The debugTokenManager to set.
	 */
	public void setDebugTokenManager(String debugTokenManager) {
		this.debugTokenManager = debugTokenManager;
	}
	/**
	 * @return Returns the errorReporting.
	 */
	public String getErrorReporting() {
		return errorReporting;
	}
	/**
	 * @param errorReporting The errorReporting to set.
	 */
	public void setErrorReporting(String errorReporting) {
		this.errorReporting = errorReporting;
	}
	/**
	 * @return Returns the forceLaCheck.
	 */
	public String getForceLaCheck() {
		return forceLaCheck;
	}
	/**
	 * @param forceLaCheck The forceLaCheck to set.
	 */
	public void setForceLaCheck(String forceLaCheck) {
		this.forceLaCheck = forceLaCheck;
	}
	/**
	 * @return Returns the ignoreCase.
	 */
	public String getIgnoreCase() {
		return ignoreCase;
	}
	/**
	 * @param ignoreCase The ignoreCase to set.
	 */
	public void setIgnoreCase(String ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	/**
	 * @return Returns the isStatic.
	 */
	public String getIsStatic() {
		return isStatic;
	}
	/**
	 * @param isStatic The isStatic to set.
	 */
	public void setIsStatic(String isStatic) {
		this.isStatic = isStatic;
	}
	/**
	 * @return Returns the javaUnicodeEscape.
	 */
	public String getJavaUnicodeEscape() {
		return javaUnicodeEscape;
	}
	/**
	 * @param javaUnicodeEscape The javaUnicodeEscape to set.
	 */
	public void setJavaUnicodeEscape(String javaUnicodeEscape) {
		this.javaUnicodeEscape = javaUnicodeEscape;
	}
	/**
	 * @return Returns the keepLineColumn.
	 */
	public String getKeepLineColumn() {
		return keepLineColumn;
	}
	/**
	 * @param keepLineColumn The keepLineColumn to set.
	 */
	public void setKeepLineColumn(String keepLineColumn) {
		this.keepLineColumn = keepLineColumn;
	}
	/**
	 * @return Returns the lookahead.
	 */
	public int getLookahead() {
		return lookAhead;
	}
	/**
	 * @param lookahead The lookahead to set.
	 */
	public void setLookahead(int lookahead) {
		this.lookAhead = lookahead;
	}
	/**
	 * @return Returns the optimizeTokenManager.
	 */
	public String getOptimizeTokenManager() {
		return optimizeTokenManager;
	}
	/**
	 * @param optimizeTokenManager The optimizeTokenManager to set.
	 */
	public void setOptimizeTokenManager(String optimizeTokenManager) {
		this.optimizeTokenManager = optimizeTokenManager;
	}
	/**
	 * @return Returns the otherAmbiguityCheck.
	 */
	public int getOtherAmbiguityCheck() {
		return otherAmbiguityCheck;
	}
	/**
	 * @param otherAmbiguityCheck The otherAmbiguityCheck to set.
	 */
	public void setOtherAmbiguityCheck(int otherAmbiguityCheck) {
		this.otherAmbiguityCheck = otherAmbiguityCheck;
	}
	/**
	 * @return Returns the outputDirectory.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}
	/**
	 * @param outputDirectory The outputDirectory to set.
	 */
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	/**
	 * @return Returns the sanityCheck.
	 */
	public String getSanityCheck() {
		return sanityCheck;
	}
	/**
	 * @param sanityCheck The sanityCheck to set.
	 */
	public void setSanityCheck(String sanityCheck) {
		this.sanityCheck = sanityCheck;
	}
	/**
	 * @return Returns the targetFileName.
	 */
	public String getTargetFileName() {
		return targetFileName;
	}
	/**
	 * @param targetFileName The targetFileName to set.
	 */
	public void setTargetFileName(String targetFileName) {
		this.targetFileName = targetFileName;
	}
	/**
	 * @return Returns the unicodeInput.
	 */
	public String getUnicodeInput() {
		return unicodeInput;
	}
	/**
	 * @param unicodeInput The unicodeInput to set.
	 */
	public void setUnicodeInput(String unicodeInput) {
		this.unicodeInput = unicodeInput;
	}
	/**
	 * @return Returns the userCharStream.
	 */
	public String getUserCharStream() {
		return userCharStream;
	}
	/**
	 * @param userCharStream The userCharStream to set.
	 */
	public void setUserCharStream(String userCharStream) {
		this.userCharStream = userCharStream;
	}
	/**
	 * @return Returns the userTokenManager.
	 */
	public String getUserTokenManager() {
		return userTokenManager;
	}
	/**
	 * @param userTokenManager The userTokenManager to set.
	 */
	public void setUserTokenManager(String userTokenManager) {
		this.userTokenManager = userTokenManager;
	}
}


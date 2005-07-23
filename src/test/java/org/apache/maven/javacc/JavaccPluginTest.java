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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.maven.javacc.JavaccPlugin;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author Exist Mergere Team
 *
*
*
 */
public class JavaccPluginTest extends TestCase {

   JavaccPlugin p;
   
   private String outputDirectory =  "src/test/resources/output/";
   private String validatorDirectory = "src/test/resources/validator/";
   private String targetFileName = "src/test/resources/SelectorParser.jj";
   
   File tmpFile = new File("tmp");
   
   public void testCreateAbsoluteOutputDirectory()
   {   
       tmpFile = new File(outputDirectory); 
       
       assertEquals("[ERROR] : Output directory path not absolute.", p.createAbsoluteOutputDirectory(outputDirectory),tmpFile.getAbsolutePath());  
       
       tmpFile = new File(tmpFile.getAbsolutePath());
       
       assertTrue("[ERROR] : Output directory not created.", tmpFile.exists()); 
       
       tmpFile.delete();
   }
   
   public void testCreateAbsoluteTargetFileName()
   {
       File f = new File(targetFileName);
       
       assertEquals("[ERROR] : Target filename not absolute.",p.createAbsoluteTargetFileName(targetFileName),f.getAbsolutePath());  
   }
   
   public void testExecute() 
   throws MojoExecutionException
   {   
   	   p.setOutputDirectory(outputDirectory);
   	   
   	   p.setTargetFileName(targetFileName);
   	   
       p.createAbsoluteOutputDirectory(p.getOutputDirectory());
       
       p.createAbsoluteTargetFileName(p.getTargetFileName());
       
       p.setBuildParser("true");
       
       p.setBuildTokenManager("true");
       
       p.setCacheTokens("false");
       
       p.setChoiceAmbiguityCheck(2);
       
       p.setCommonTokenAction("false");
       
       p.setDebugLookAhead("false");
       
       p.setDebugParser("false");
       
       p.setDebugTokenManager("false");
       
       p.setErrorReporting("false");
       
       p.setForceLaCheck("false");
       
       p.setIgnoreCase("false");
       
       p.setIsStatic("false");
       
       p.setJavaUnicodeEscape("false");
       
       p.setKeepLineColumn("true");
       
       p.setLookahead(1);
       
       p.setOptimizeTokenManager("true");
       
       p.setOtherAmbiguityCheck(1);
       
       p.setSanityCheck("true");
       
       p.setUnicodeInput("true");
       
       p.setUserCharStream("false");
       
       p.setUserTokenManager("false");
       
       p.execute();
       
   }
   
   public void testOutputSourceFileOne()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("ParseException.java");
   }
   
   public void testOutputSourceFileTwo()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("SelectorParser.java");
   }
   
   public void testOutputSourceFileThree()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("SelectorParserConstants.java");
   }
   
   public void testOutputSourceFileFour()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("SelectorParserTokenManager.java");
   }
   
   public void testOutputSourceFileFive()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("SimpleCharStream.java");
   }
   
   public void testOutputSourceFileSix()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("Token.java");
   }
   
   public void testOutputSourceFileSeven()  throws FileNotFoundException, IOException 
   {
       validateOutputFile("TokenMgrError.java");    
   }
   
   public void testDeleteOutputDirectory()
   {
       tmpFile = new File(outputDirectory);
       
       assertTrue(true);
   }
   
   public void validateOutputFile(String FileName)  throws FileNotFoundException, IOException 
   {
       tmpFile  = new File(outputDirectory + FileName);
       
       assertTrue("[ERROR] :  " + FileName + " was not created." ,tmpFile.exists());
       
       assertTrue("[ERROR] : "  + FileName + " was invalid." ,compareTextFiles(tmpFile, new File(validatorDirectory + FileName)));
       
   }
   
   public void testOutputDirectory() {
   	
   		p.setOutputDirectory(outputDirectory);
   		
   		assertEquals("src/test/resources/output/", p.getOutputDirectory());
   }
   
   public void testTargetFileName() {
   	
   		p.setTargetFileName(targetFileName);
   		
   		assertEquals("src/test/resources/SelectorParser.jj", p.getTargetFileName());
   }
   
   
   public void setUp() 
   throws Exception 
   {
       p = new JavaccPlugin();
   }
   
   public void tearDown()
   throws Exception 
   {
       if (tmpFile.exists()) tmpFile.delete(); // clean up created temporary file
   }
   
   
   private boolean compareTextFiles(File f1, File f2) throws FileNotFoundException, IOException 
   { 
       String text1 = getTextContents(f1); 
       
       String text2 = getTextContents(f2); 
       
       StringTokenizer tokenizer1 = new StringTokenizer(text1); 
       
       StringTokenizer tokenizer2 = new StringTokenizer(text2); 
       
       if (tokenizer1.countTokens() != tokenizer2.countTokens()) return false; 
       
       while (tokenizer1.hasMoreTokens()) 
       { 
           if (!tokenizer1.nextToken().equalsIgnoreCase(tokenizer2.nextToken())) return false; 
       } 
       return true; 
   } 
   
   private String getTextContents(File f) throws FileNotFoundException, IOException 
   { 
       FileInputStream fIn = new FileInputStream(f); 
       
       byte fBytes[] = new byte[fIn.available()]; 
       
       fIn.read(fBytes); 
       
       fIn.close(); 
       
       return new String(fBytes); 
   } 
   

    public void testLookAhead() {
    	
    	p.setLookahead(1);
    	assertEquals("LOOKAHEAD VALUE", 1, p.getLookahead());
    }
    
    public void testChoiceAmbiguityCheck() {
    	
    	p.setChoiceAmbiguityCheck(1);
    	assertEquals("CHOICEAMBIGUITYCHECK", 1, p.getChoiceAmbiguityCheck());
    }
    
    public void testOtherAmbiguityCheck() {
    	
    	p.setOtherAmbiguityCheck(1);
    	assertEquals("OTHERAMBIGUITYCHECK", 1, p.getOtherAmbiguityCheck());
    }
    
    public void testIsStatic() {
    	
    	p.setIsStatic("true");
    	assertEquals("ISSTATIC", "true", p.getIsStatic());
    }
    
    public void testDebugParser() {
    	
    	p.setDebugParser("true");
    	assertEquals("DEBUGPARSER", "true", p.getDebugParser());
    }
    
    public void testDebugLookAhead() {
    	
    	p.setDebugLookAhead("false");
    	assertEquals("DEBUGLOOKAHEAD", "false", p.getDebugLookAhead());
    }
    
    public void testDebugTokenManager() {
    	
    	p.setDebugTokenManager("false");
    	assertEquals("DEBUGTOKENMANAGER", "false", p.getDebugTokenManager());
    }
    
    public void testOptimizeTokenManager() {
    	
    	p.setOptimizeTokenManager("true");
    	assertEquals("OPTIMIZETOKENMANAGER", "true", p.getOptimizeTokenManager());
    }
    
    public void testErrorReporting() {
    	
    	p.setErrorReporting("true");
    	assertEquals("ERRORREPORTING", "true", p.getErrorReporting());
    }
    
    public void testJavaUnicodeEscape() {
    	
    	p.setJavaUnicodeEscape("false");
    	assertEquals("JAVAUNICODEESCAPE", "false", p.getJavaUnicodeEscape());
    }
    
    public void testUnicodeInput() {
    	
    	p.setUnicodeInput("true");
    	assertEquals("UNICODEINPUT", "true", p.getUnicodeInput());
    }
    
    public void testIgnoreCase() {
    	
    	p.setIgnoreCase("true");
    	assertEquals("IGNORECASE", "true", p.getIgnoreCase());
    }
    
    public void testCommonTokenAction() {
    	
    	p.setCommonTokenAction("true");
    	assertEquals("COMMONTOKENACTION", "true", p.getCommonTokenAction());
    }
    
    public void testUserTokenManager() {
    	
    	p.setUserTokenManager("true");
    	assertEquals("USERTOKENMANAGER", "true", p.getUserTokenManager());
    }
    
    public void testUserCharStream() {
    	
    	p.setUserCharStream("true");
    	assertEquals("USERCHARSTREAM", "true", p.getUserCharStream());
    }
    
    public void testBuildParser() {
    	
    	p.setBuildParser("true");
    	
    	assertEquals("BUILDPARSER", "true", p.getBuildParser());
    }
    
    public void testBuildTokenManager() {
    	
    	p.setBuildTokenManager("true");
    	assertEquals("BUILDTOKENMANAGER", "true", p.getBuildTokenManager());
    }
    
    public void testSanityCheck() {
    	
    	p.setSanityCheck("true");
    	assertEquals("SANITYCHECK", "true", p.getSanityCheck());
    }
    
    public void testForceLaCheck() {
    	
    	p.setForceLaCheck("true");
    	assertEquals("FORCELACHECK", "true", p.getForceLaCheck());
    }
    
    public void testCacheTokens() {
    	
    	p.setCacheTokens("true");
    	assertEquals("CACHETOKENS", "true", p.getCacheTokens());
    }
    
    public void testKeepLineColumn() {
    	
    	p.setKeepLineColumn("true");
    	assertEquals("KEEPLINECOLUMN", "true", p.getKeepLineColumn());
    }
}
